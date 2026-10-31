package com.hms.mock;

import com.hms.dto.MedicineFormDto;
import com.hms.dto.MedicineLotFormDto;
import com.hms.model.Medicine;
import com.hms.model.MedicineLot;
import com.hms.model.Prescription;
import com.hms.model.PrescriptionDetail;
import com.hms.service.PharmacyDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MockPharmacyService {

    private final MockDataStore store;

    @Autowired(required = false)
    private PharmacyDbService pharmacyDbService;

    public MockPharmacyService(MockDataStore store) {
        this.store = store;
    }

    public List<Medicine> getAllMedicines() {
        return new ArrayList<>(store.medicines.values()).stream()
                .sorted((a, b) -> a.getIdThuoc().compareTo(b.getIdThuoc()))
                .collect(Collectors.toList());
    }

    public List<MedicineLot> getAllMedicineLots() {
        return new ArrayList<>(store.medicineLots.values()).stream()
                .sorted(Comparator.comparing(MedicineLot::getHanSuDung))
                .collect(Collectors.toList());
    }

    public List<Prescription> getAllPrescriptions() {
        store.linkAll();
        return new ArrayList<>(store.prescriptions.values()).stream()
                .sorted((a, b) -> b.getIdDt().compareTo(a.getIdDt()))
                .collect(Collectors.toList());
    }

    public Prescription getPrescriptionById(String id) {
        if (id == null) return null;
        store.linkAll();
        return store.prescriptions.get(id);
    }

    public Prescription getPrescriptionByExamId(String examId) {
        if (examId == null) return null;
        store.linkAll();
        return store.prescriptions.values().stream()
                .filter(p -> examId.equals(p.getIdPhieukham()))
                .findFirst()
                .orElse(null);
    }

    public Medicine getMedicineById(String id) {
        if (id == null) return null;
        return store.medicines.get(id);
    }

    public MedicineLot getLotById(String idLo) {
        if (idLo == null) return null;
        return store.medicineLots.get(idLo);
    }

    /**
     * Thêm thuốc mới vào danh mục
     */
    public synchronized Medicine createMedicine(MedicineFormDto dto) {
        if (dto.getTenThuoc() == null || dto.getTenThuoc().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thuốc không được để trống.");
        }
        if (dto.getDonVi() == null || dto.getDonVi().trim().isEmpty()) {
            throw new IllegalArgumentException("Đơn vị tính không được để trống.");
        }
        if (dto.getGiaBan() != null && dto.getGiaBan() < 0) {
            throw new IllegalArgumentException("Giá bán không được nhỏ hơn 0.");
        }

        String idThuoc = dto.getIdThuoc() != null ? dto.getIdThuoc().trim().toUpperCase() : "";
        if (idThuoc.isEmpty()) {
            idThuoc = store.nextMedicineId();
        } else if (store.medicines.containsKey(idThuoc)) {
            throw new IllegalArgumentException("Mã thuốc [" + idThuoc + "] đã tồn tại trong danh mục.");
        }

        Medicine med = new Medicine(
                idThuoc,
                dto.getTenThuoc().trim(),
                dto.getHoatChat() != null ? dto.getHoatChat().trim() : "",
                dto.getHamLuong() != null ? dto.getHamLuong().trim() : "",
                dto.getNhaSanXuat() != null ? dto.getNhaSanXuat().trim() : "",
                Boolean.TRUE.equals(dto.getCanKeDon()),
                0, // Khởi tạo tồn kho = 0 khi chưa nhập lô
                dto.getDonVi().trim(),
                dto.getGiaBan() != null ? dto.getGiaBan() : 0L,
                "Đang kinh doanh"
        );

        store.medicines.put(idThuoc, med);
        if (pharmacyDbService != null) {
            try { pharmacyDbService.saveMedicine(med); } catch (Exception ignored) {}
        }
        return med;
    }

    /**
     * Xóa thuốc khỏi danh mục:
     * - Nếu đã có lô hoặc có trong đơn thuốc -> chuyển sang 'Ngừng kinh doanh' để bảo toàn dữ liệu.
     * - Nếu chưa phát sinh bất kỳ lô hay đơn thuốc nào -> xóa vật lý.
     */
    public synchronized String deleteMedicine(String idThuoc) {
        Medicine med = store.medicines.get(idThuoc);
        if (med == null) {
            throw new IllegalArgumentException("Không tìm thấy mã thuốc: " + idThuoc);
        }

        boolean hasLots = store.medicineLots.values().stream()
                .anyMatch(l -> idThuoc.equalsIgnoreCase(l.getIdThuoc()));
        boolean hasRxDetails = store.prescriptionDetails.values().stream()
                .anyMatch(d -> idThuoc.equalsIgnoreCase(d.getIdThuoc()));

        if (hasLots || hasRxDetails) {
            med.setTrangThai("Ngừng kinh doanh");
            return "Thuốc [" + med.getTenThuoc() + "] đã có lô hoặc đơn thuốc liên kết. Đã chuyển trạng thái sang [Ngừng kinh doanh] để bảo toàn lịch sử dữ liệu.";
        } else {
            store.medicines.remove(idThuoc);
            return "Đã xóa hoàn toàn thuốc [" + idThuoc + " - " + med.getTenThuoc() + "] khỏi danh mục thành công.";
        }
    }

    /**
     * Tính toán và đồng bộ lại tổng tồn kho của thuốc từ các lô còn hạn và đang hoạt động
     */
    public synchronized void recalculateMedicineStock(String idThuoc) {
        if (idThuoc == null) return;
        Medicine med = store.medicines.get(idThuoc);
        if (med == null) return;

        int totalStock = store.medicineLots.values().stream()
                .filter(lot -> idThuoc.equalsIgnoreCase(lot.getIdThuoc())
                        && !lot.isExpired()
                        && !lot.isRemoved()
                        && lot.getSoLuongTon() != null && lot.getSoLuongTon() > 0)
                .mapToInt(MedicineLot::getSoLuongTon)
                .sum();

        med.setSoLuongTonKho(totalStock);
    }

    /**
     * Nhập lô thuốc mới
     */
    public synchronized MedicineLot createLot(MedicineLotFormDto dto) {
        if (dto.getIdThuoc() == null || dto.getIdThuoc().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn thuốc hợp lệ.");
        }
        Medicine med = store.medicines.get(dto.getIdThuoc().trim());
        if (med == null) {
            throw new IllegalArgumentException("Thuốc [" + dto.getIdThuoc() + "] không tồn tại.");
        }
        if (dto.getSoLo() == null || dto.getSoLo().trim().isEmpty()) {
            throw new IllegalArgumentException("Số lô không được để trống.");
        }
        String soLo = dto.getSoLo().trim().toUpperCase();
        boolean existsSoLo = store.medicineLots.values().stream()
                .anyMatch(l -> soLo.equalsIgnoreCase(l.getSoLo()));
        if (existsSoLo) {
            throw new IllegalArgumentException("Số lô [" + soLo + "] đã tồn tại trong hệ thống.");
        }
        if (dto.getSoLuongNhap() == null || dto.getSoLuongNhap() <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
        }
        if (dto.getGiaNhap() != null && dto.getGiaNhap() < 0) {
            throw new IllegalArgumentException("Giá nhập không được âm.");
        }
        if (dto.getGiaBan() != null && dto.getGiaBan() < 0) {
            throw new IllegalArgumentException("Giá bán không được âm.");
        }
        if (dto.getHanSuDung() == null || dto.getHanSuDung().trim().isEmpty()) {
            throw new IllegalArgumentException("Hạn sử dụng không được để trống.");
        }

        LocalDate ngayNhap = LocalDate.now();
        if (dto.getNgayNhap() != null && !dto.getNgayNhap().trim().isEmpty()) {
            try {
                ngayNhap = LocalDate.parse(dto.getNgayNhap().trim());
            } catch (Exception ignored) {}
        }

        try {
            LocalDate expDate = LocalDate.parse(dto.getHanSuDung().trim());
            if (!expDate.isAfter(ngayNhap)) {
                throw new IllegalArgumentException("Hạn sử dụng (" + expDate + ") phải sau ngày nhập (" + ngayNhap + ").");
            }
            if (dto.getNgaySanXuat() != null && !dto.getNgaySanXuat().trim().isEmpty()) {
                LocalDate mfgDate = LocalDate.parse(dto.getNgaySanXuat().trim());
                if (mfgDate.isAfter(expDate)) {
                    throw new IllegalArgumentException("Ngày sản xuất không thể sau hạn sử dụng.");
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ (chuẩn yyyy-MM-dd).");
        }

        String idLo = store.nextLotId();
        MedicineLot lot = new MedicineLot(
                idLo,
                med.getIdThuoc(),
                soLo,
                dto.getHanSuDung().trim(),
                dto.getSoLuongNhap(),
                dto.getSoLuongNhap(), // Tồn kho ban đầu bằng số lượng nhập
                dto.getGiaNhap() != null ? dto.getGiaNhap() : 0L,
                dto.getGiaBan() != null ? dto.getGiaBan() : (med.getGiaBan() != null ? med.getGiaBan() : 0L),
                ngayNhap.toString(),
                dto.getNgaySanXuat() != null ? dto.getNgaySanXuat().trim() : "",
                dto.getNhaCungCap() != null ? dto.getNhaCungCap().trim() : "",
                "Hoạt động"
        );
        lot.setTenThuoc(med.getTenThuoc());
        lot.setDonVi(med.getDonVi());

        store.medicineLots.put(idLo, lot);
        recalculateMedicineStock(med.getIdThuoc());
        if (pharmacyDbService != null) {
            try { pharmacyDbService.saveLot(lot); } catch (Exception ignored) {}
        }
        return lot;
    }

    /**
     * Xóa / Loại bỏ lô thuốc đã hết hạn
     */
    public synchronized String deleteExpiredLot(String idLo) {
        MedicineLot lot = store.medicineLots.get(idLo);
        if (lot == null) {
            throw new IllegalArgumentException("Không tìm thấy lô thuốc mã: " + idLo);
        }

        if (!lot.isExpired()) {
            throw new IllegalStateException("Lô thuốc [" + lot.getSoLo() + "] còn hạn sử dụng đến " + lot.getHanSuDung() + ", không thể loại bỏ theo quy định!");
        }

        int tonTruoc = lot.getSoLuongTon() != null ? lot.getSoLuongTon() : 0;
        String idThuoc = lot.getIdThuoc();

        // Kiểm tra xem lô đã từng xuất kho chưa
        if (lot.getSoLuongNhap() != null && tonTruoc < lot.getSoLuongNhap()) {
            // Đã từng cấp phát -> bảo toàn lịch sử xuất nhập, chuyển trạng thái và gán tồn = 0
            lot.setTrangThai("Đã loại bỏ (Hết hạn)");
            lot.setSoLuongTon(0);
        } else {
            // Chưa từng xuất kho -> xóa khỏi danh sách
            store.medicineLots.remove(idLo);
        }

        recalculateMedicineStock(idThuoc);
        store.linkAll();

        if (pharmacyDbService != null) {
            try { pharmacyDbService.deleteLot(idLo); } catch (Exception ignored) {}
        }

        return "Đã loại bỏ lô thuốc hết hạn [" + lot.getSoLo() + "] (HSD: " + lot.getHanSuDung() + ") và trừ " + tonTruoc + " đơn vị tồn kho.";
    }

    public synchronized boolean dispensePrescription(String prescriptionId) {
        Prescription rx = store.prescriptions.get(prescriptionId);
        if (rx == null || "Đã cấp phát".equalsIgnoreCase(rx.getTrangThai())) {
            return false;
        }

        List<PrescriptionDetail> details = rx.getItems();
        if (details == null || details.isEmpty()) {
            details = store.prescriptionDetails.values().stream()
                    .filter(d -> prescriptionId.equals(d.getIdDt()))
                    .collect(Collectors.toList());
        }

        // Deduct inventory
        for (PrescriptionDetail d : details) {
            String medId = d.getIdThuoc();
            int qtyNeeded = d.getSoLuong();

            // Deduct lots by FEFO (First Expired First Out) - CHỈ LÔ CÒN HẠN VÀ HOẠT ĐỘNG
            List<MedicineLot> lots = store.medicineLots.values().stream()
                    .filter(lot -> medId.equals(lot.getIdThuoc())
                            && lot.getSoLuongTon() > 0
                            && !lot.isExpired()
                            && !lot.isRemoved())
                    .sorted(Comparator.comparing(MedicineLot::getHanSuDung))
                    .collect(Collectors.toList());

            int remaining = qtyNeeded;
            for (MedicineLot lot : lots) {
                if (remaining <= 0) break;
                int lotStock = lot.getSoLuongTon();
                int deduct = Math.min(lotStock, remaining);
                lot.setSoLuongTon(lotStock - deduct);
                remaining -= deduct;
            }

            // Đồng bộ lại tổng tồn kho thuốc
            recalculateMedicineStock(medId);
        }

        rx.setTrangThai("Đã cấp phát");
        store.linkAll();
        return true;
    }

    public List<Medicine> searchMedicines(String keyword, String status) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return getAllMedicines().stream()
                .filter(m -> {
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
                        if ("active".equalsIgnoreCase(status) && m.isDiscontinued()) return false;
                        if ("discontinued".equalsIgnoreCase(status) && !m.isDiscontinued()) return false;
                        if ("outOfStock".equalsIgnoreCase(status) && (m.getSoLuongTonKho() != null && m.getSoLuongTonKho() > 0)) return false;
                    }
                    if (kw.isEmpty()) return true;
                    String target = (m.getIdThuoc() + " " + m.getTenThuoc() + " " + (m.getHoatChat() != null ? m.getHoatChat() : "") + " " + (m.getNhaSanXuat() != null ? m.getNhaSanXuat() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }

    public List<MedicineLot> searchLots(String keyword, String status) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return getAllMedicineLots().stream()
                .filter(lot -> {
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
                        if ("expired".equalsIgnoreCase(status) && !lot.isExpired()) return false;
                        if ("expiring".equalsIgnoreCase(status) && !lot.isExpiringSoon()) return false;
                        if ("valid".equalsIgnoreCase(status) && (lot.isExpired() || lot.isExpiringSoon())) return false;
                    }
                    if (kw.isEmpty()) return true;
                    String target = (lot.getSoLo() + " " + lot.getIdThuoc() + " " + (lot.getTenThuoc() != null ? lot.getTenThuoc() : "") + " " + (lot.getNhaCungCap() != null ? lot.getNhaCungCap() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }

    public List<Medicine> searchMedicines(String keyword) {
        return searchMedicines(keyword, null);
    }

    public List<Prescription> searchPrescriptions(String keyword, String status) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        store.linkAll();
        return getAllPrescriptions().stream()
                .filter(p -> {
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status) && !status.equalsIgnoreCase(p.getTrangThai())) {
                        return false;
                    }
                    if (kw.isEmpty()) return true;
                    String target = (p.getIdDt() + " " + p.getIdPhieukham() + " " + (p.getPatientName() != null ? p.getPatientName() : "") + " " + (p.getPatientId() != null ? p.getPatientId() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }
}


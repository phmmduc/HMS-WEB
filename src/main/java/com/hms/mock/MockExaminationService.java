package com.hms.mock;

import com.hms.dto.DoctorExamDto;
import com.hms.dto.ExamFormDto;
import com.hms.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MockExaminationService {

    private final MockDataStore store;

    public MockExaminationService(MockDataStore store) {
        this.store = store;
    }

    public List<Examination> getAllExaminations() {
        store.linkAll();
        return new ArrayList<>(store.examinations.values()).stream()
                .sorted((a, b) -> b.getIdPhieukham().compareTo(a.getIdPhieukham()))
                .collect(Collectors.toList());
    }

    public Examination getExaminationById(String id) {
        if (id == null) return null;
        store.linkAll();
        return store.examinations.get(id);
    }

    public List<Examination> searchExaminations(String keyword, String status, String roomId) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        store.linkAll();
        return getAllExaminations().stream()
                .filter(ex -> {
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status) && !status.equalsIgnoreCase(ex.getTrangThai())) {
                        return false;
                    }
                    if (roomId != null && !roomId.isEmpty() && !"all".equalsIgnoreCase(roomId) && !roomId.equalsIgnoreCase(ex.getIdPk())) {
                        return false;
                    }
                    if (kw.isEmpty()) return true;
                    String target = (ex.getIdPhieukham() + " " + ex.getPatientName() + " " + ex.getIdBn() + " " + (ex.getRoomName() != null ? ex.getRoomName() : "") + " " + (ex.getDoctorName() != null ? ex.getDoctorName() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }

    public Examination createExamination(ExamFormDto dto) {
        String examId = store.nextExamId();
        Examination exam = new Examination();
        exam.setIdPhieukham(examId);
        exam.setIdBn(dto.getIdBn());
        exam.setIdPk(dto.getIdPk());
        exam.setIdNv(dto.getIdNv());
        exam.setNgayDky(dto.getNgayDky() != null && !dto.getNgayDky().isEmpty() ? dto.getNgayDky() : LocalDate.now().toString());
        exam.setThoiGianKham(dto.getThoiGianKham());
        exam.setTrieuChung(dto.getTrieuChung() != null ? dto.getTrieuChung().trim() : "");
        exam.setPhiKham(dto.getPhiKham() != null ? dto.getPhiKham() : 150000L);
        exam.setTrangThai("Chờ khám");

        store.examinations.put(examId, exam);

        // Auto-create initial billing invoice
        String invoiceId = store.nextInvoiceId();
        Invoice invoice = new Invoice(invoiceId, examId, exam.getPhiKham(), "Phí khám", "", "", "Chưa thanh toán");
        invoice.setIdBn(exam.getIdBn());
        store.invoices.put(invoiceId, invoice);

        store.linkAll();
        return exam;
    }

    public synchronized Examination updateDoctorExamination(DoctorExamDto dto) {
        String examId = dto.getIdPhieukham();
        Examination exam = store.examinations.get(examId);
        if (exam == null) return null;

        exam.setThoiGianKham(dto.getThoiGianKham());
        exam.setIdCd(dto.getIdCd() != null && !dto.getIdCd().isEmpty() ? dto.getIdCd() : null);
        exam.setTrieuChung(dto.getTrieuChung() != null ? dto.getTrieuChung().trim() : "");
        exam.setKetLuan(dto.getKetLuan() != null ? dto.getKetLuan().trim() : "");

        if (dto.getIdNv() != null && !dto.getIdNv().isBlank()) {
            exam.setIdNv(dto.getIdNv().trim());
        }

        // 1. Labs ordered
        List<String> chosenLabs = dto.getLabIds() != null ? dto.getLabIds() : Collections.emptyList();
        // Remove unselected labs for this exam
        store.labResults.values().removeIf(lr -> examId.equals(lr.getIdPhieukham()) && !chosenLabs.contains(lr.getIdLxn()));
        for (String labId : chosenLabs) {
            boolean exists = store.labResults.values().stream().anyMatch(lr -> examId.equals(lr.getIdPhieukham()) && labId.equals(lr.getIdLxn()));
            if (!exists) {
                String kqId = store.nextLabResultId();
                LabResult lr = new LabResult(kqId, examId, labId, "", "", LocalDate.now().toString(), "Chờ lấy mẫu");
                store.labResults.put(kqId, lr);
            }
        }

        // 2. Prescription
        List<String> medIds = dto.getMedicineId();
        List<Integer> qtys = dto.getQuantity();
        List<String> doses = dto.getDosage();
        List<String> usages = dto.getUsage();
        List<String> notes = dto.getMedicineNote();

        boolean hasMedicines = medIds != null && !medIds.isEmpty() && medIds.stream().anyMatch(id -> id != null && !id.trim().isEmpty());

        if (hasMedicines) {
            // Find or create prescription
            Prescription rx = store.prescriptions.values().stream()
                    .filter(p -> examId.equals(p.getIdPhieukham()))
                    .findFirst()
                    .orElse(null);

            String rxId = (rx != null) ? rx.getIdDt() : store.nextPrescriptionId();
            String docId = (dto.getIdNv() != null && !dto.getIdNv().isBlank()) ? dto.getIdNv().trim() : exam.getIdNv();

            if (rx == null) {
                rx = new Prescription(rxId, examId, docId, dto.getRxNgayKe() != null ? dto.getRxNgayKe() : LocalDate.now().toString(), 0L, dto.getRxGhiChu(), "Chờ cấp phát");
            } else {
                rx.setIdNv(docId);
                rx.setNgayKe(dto.getRxNgayKe() != null ? dto.getRxNgayKe() : LocalDate.now().toString());
                rx.setGhiChu(dto.getRxGhiChu());
            }

            // Remove existing details
            store.prescriptionDetails.values().removeIf(d -> rxId.equals(d.getIdDt()));

            long totalRx = 0L;
            List<PrescriptionDetail> newDetails = new ArrayList<>();
            for (int i = 0; i < medIds.size(); i++) {
                String mId = medIds.get(i);
                if (mId == null || mId.trim().isEmpty()) continue;
                int q = (qtys != null && i < qtys.size() && qtys.get(i) != null) ? qtys.get(i) : 1;
                if (q < 1) q = 1; // Validate quantity > 0
                String d = (doses != null && i < doses.size()) ? doses.get(i) : "";
                String u = (usages != null && i < usages.size()) ? usages.get(i) : "";
                String n = (notes != null && i < notes.size()) ? notes.get(i) : "";

                // Get unit price from lots
                long unitPrice = store.medicineLots.values().stream()
                        .filter(lot -> mId.equals(lot.getIdThuoc()) && lot.getSoLuongTon() > 0)
                        .findFirst()
                        .map(MedicineLot::getGiaBan)
                        .orElse(1000L);

                totalRx += (unitPrice * q);
                String detailId = store.nextPrescriptionDetailId();
                PrescriptionDetail pd = new PrescriptionDetail(detailId, rxId, mId, q, d, u, n, unitPrice);
                Medicine med = store.medicines.get(mId);
                if (med != null) {
                    pd.setTenThuoc(med.getTenThuoc());
                    pd.setDonVi(med.getDonVi());
                }
                store.prescriptionDetails.put(detailId, pd);
                newDetails.add(pd);
            }

            rx.setTongTien(totalRx);
            rx.setItems(newDetails);
            store.prescriptions.put(rxId, rx);
            exam.setPrescription(rx);
        } else {
            // If doctor removed all medicines
            Prescription rx = store.prescriptions.values().stream()
                    .filter(p -> examId.equals(p.getIdPhieukham()))
                    .findFirst()
                    .orElse(null);
            if (rx != null) {
                store.prescriptionDetails.values().removeIf(d -> rx.getIdDt().equals(d.getIdDt()));
                rx.setItems(Collections.emptyList());
                rx.setTongTien(0L);
            }
        }

        // Set status
        if (!chosenLabs.isEmpty()) {
            boolean hasPendingLabs = store.labResults.values().stream()
                    .filter(lr -> examId.equals(lr.getIdPhieukham()))
                    .anyMatch(lr -> !"Hoàn thành".equalsIgnoreCase(lr.getTrangThai()));
            exam.setTrangThai(hasPendingLabs ? "Chờ xét nghiệm" : "Chờ thanh toán");
        } else {
            exam.setTrangThai("Chờ thanh toán");
        }

        // 3. Recalculate Invoice
        syncInvoice(examId);

        store.linkAll();
        return exam;
    }

    public void syncInvoice(String examId) {
        Examination exam = store.examinations.get(examId);
        if (exam == null) return;

        long labAmount = store.labResults.values().stream()
                .filter(lr -> examId.equals(lr.getIdPhieukham()))
                .mapToLong(lr -> {
                    LabType lt = store.labTypes.get(lr.getIdLxn());
                    return lt != null ? lt.getChiPhi() : 0L;
                }).sum();

        long rxAmount = store.prescriptions.values().stream()
                .filter(rx -> examId.equals(rx.getIdPhieukham()))
                .mapToLong(Prescription::getTongTien)
                .sum();

        long total = (exam.getPhiKham() != null ? exam.getPhiKham() : 0L) + labAmount + rxAmount;

        List<String> categories = new ArrayList<>();
        categories.add("Phí khám");
        if (labAmount > 0) categories.add("Xét nghiệm");
        if (rxAmount > 0) categories.add("Thuốc");

        Invoice inv = store.invoices.values().stream()
                .filter(i -> examId.equals(i.getIdPhieukham()))
                .findFirst()
                .orElse(null);

        if (inv != null) {
            inv.setSoTien(total);
            inv.setLoaiThu(String.join(" + ", categories));
            if (!"Đã thanh toán".equalsIgnoreCase(inv.getTrangThai())) {
                inv.setTrangThai("Chưa thanh toán");
            }
        } else {
            String invId = store.nextInvoiceId();
            Invoice newInv = new Invoice(invId, examId, total, String.join(" + ", categories), "", "", "Chưa thanh toán");
            newInv.setIdBn(exam.getIdBn());
            store.invoices.put(invId, newInv);
        }
    }

    public List<Examination> getQueueForDoctor(String keyword) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        store.linkAll();
        return store.examinations.values().stream()
                .filter(ex -> !"Hoàn tất".equalsIgnoreCase(ex.getTrangThai()))
                .filter(ex -> {
                    if (kw.isEmpty()) return true;
                    String target = (ex.getIdPhieukham() + " " + ex.getPatientName() + " " + (ex.getRoomName() != null ? ex.getRoomName() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .sorted((a, b) -> a.getIdPhieukham().compareTo(b.getIdPhieukham()))
                .collect(Collectors.toList());
    }

    public List<Prescription> getRecentPrescriptionsForDoctor(String keyword) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        store.linkAll();
        return store.prescriptions.values().stream()
                .filter(p -> {
                    if (kw.isEmpty()) return true;
                    String target = (p.getIdDt() + " " + p.getIdPhieukham() + " " + (p.getPatientName() != null ? p.getPatientName() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .sorted((a, b) -> b.getIdDt().compareTo(a.getIdDt()))
                .collect(Collectors.toList());
    }

    public List<Examination> getExaminationsForStaff(Staff staff, String keyword, String status, String roomId) {
        if (staff == null || !"Bác sĩ".equalsIgnoreCase(staff.getVaiTro())) {
            return searchExaminations(keyword, status, roomId);
        }

        String docId = staff.getIdNv();
        String docRoomId = staff.getIdPk();

        return searchExaminations(keyword, status, roomId).stream()
                .filter(ex -> (docId != null && docId.equals(ex.getIdNv()))
                        || (docRoomId != null && docRoomId.equals(ex.getIdPk())))
                .collect(Collectors.toList());
    }

    public boolean isDoctorAllowedToView(Staff staff, Examination exam) {
        if (staff == null || exam == null || !"Bác sĩ".equalsIgnoreCase(staff.getVaiTro())) {
            return true;
        }
        String docId = staff.getIdNv();
        String docRoomId = staff.getIdPk();
        return (docId != null && docId.equals(exam.getIdNv()))
                || (docRoomId != null && docRoomId.equals(exam.getIdPk()));
    }
}


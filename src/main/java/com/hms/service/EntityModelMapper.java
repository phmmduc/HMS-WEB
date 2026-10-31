package com.hms.service;

import com.hms.entity.*;
import com.hms.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EntityModelMapper {

    // ==========================================
    // 1. BENH NHAN <-> PATIENT
    // ==========================================
    public Patient toPatientModel(BenhNhan entity,
                                 List<BenhNhanSdt> phones,
                                 NguoiThan relative,
                                 List<NguoiThanSdt> relPhones,
                                 BaoHiemYTe insurance) {
        if (entity == null) return null;
        Patient model = new Patient();
        model.setIdBn(entity.getIdBn());
        model.setHoTen(entity.getHoTen());
        model.setCccd(entity.getCccd() != null ? entity.getCccd().trim() : "");
        model.setNgaySinh(entity.getNgaySinh() != null ? entity.getNgaySinh().toString() : "");
        model.setGioiTinh(entity.getGioiTinh() != null ? entity.getGioiTinh() : "Nam");

        if (phones != null && !phones.isEmpty()) {
            model.setSdt(phones.stream()
                    .filter(p -> p != null && p.getId() != null && p.getId().getSdt() != null)
                    .map(p -> p.getId().getSdt())
                    .collect(Collectors.toList()));
        } else {
            model.setSdt(new ArrayList<>());
        }

        model.setDiaChi("Hà Nội");
        model.setNgayTao(LocalDate.now().toString());
        model.setTrangThai("Ổn định");

        // Relative
        if (relative != null) {
            PatientRelative rel = new PatientRelative();
            rel.setIdNt(relative.getIdNt());
            rel.setIdBn(relative.getIdBn());
            rel.setHoTen(relative.getHoTen());
            rel.setMoiQuanHe(relative.getMoiQuanHe());
            rel.setGioiTinh(relative.getGioiTinh());
            if (relPhones != null && !relPhones.isEmpty()) {
                rel.setSdt(relPhones.stream()
                        .filter(p -> p != null && p.getId() != null && p.getId().getSdt() != null)
                        .map(p -> p.getId().getSdt())
                        .collect(Collectors.toList()));
            } else {
                rel.setSdt(new ArrayList<>());
            }
            model.setRelative(rel);
        }

        // Insurance
        if (insurance != null) {
            HealthInsurance ins = new HealthInsurance();
            ins.setIdBhyt(insurance.getSobhyt());
            ins.setSoBhyt(insurance.getSobhyt());
            ins.setIdBn(insurance.getIdBn());
            ins.setMucHuong(insurance.getMucHuong() != null ? Integer.valueOf(insurance.getMucHuong().intValue()) : 80);
            ins.setNhomDoiTuong(insurance.getNhomDoiTuong());
            ins.setNoiDangKy(insurance.getNoiDangKy());
            ins.setNgayBatDau(insurance.getNgayBatDau() != null ? insurance.getNgayBatDau().toString() : "");
            ins.setNgayKetThuc(insurance.getNgayKetThuc() != null ? insurance.getNgayKetThuc().toString() : "");
            model.setInsurance(ins);
        }

        return model;
    }

    public BenhNhan toBenhNhanEntity(Patient model) {
        if (model == null) return null;
        BenhNhan entity = new BenhNhan();
        entity.setIdBn(model.getIdBn());
        entity.setHoTen(model.getHoTen());
        entity.setCccd(model.getCccd());
        if (model.getNgaySinh() != null && !model.getNgaySinh().isBlank()) {
            try {
                entity.setNgaySinh(LocalDate.parse(model.getNgaySinh().trim()));
            } catch (Exception ignored) {}
        }
        entity.setGioiTinh(model.getGioiTinh());
        return entity;
    }

    // ==========================================
    // 2. NHAN VIEN <-> STAFF
    // ==========================================
    public Staff toStaffModel(NhanVien nv,
                             List<NhanVienSdt> phones,
                             BacSy bacSy,
                             YTa yTa,
                             DuocSi duocSi) {
        if (nv == null) return null;
        Staff model = new Staff();
        model.setIdNv(nv.getIdNv());
        model.setHoTen(nv.getHoTen());
        model.setGioiTinh(nv.getGioiTinh() != null ? nv.getGioiTinh() : "Nam");
        model.setNgaySinh(nv.getNgaySinh() != null ? nv.getNgaySinh().toString() : "");
        model.setEmail(nv.getIdNv() + "@hms.local");
        model.setTrangThai(Staff.normalizeTrangThai(nv.getTrangThai()));

        if (phones != null && !phones.isEmpty()) {
            model.setPhoneNumbers(phones.stream()
                    .filter(p -> p != null && p.getId() != null && p.getId().getSdt() != null)
                    .map(p -> p.getId().getSdt())
                    .collect(Collectors.toList()));
        } else {
            model.setPhoneNumbers(new ArrayList<>());
        }

        if (bacSy != null) {
            model.setVaiTro("Bác sĩ");
            model.setChucDanh(bacSy.getHocVi() != null ? bacSy.getHocVi() : "Bác sĩ CKI");
            if (bacSy.getChuyenKhoa() != null) {
                model.setSpecialtyDetail(bacSy.getChuyenKhoa().getTenCk());
            }
        } else if (yTa != null) {
            model.setVaiTro("Y tá");
            model.setChucDanh(yTa.getCapBac() != null ? yTa.getCapBac() : "Điều dưỡng");
        } else if (duocSi != null) {
            model.setVaiTro("Dược sĩ");
            model.setChucDanh(duocSi.getBangCap() != null ? duocSi.getBangCap() : "Dược sĩ đại học");
        } else {
            model.setVaiTro(nv.getVaiTro() != null ? nv.getVaiTro() : "Quản lý");
            model.setChucDanh("Cán bộ nhân viên");
        }

        return model;
    }

    public NhanVien toNhanVienEntity(Staff model) {
        if (model == null) return null;
        NhanVien entity = new NhanVien();
        entity.setIdNv(model.getIdNv());
        entity.setHoTen(model.getHoTen());
        entity.setGioiTinh(model.getGioiTinh());
        if (model.getNgaySinh() != null && !model.getNgaySinh().isBlank()) {
            try {
                entity.setNgaySinh(LocalDate.parse(model.getNgaySinh().trim()));
            } catch (Exception ignored) {}
        }
        entity.setVaiTro(model.getVaiTro());
        entity.setTrangThai(Staff.normalizeTrangThai(model.getTrangThai()));
        return entity;
    }

    // ==========================================
    // 3. CHUYEN KHOA & PHONG KHAM
    // ==========================================
    public Specialty toSpecialtyModel(ChuyenKhoa ck) {
        if (ck == null) return null;
        return new Specialty(ck.getIdCk(), ck.getTenCk());
    }

    public ChuyenKhoa toChuyenKhoaEntity(Specialty model) {
        if (model == null) return null;
        ChuyenKhoa entity = new ChuyenKhoa();
        entity.setIdCk(model.getIdCk());
        entity.setTenCk(model.getTenCk());
        return entity;
    }

    public ClinicRoom toClinicRoomModel(PhongKham pk) {
        if (pk == null) return null;
        ClinicRoom model = new ClinicRoom();
        model.setIdPk(pk.getIdPk());
        model.setTenPk(pk.getTenPk());
        model.setViTri(pk.getViTri());
        model.setIdCk(pk.getChuyenKhoa() != null ? pk.getChuyenKhoa().getIdCk() : null);
        model.setTenChuyenKhoa(pk.getChuyenKhoa() != null ? pk.getChuyenKhoa().getTenCk() : "");
        model.setTrangThai("Hoạt động");
        return model;
    }

    public PhongKham toPhongKhamEntity(ClinicRoom model, ChuyenKhoa ck) {
        if (model == null) return null;
        PhongKham entity = new PhongKham();
        entity.setIdPk(model.getIdPk());
        entity.setTenPk(model.getTenPk());
        entity.setViTri(model.getViTri());
        entity.setChuyenKhoa(ck);
        return entity;
    }

    // ==========================================
    // 4. CHAN DOAN & LOAI XET NGHIEM
    // ==========================================
    public Diagnosis toDiagnosisModel(ChanDoan cd) {
        if (cd == null) return null;
        return new Diagnosis(cd.getIdCd(), cd.getMaIcd(), cd.getTenBenh());
    }

    public ChanDoan toChanDoanEntity(Diagnosis model) {
        if (model == null) return null;
        ChanDoan entity = new ChanDoan();
        entity.setIdCd(model.getIdCd());
        entity.setMaIcd(model.getMaIcd());
        entity.setTenBenh(model.getTenBenh());
        return entity;
    }

    public LabType toLabTypeModel(LoaiXetNghiem lxn) {
        if (lxn == null) return null;
        long chiPhi = lxn.getChiPhi() != null ? lxn.getChiPhi().longValue() : 0L;
        return new LabType(lxn.getIdLoaiXn(), lxn.getTenLoai(), chiPhi, "Hoạt động");
    }

    public LoaiXetNghiem toLoaiXetNghiemEntity(LabType model) {
        if (model == null) return null;
        LoaiXetNghiem entity = new LoaiXetNghiem();
        entity.setIdLoaiXn(model.getIdLoaiXn());
        entity.setTenLoai(model.getTenLoai());
        entity.setChiPhi(BigDecimal.valueOf(model.getChiPhi()));
        return entity;
    }

    public LabResult toLabResultModel(KetQuaXetNghiem kq) {
        if (kq == null) return null;
        LabResult model = new LabResult();
        model.setIdKqxn(kq.getIdKqxn());
        model.setIdPhieukham(kq.getPhieuKham() != null ? kq.getPhieuKham().getIdPhieukham() : null);
        model.setIdLxn(kq.getLoaiXetNghiem() != null ? kq.getLoaiXetNghiem().getIdLoaiXn() : null);
        model.setTenLxn(kq.getLoaiXetNghiem() != null ? kq.getLoaiXetNghiem().getTenLoai() : "");
        model.setKetQua(kq.getKetQua() != null ? kq.getKetQua() : "");
        model.setKetLuan(kq.getKetLuan() != null ? kq.getKetLuan() : "");
        model.setThoiGian(kq.getThoiGian() != null ? kq.getThoiGian().toString() : "");
        model.setTrangThai(kq.getTrangThai() != null ? kq.getTrangThai() : "Chờ lấy mẫu");
        return model;
    }

    // ==========================================
    // 5. THUOC & LO THUOC
    // ==========================================
    public Medicine toMedicineModel(Thuoc t, List<LoThuoc> lots) {
        if (t == null) return null;
        Medicine model = new Medicine();
        model.setIdThuoc(t.getIdThuoc());
        model.setTenThuoc(t.getTenThuoc());
        model.setNhaSanXuat(t.getNhaSanXuat());
        model.setCanKeDon(Boolean.TRUE.equals(t.getCanKeDon()));
        model.setSoLuongTonKho(t.getSoLuongTonKho() != null ? t.getSoLuongTonKho() : 0);
        model.setDonVi("Viên");
        model.setTrangThai("Đang kinh doanh");

        if (lots != null && !lots.isEmpty()) {
            model.setGiaBan(lots.get(0).getGiaBan() != null ? lots.get(0).getGiaBan().longValue() : 5000L);
        } else {
            model.setGiaBan(5000L);
        }
        return model;
    }

    public Thuoc toThuocEntity(Medicine model) {
        if (model == null) return null;
        Thuoc entity = new Thuoc();
        entity.setIdThuoc(model.getIdThuoc());
        entity.setTenThuoc(model.getTenThuoc());
        entity.setNhaSanXuat(model.getNhaSanXuat());
        entity.setCanKeDon(Boolean.TRUE.equals(model.getCanKeDon()));
        entity.setSoLuongTonKho(model.getSoLuongTonKho());
        return entity;
    }

    public MedicineLot toMedicineLotModel(LoThuoc lot) {
        if (lot == null) return null;
        MedicineLot model = new MedicineLot();
        model.setIdLo(lot.getIdLo());
        model.setIdThuoc(lot.getThuoc() != null ? lot.getThuoc().getIdThuoc() : null);
        model.setSoLo(lot.getSoLo());
        model.setHanSuDung(lot.getHanSuDung() != null ? lot.getHanSuDung().toString() : "");
        model.setSoLuongNhap(lot.getSoLuongNhap() != null ? lot.getSoLuongNhap() : 0);
        model.setSoLuongTon(lot.getSoLuongCon() != null ? lot.getSoLuongCon() : 0);
        model.setGiaNhap(lot.getGiaNhap() != null ? lot.getGiaNhap().longValue() : 0L);
        model.setGiaBan(lot.getGiaBan() != null ? lot.getGiaBan().longValue() : 0L);
        model.setNgayNhap(LocalDate.now().toString());
        if (lot.getThuoc() != null) {
            model.setTenThuoc(lot.getThuoc().getTenThuoc());
        }
        return model;
    }

    public LoThuoc toLoThuocEntity(MedicineLot model, Thuoc thuoc) {
        if (model == null) return null;
        LoThuoc entity = new LoThuoc();
        entity.setIdLo(model.getIdLo());
        entity.setThuoc(thuoc);
        entity.setSoLo(model.getSoLo());
        if (model.getHanSuDung() != null && !model.getHanSuDung().isBlank()) {
            try { entity.setHanSuDung(LocalDate.parse(model.getHanSuDung().trim())); } catch (Exception ignored) {}
        }
        entity.setSoLuongNhap(model.getSoLuongNhap());
        entity.setSoLuongCon(model.getSoLuongTon());
        entity.setGiaNhap(BigDecimal.valueOf(model.getGiaNhap()));
        entity.setGiaBan(BigDecimal.valueOf(model.getGiaBan()));
        return entity;
    }

    // ==========================================
    // 6. PHIEU KHAM <-> EXAMINATION
    // ==========================================
    public Examination toExaminationModel(PhieuKham pk) {
        if (pk == null) return null;
        Examination model = new Examination();
        model.setIdPhieukham(pk.getIdPhieukham());
        model.setIdBn(pk.getBenhAn() != null ? pk.getBenhAn().getIdBn() : null);
        model.setIdPk(pk.getPhongKham() != null ? pk.getPhongKham().getIdPk() : null);
        model.setIdNv(pk.getBacSy() != null ? pk.getBacSy().getIdNv() : null);

        if (pk.getThoiGianKham() != null) {
            model.setNgayDky(pk.getThoiGianKham().toLocalDate().toString());
            model.setThoiGianKham(pk.getThoiGianKham().toLocalTime().toString());
        } else if (pk.getThoiGianDky() != null) {
            model.setNgayDky(pk.getThoiGianDky().toLocalDate().toString());
            model.setThoiGianKham(pk.getThoiGianDky().toLocalTime().toString());
        } else {
            model.setNgayDky(LocalDate.now().toString());
            model.setThoiGianKham("08:00");
        }

        model.setTrieuChung(pk.getTrieuChung());
        model.setTrangThai(pk.getTrangThai() != null ? pk.getTrangThai() : "Chờ khám");
        model.setPhiKham(pk.getPhiKham() != null ? pk.getPhiKham().longValue() : 150000L);

        return model;
    }

    // ==========================================
    // 7. PHIEU THU VIEN PHI <-> INVOICE
    // ==========================================
    public Invoice toInvoiceModel(PhieuThuVienPhi pt) {
        if (pt == null) return null;
        Invoice model = new Invoice();
        model.setIdHoadon(pt.getIdHoadon());
        model.setIdPhieukham(pt.getPhieuKham() != null ? pt.getPhieuKham().getIdPhieukham() : null);
        model.setIdNv(pt.getNhanVien() != null ? pt.getNhanVien().getIdNv() : null);
        model.setSoTien(pt.getSoTien() != null ? pt.getSoTien().longValue() : 0L);
        model.setLoaiThu(pt.getLoaiThu() != null ? pt.getLoaiThu() : "Viện phí");
        model.setPhuongThucTt(pt.getPhuongThucTt() != null ? pt.getPhuongThucTt() : "TIEN_MAT");
        model.setThoiGianThu(pt.getThoiGianThu() != null ? pt.getThoiGianThu().toString() : "");
        model.setTrangThai(pt.getTrangThai() != null ? pt.getTrangThai() : "Chưa thanh toán");

        if (pt.getPhieuKham() != null && pt.getPhieuKham().getBenhAn() != null) {
            model.setIdBn(pt.getPhieuKham().getBenhAn().getIdBn());
        }

        model.setTienKham(150000L);
        model.setBenhNhanTra(model.getSoTien());
        return model;
    }

    public PhieuThuVienPhi toPhieuThuVienPhiEntity(Invoice model, PhieuKham pk, NhanVien nv) {
        if (model == null) return null;
        PhieuThuVienPhi entity = new PhieuThuVienPhi();
        entity.setIdHoadon(model.getIdHoadon());
        entity.setPhieuKham(pk);
        entity.setNhanVien(nv);
        entity.setSoTien(BigDecimal.valueOf(model.getSoTien()));
        entity.setLoaiThu(model.getLoaiThu());
        entity.setPhuongThucTt(model.getPhuongThucTt() != null ? model.getPhuongThucTt() : "TIEN_MAT");
        entity.setThoiGianThu(LocalDateTime.now());
        entity.setTrangThai(model.getTrangThai() != null ? model.getTrangThai() : "DA_THU");
        return entity;
    }
}

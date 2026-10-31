package com.hms.service;

import com.hms.entity.*;
import com.hms.entity.id.BenhNhanSdtId;
import com.hms.entity.id.NhanVienSdtId;
import com.hms.mock.MockDataStore;
import com.hms.model.*;
import com.hms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DatabaseDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DatabaseDataSeeder.class);

    private final MockDataStore mockDataStore;
    private final ChuyenKhoaRepository chuyenKhoaRepo;
    private final PhongKhamRepository phongKhamRepo;
    private final NhanVienRepository nhanVienRepo;
    private final NhanVienSdtRepository nhanVienSdtRepo;
    private final BacSyRepository bacSyRepo;
    private final YTaRepository yTaRepo;
    private final DuocSiRepository duocSiRepo;
    private final ChanDoanRepository chanDoanRepo;
    private final LoaiXetNghiemRepository loaiXnRepo;
    private final ThuocRepository thuocRepo;
    private final LoThuocRepository loThuocRepo;
    private final BenhNhanRepository benhNhanRepo;
    private final BenhNhanSdtRepository benhNhanSdtRepo;
    private final BaoHiemYTeRepository baoHiemYTeRepo;
    private final BenhAnRepository benhAnRepo;
    private final PhieuKhamRepository phieuKhamRepo;
    private final PhieuThuVienPhiRepository phieuThuRepo;

    public DatabaseDataSeeder(MockDataStore mockDataStore,
                              ChuyenKhoaRepository chuyenKhoaRepo,
                              PhongKhamRepository phongKhamRepo,
                              NhanVienRepository nhanVienRepo,
                              NhanVienSdtRepository nhanVienSdtRepo,
                              BacSyRepository bacSyRepo,
                              YTaRepository yTaRepo,
                              DuocSiRepository duocSiRepo,
                              ChanDoanRepository chanDoanRepo,
                              LoaiXetNghiemRepository loaiXnRepo,
                              ThuocRepository thuocRepo,
                              LoThuocRepository loThuocRepo,
                              BenhNhanRepository benhNhanRepo,
                              BenhNhanSdtRepository benhNhanSdtRepo,
                              BaoHiemYTeRepository baoHiemYTeRepo,
                              BenhAnRepository benhAnRepo,
                              PhieuKhamRepository phieuKhamRepo,
                              PhieuThuVienPhiRepository phieuThuRepo) {
        this.mockDataStore = mockDataStore;
        this.chuyenKhoaRepo = chuyenKhoaRepo;
        this.phongKhamRepo = phongKhamRepo;
        this.nhanVienRepo = nhanVienRepo;
        this.nhanVienSdtRepo = nhanVienSdtRepo;
        this.bacSyRepo = bacSyRepo;
        this.yTaRepo = yTaRepo;
        this.duocSiRepo = duocSiRepo;
        this.chanDoanRepo = chanDoanRepo;
        this.loaiXnRepo = loaiXnRepo;
        this.thuocRepo = thuocRepo;
        this.loThuocRepo = loThuocRepo;
        this.benhNhanRepo = benhNhanRepo;
        this.benhNhanSdtRepo = benhNhanSdtRepo;
        this.baoHiemYTeRepo = baoHiemYTeRepo;
        this.benhAnRepo = benhAnRepo;
        this.phieuKhamRepo = phieuKhamRepo;
        this.phieuThuRepo = phieuThuRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDatabaseIfAvailable() {
        try {
            long count = chuyenKhoaRepo.count();
            log.info("Database connection verified. Existing specialties count: {}", count);
            if (count > 0) {
                log.info("Database already contains data. Skipping initial seeding.");
                return;
            }

            log.info("Database tables detected but empty. Seeding initial MediCore HMS standard records...");

            // 1. Specialties
            for (Specialty sp : mockDataStore.specialties.values()) {
                ChuyenKhoa ck = new ChuyenKhoa();
                ck.setIdCk(sp.getIdCk());
                ck.setTenCk(sp.getTenCk());
                chuyenKhoaRepo.save(ck);
            }

            // 2. Clinic Rooms
            for (ClinicRoom r : mockDataStore.rooms.values()) {
                PhongKham pk = new PhongKham();
                pk.setIdPk(r.getIdPk());
                pk.setTenPk(r.getTenPk());
                pk.setViTri(r.getViTri());
                if (r.getIdCk() != null) {
                    chuyenKhoaRepo.findById(r.getIdCk()).ifPresent(pk::setChuyenKhoa);
                }
                phongKhamRepo.save(pk);
            }

            // 3. Staff
            for (Staff s : mockDataStore.staffMembers.values()) {
                NhanVien nv = new NhanVien();
                nv.setIdNv(s.getIdNv());
                nv.setHoTen(s.getHoTen());
                nv.setGioiTinh(s.getGioiTinh());
                nv.setVaiTro(s.getVaiTro());
                nv.setTrangThai(Staff.normalizeTrangThai(s.getTrangThai()));
                if (s.getNgaySinh() != null && !s.getNgaySinh().isBlank()) {
                    try { nv.setNgaySinh(LocalDate.parse(s.getNgaySinh().trim())); } catch (Exception ignored) {}
                }
                nhanVienRepo.save(nv);

                if (s.getPhoneNumbers() != null) {
                    for (String ph : s.getPhoneNumbers()) {
                        nhanVienSdtRepo.save(new NhanVienSdt(new NhanVienSdtId(s.getIdNv(), ph), nv));
                    }
                }

                if ("Bác sĩ".equalsIgnoreCase(s.getVaiTro())) {
                    BacSy bs = new BacSy();
                    bs.setIdNv(s.getIdNv());
                    bs.setNhanVien(nv);
                    bs.setHocVi(s.getChucDanh());
                    bs.setSoCchn("CCHN-" + s.getIdNv());
                    if (s.getIdPk() != null) {
                        phongKhamRepo.findById(s.getIdPk()).ifPresent(p -> bs.setChuyenKhoa(p.getChuyenKhoa()));
                    }
                    if (bs.getChuyenKhoa() == null) {
                        chuyenKhoaRepo.findById("CK01").ifPresent(bs::setChuyenKhoa);
                    }
                    bacSyRepo.save(bs);
                } else if ("Y tá".equalsIgnoreCase(s.getVaiTro())) {
                    YTa y = new YTa();
                    y.setIdNv(s.getIdNv());
                    y.setNhanVien(nv);
                    y.setCapBac(s.getChucDanh());
                    yTaRepo.save(y);
                } else if ("Dược sĩ".equalsIgnoreCase(s.getVaiTro())) {
                    DuocSi ds = new DuocSi();
                    ds.setIdNv(s.getIdNv());
                    ds.setNhanVien(nv);
                    ds.setBangCap(s.getChucDanh());
                    duocSiRepo.save(ds);
                }
            }

            // 4. Diagnoses
            for (Diagnosis d : mockDataStore.diagnoses.values()) {
                ChanDoan cd = new ChanDoan();
                cd.setIdCd(d.getIdCd());
                cd.setMaIcd(d.getMaIcd());
                cd.setTenBenh(d.getTenBenh());
                chanDoanRepo.save(cd);
            }

            // 5. Lab Types
            for (LabType lt : mockDataStore.labTypes.values()) {
                LoaiXetNghiem lxn = new LoaiXetNghiem();
                lxn.setIdLoaiXn(lt.getIdLoaiXn());
                lxn.setTenLoai(lt.getTenLoai());
                lxn.setChiPhi(BigDecimal.valueOf(lt.getChiPhi()));
                loaiXnRepo.save(lxn);
            }

            // 6. Medicines & Lots
            for (Medicine m : mockDataStore.medicines.values()) {
                Thuoc t = new Thuoc();
                t.setIdThuoc(m.getIdThuoc());
                t.setTenThuoc(m.getTenThuoc());
                t.setNhaSanXuat(m.getNhaSanXuat());
                t.setCanKeDon(Boolean.TRUE.equals(m.getCanKeDon()));
                t.setSoLuongTonKho(m.getSoLuongTonKho());
                thuocRepo.save(t);
            }

            for (MedicineLot l : mockDataStore.medicineLots.values()) {
                LoThuoc lot = new LoThuoc();
                lot.setIdLo(l.getIdLo());
                thuocRepo.findById(l.getIdThuoc()).ifPresent(lot::setThuoc);
                lot.setSoLo(l.getSoLo());
                if (l.getHanSuDung() != null && !l.getHanSuDung().isBlank()) {
                    try { lot.setHanSuDung(LocalDate.parse(l.getHanSuDung().trim())); } catch (Exception ignored) {}
                }
                lot.setSoLuongNhap(l.getSoLuongNhap());
                lot.setSoLuongCon(l.getSoLuongTon());
                lot.setGiaNhap(BigDecimal.valueOf(l.getGiaNhap()));
                lot.setGiaBan(BigDecimal.valueOf(l.getGiaBan()));
                loThuocRepo.save(lot);
            }

            // 7. Patients
            for (Patient p : mockDataStore.patients.values()) {
                BenhNhan bn = new BenhNhan();
                bn.setIdBn(p.getIdBn());
                bn.setHoTen(p.getHoTen());
                bn.setCccd(p.getCccd());
                bn.setGioiTinh(p.getGioiTinh());
                if (p.getNgaySinh() != null && !p.getNgaySinh().isBlank()) {
                    try { bn.setNgaySinh(LocalDate.parse(p.getNgaySinh().trim())); } catch (Exception ignored) {}
                }
                benhNhanRepo.save(bn);

                if (p.getSdt() != null) {
                    for (String phone : p.getSdt()) {
                        benhNhanSdtRepo.save(new BenhNhanSdt(new BenhNhanSdtId(p.getIdBn(), phone), bn));
                    }
                }

                // Medical record
                BenhAn ba = new BenhAn();
                ba.setIdBenhan("BA" + p.getIdBn().replace("BN", ""));
                ba.setBenhNhan(bn);
                ba.setIdBn(p.getIdBn());
                ba.setNgayTao(LocalDate.now());
                ba.setTienSuBenh("Tiền sử sức khỏe ghi nhận ban đầu");
                ba.setTrangThai("DANG_MO");
                benhAnRepo.save(ba);

                if (p.getInsurance() != null) {
                    HealthInsurance ins = p.getInsurance();
                    BaoHiemYTe bh = new BaoHiemYTe();
                    bh.setSobhyt(ins.getSoBhyt());
                    bh.setBenhNhan(bn);
                    bh.setIdBn(p.getIdBn());
                    bh.setMucHuong(ins.getMucHuong() != null ? ins.getMucHuong().shortValue() : (short) 80);
                    bh.setNhomDoiTuong(ins.getNhomDoiTuong());
                    bh.setNoiDangKy(ins.getNoiDangKy());
                    if (ins.getNgayBatDau() != null && !ins.getNgayBatDau().isBlank()) {
                        try { bh.setNgayBatDau(LocalDate.parse(ins.getNgayBatDau().trim())); } catch (Exception ignored) {}
                    }
                    if (ins.getNgayKetThuc() != null && !ins.getNgayKetThuc().isBlank()) {
                        try { bh.setNgayKetThuc(LocalDate.parse(ins.getNgayKetThuc().trim())); } catch (Exception ignored) {}
                    }
                    baoHiemYTeRepo.save(bh);
                }
            }

            // 8. Examinations & Invoices
            for (Examination ex : mockDataStore.examinations.values()) {
                PhieuKham pk = new PhieuKham();
                pk.setIdPhieukham(ex.getIdPhieukham());
                benhAnRepo.findById("BA" + ex.getIdBn().replace("BN", "")).ifPresent(pk::setBenhAn);
                phongKhamRepo.findById(ex.getIdPk()).ifPresent(pk::setPhongKham);
                bacSyRepo.findById(ex.getIdNv()).ifPresent(pk::setBacSy);
                pk.setThoiGianKham(LocalDateTime.now());
                pk.setTrieuChung(ex.getTrieuChung());
                pk.setTrangThai(ex.getTrangThai());
                pk.setPhiKham(BigDecimal.valueOf(ex.getPhiKham() != null ? ex.getPhiKham() : 150000L));
                phieuKhamRepo.save(pk);
            }

            for (Invoice inv : mockDataStore.invoices.values()) {
                PhieuThuVienPhi pt = new PhieuThuVienPhi();
                pt.setIdHoadon(inv.getIdHoadon());
                phieuKhamRepo.findById(inv.getIdPhieukham()).ifPresent(pt::setPhieuKham);
                nhanVienRepo.findById(inv.getIdNv() != null ? inv.getIdNv() : "NV005").ifPresent(pt::setNhanVien);
                pt.setSoTien(BigDecimal.valueOf(inv.getSoTien()));
                pt.setLoaiThu(inv.getLoaiThu());
                pt.setPhuongThucTt(inv.getPhuongThucTt() != null ? inv.getPhuongThucTt() : "TIEN_MAT");
                pt.setThoiGianThu(LocalDateTime.now());
                pt.setTrangThai(inv.getTrangThai());
                phieuThuRepo.save(pt);
            }

            log.info("MediCore HMS Database successfully seeded with standard sample data.");
        } catch (Exception ex) {
            log.warn("Database initialization skipped or running with uninitialized DDL (Safe fallback active): {}", ex.getMessage());
        }
    }
}

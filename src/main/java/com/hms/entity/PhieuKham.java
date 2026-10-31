package com.hms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "phieukham")
public class PhieuKham {

    @Id
    @Column(name = "id_phieukham", length = 15, nullable = false)
    private String idPhieukham;

    @Column(name = "id_benhan", length = 10, insertable = false, updatable = false)
    private String idBenhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_benhan", nullable = false)
    private BenhAn benhAn;

    @Column(name = "id_pk", length = 10, insertable = false, updatable = false)
    private String idPk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pk")
    private PhongKham phongKham;

    @Column(name = "id_bacsy", length = 10, insertable = false, updatable = false)
    private String idBacsy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bacsy")
    private BacSy bacSy;

    @Column(name = "sobhyt", length = 15, insertable = false, updatable = false)
    private String sobhyt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sobhyt")
    private BaoHiemYTe baoHiemYTe;

    @Column(name = "thoi_gian_dky")
    private LocalDateTime thoiGianDky;

    @Column(name = "thoi_gian_kham")
    private LocalDateTime thoiGianKham;

    @Column(name = "trieu_chung", columnDefinition = "TEXT")
    private String trieuChung;

    @Column(name = "phi_kham", precision = 12, scale = 2)
    private BigDecimal phiKham;

    @Column(name = "trang_thai", length = 20)
    private String trangThai; // CHO_KHAM, DANG_KHAM, DA_KHAM, HUY

    @OneToMany(mappedBy = "phieuKham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<KetQuaXetNghiem> danhSachKqxn = new ArrayList<>();

    @OneToMany(mappedBy = "phieuKham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DonThuoc> danhSachDonThuoc = new ArrayList<>();

    @OneToMany(mappedBy = "phieuKham", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PhieuKhamChanDoan> danhSachChanDoan = new ArrayList<>();

    @OneToMany(mappedBy = "phieuKham", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PhieuKhamNhanVien> danhSachNhanVienTiepNhan = new ArrayList<>();

    @OneToMany(mappedBy = "phieuKham", fetch = FetchType.LAZY)
    private List<PhieuThuVienPhi> danhSachHoaDon = new ArrayList<>();

    public PhieuKham() {}

    public PhieuKham(String idPhieukham, BenhAn benhAn, PhongKham phongKham, BacSy bacSy, BaoHiemYTe baoHiemYTe,
                     LocalDateTime thoiGianDky, LocalDateTime thoiGianKham, String trieuChung, BigDecimal phiKham, String trangThai) {
        this.idPhieukham = idPhieukham;
        this.benhAn = benhAn;
        if (benhAn != null) this.idBenhan = benhAn.getIdBenhan();
        this.phongKham = phongKham;
        if (phongKham != null) this.idPk = phongKham.getIdPk();
        this.bacSy = bacSy;
        if (bacSy != null) this.idBacsy = bacSy.getIdNv();
        this.baoHiemYTe = baoHiemYTe;
        if (baoHiemYTe != null) this.sobhyt = baoHiemYTe.getSobhyt();
        this.thoiGianDky = thoiGianDky;
        this.thoiGianKham = thoiGianKham;
        this.trieuChung = trieuChung;
        this.phiKham = phiKham;
        this.trangThai = trangThai;
    }

    public String getIdPhieukham() { return idPhieukham; }
    public void setIdPhieukham(String idPhieukham) { this.idPhieukham = idPhieukham; }

    public String getIdBenhan() { return idBenhan; }
    public void setIdBenhan(String idBenhan) { this.idBenhan = idBenhan; }

    public BenhAn getBenhAn() { return benhAn; }
    public void setBenhAn(BenhAn benhAn) {
        this.benhAn = benhAn;
        if (benhAn != null) this.idBenhan = benhAn.getIdBenhan();
    }

    public String getIdPk() { return idPk; }
    public void setIdPk(String idPk) { this.idPk = idPk; }

    public PhongKham getPhongKham() { return phongKham; }
    public void setPhongKham(PhongKham phongKham) {
        this.phongKham = phongKham;
        if (phongKham != null) this.idPk = phongKham.getIdPk();
    }

    public String getIdBacsy() { return idBacsy; }
    public void setIdBacsy(String idBacsy) { this.idBacsy = idBacsy; }

    public BacSy getBacSy() { return bacSy; }
    public void setBacSy(BacSy bacSy) {
        this.bacSy = bacSy;
        if (bacSy != null) this.idBacsy = bacSy.getIdNv();
    }

    public String getSobhyt() { return sobhyt; }
    public void setSobhyt(String sobhyt) { this.sobhyt = sobhyt; }

    public BaoHiemYTe getBaoHiemYTe() { return baoHiemYTe; }
    public void setBaoHiemYTe(BaoHiemYTe baoHiemYTe) {
        this.baoHiemYTe = baoHiemYTe;
        if (baoHiemYTe != null) this.sobhyt = baoHiemYTe.getSobhyt();
    }

    public LocalDateTime getThoiGianDky() { return thoiGianDky; }
    public void setThoiGianDky(LocalDateTime thoiGianDky) { this.thoiGianDky = thoiGianDky; }

    public LocalDateTime getThoiGianKham() { return thoiGianKham; }
    public void setThoiGianKham(LocalDateTime thoiGianKham) { this.thoiGianKham = thoiGianKham; }

    public LocalDateTime getNgayKham() { return thoiGianKham; }
    public void setNgayKham(LocalDateTime ngayKham) { this.thoiGianKham = ngayKham; }

    public String getTrieuChung() { return trieuChung; }
    public void setTrieuChung(String trieuChung) { this.trieuChung = trieuChung; }

    public BigDecimal getPhiKham() { return phiKham; }
    public void setPhiKham(BigDecimal phiKham) { this.phiKham = phiKham; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public List<KetQuaXetNghiem> getDanhSachKqxn() { return danhSachKqxn; }
    public void setDanhSachKqxn(List<KetQuaXetNghiem> danhSachKqxn) { this.danhSachKqxn = danhSachKqxn; }

    public List<DonThuoc> getDanhSachDonThuoc() { return danhSachDonThuoc; }
    public void setDanhSachDonThuoc(List<DonThuoc> danhSachDonThuoc) { this.danhSachDonThuoc = danhSachDonThuoc; }

    public List<PhieuKhamChanDoan> getDanhSachChanDoan() { return danhSachChanDoan; }
    public void setDanhSachChanDoan(List<PhieuKhamChanDoan> danhSachChanDoan) { this.danhSachChanDoan = danhSachChanDoan; }

    public List<PhieuKhamNhanVien> getDanhSachNhanVienTiepNhan() { return danhSachNhanVienTiepNhan; }
    public void setDanhSachNhanVienTiepNhan(List<PhieuKhamNhanVien> danhSachNhanVienTiepNhan) { this.danhSachNhanVienTiepNhan = danhSachNhanVienTiepNhan; }

    public List<PhieuThuVienPhi> getDanhSachHoaDon() { return danhSachHoaDon; }
    public void setDanhSachHoaDon(List<PhieuThuVienPhi> danhSachHoaDon) { this.danhSachHoaDon = danhSachHoaDon; }
}

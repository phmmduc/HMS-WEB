package com.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "donthuoc")
public class DonThuoc {

    @Id
    @Column(name = "id_dt", length = 15, nullable = false)
    private String idDt;

    @Column(name = "id_phieukham", length = 15, insertable = false, updatable = false)
    private String idPhieukham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieukham", nullable = false)
    private PhieuKham phieuKham;

    @Column(name = "id_duocsi", length = 10, insertable = false, updatable = false)
    private String idDuocsi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_duocsi")
    private DuocSi duocSi;

    @Column(name = "ngay_ke")
    private LocalDate ngayKe;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "trang_thai", length = 20)
    private String trangThai; // CHO_CAP_PHAT, DA_CAP_PHAT, HUY

    @OneToMany(mappedBy = "donThuoc", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChiTietDonThuoc> chiTietList = new ArrayList<>();

    public DonThuoc() {}

    public DonThuoc(String idDt, PhieuKham phieuKham, DuocSi duocSi, LocalDate ngayKe, String ghiChu, String trangThai) {
        this.idDt = idDt;
        this.phieuKham = phieuKham;
        if (phieuKham != null) this.idPhieukham = phieuKham.getIdPhieukham();
        this.duocSi = duocSi;
        if (duocSi != null) this.idDuocsi = duocSi.getIdNv();
        this.ngayKe = ngayKe;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    public String getIdDt() { return idDt; }
    public void setIdDt(String idDt) { this.idDt = idDt; }

    public String getIdPhieukham() { return idPhieukham; }
    public void setIdPhieukham(String idPhieukham) { this.idPhieukham = idPhieukham; }

    public PhieuKham getPhieuKham() { return phieuKham; }
    public void setPhieuKham(PhieuKham phieuKham) {
        this.phieuKham = phieuKham;
        if (phieuKham != null) this.idPhieukham = phieuKham.getIdPhieukham();
    }

    public String getIdDuocsi() { return idDuocsi; }
    public void setIdDuocsi(String idDuocsi) { this.idDuocsi = idDuocsi; }

    public DuocSi getDuocSi() { return duocSi; }
    public void setDuocSi(DuocSi duocSi) {
        this.duocSi = duocSi;
        if (duocSi != null) this.idDuocsi = duocSi.getIdNv();
    }

    public LocalDate getNgayKe() { return ngayKe; }
    public void setNgayKe(LocalDate ngayKe) { this.ngayKe = ngayKe; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public List<ChiTietDonThuoc> getChiTietList() { return chiTietList; }
    public void setChiTietList(List<ChiTietDonThuoc> chiTietList) { this.chiTietList = chiTietList; }

    public void addChiTiet(ChiTietDonThuoc ct) {
        if (ct != null) {
            ct.setDonThuoc(this);
            this.chiTietList.add(ct);
        }
    }
}


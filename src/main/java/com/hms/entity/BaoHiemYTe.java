package com.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "baohiemyte")
public class BaoHiemYTe {

    @Id
    @Column(name = "sobhyt", length = 15, nullable = false)
    private String sobhyt;

    @Column(name = "id_bn", length = 10, insertable = false, updatable = false)
    private String idBn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bn", nullable = false)
    private BenhNhan benhNhan;

    @Column(name = "muc_huong")
    private Short mucHuong;

    @Column(name = "nhom_doi_tuong", length = 50)
    private String nhomDoiTuong;

    @Column(name = "noi_dang_ky", length = 150)
    private String noiDangKy;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    public BaoHiemYTe() {}

    public BaoHiemYTe(String sobhyt, BenhNhan benhNhan, Short mucHuong, String nhomDoiTuong, String noiDangKy, LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.sobhyt = sobhyt;
        this.benhNhan = benhNhan;
        if (benhNhan != null) this.idBn = benhNhan.getIdBn();
        this.mucHuong = mucHuong;
        this.nhomDoiTuong = nhomDoiTuong;
        this.noiDangKy = noiDangKy;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getSobhyt() { return sobhyt; }
    public void setSobhyt(String sobhyt) { this.sobhyt = sobhyt; }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public BenhNhan getBenhNhan() { return benhNhan; }
    public void setBenhNhan(BenhNhan benhNhan) {
        this.benhNhan = benhNhan;
        if (benhNhan != null) this.idBn = benhNhan.getIdBn();
    }

    public Short getMucHuong() { return mucHuong; }
    public void setMucHuong(Short mucHuong) { this.mucHuong = mucHuong; }
    public void setMucHuong(Integer mucHuong) { this.mucHuong = mucHuong != null ? mucHuong.shortValue() : null; }

    public String getNhomDoiTuong() { return nhomDoiTuong; }
    public void setNhomDoiTuong(String nhomDoiTuong) { this.nhomDoiTuong = nhomDoiTuong; }

    public String getNoiDangKy() { return noiDangKy; }
    public void setNoiDangKy(String noiDangKy) { this.noiDangKy = noiDangKy; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
}

package com.hms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lothuoc", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_thuoc", "so_lo"})
})
public class LoThuoc {

    @Id
    @Column(name = "id_lo", length = 10, nullable = false)
    private String idLo;

    @Column(name = "id_thuoc", length = 10, insertable = false, updatable = false)
    private String idThuoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_thuoc", nullable = false)
    private Thuoc thuoc;

    @Column(name = "so_lo", length = 30)
    private String soLo;

    @Column(name = "han_su_dung")
    private LocalDate hanSuDung;

    @Column(name = "so_luong_nhap")
    private Integer soLuongNhap;

    @Column(name = "so_luong_con")
    private Integer soLuongCon;

    @Column(name = "gia_nhap", precision = 12, scale = 2)
    private BigDecimal giaNhap;

    @Column(name = "gia_ban", precision = 12, scale = 2)
    private BigDecimal giaBan;

    @Transient
    private LocalDate ngaySanXuat;

    @Transient
    private LocalDate ngayNhap;

    public LoThuoc() {}

    public LoThuoc(String idLo, Thuoc thuoc, String soLo, LocalDate hanSuDung, Integer soLuongNhap, Integer soLuongCon, BigDecimal giaNhap, BigDecimal giaBan) {
        this.idLo = idLo;
        this.thuoc = thuoc;
        if (thuoc != null) this.idThuoc = thuoc.getIdThuoc();
        this.soLo = soLo;
        this.hanSuDung = hanSuDung;
        this.soLuongNhap = soLuongNhap;
        this.soLuongCon = soLuongCon;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
    }

    public String getIdLo() { return idLo; }
    public void setIdLo(String idLo) { this.idLo = idLo; }

    public String getIdThuoc() { return idThuoc; }
    public void setIdThuoc(String idThuoc) { this.idThuoc = idThuoc; }

    public Thuoc getThuoc() { return thuoc; }
    public void setThuoc(Thuoc thuoc) {
        this.thuoc = thuoc;
        if (thuoc != null) this.idThuoc = thuoc.getIdThuoc();
    }

    public String getSoLo() { return soLo; }
    public void setSoLo(String soLo) { this.soLo = soLo; }

    public LocalDate getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(LocalDate hanSuDung) { this.hanSuDung = hanSuDung; }

    public Integer getSoLuongNhap() { return soLuongNhap; }
    public void setSoLuongNhap(Integer soLuongNhap) { this.soLuongNhap = soLuongNhap; }

    public Integer getSoLuongCon() { return soLuongCon; }
    public void setSoLuongCon(Integer soLuongCon) { this.soLuongCon = soLuongCon; }

    public Integer getSoLuongConLai() { return soLuongCon; }
    public void setSoLuongConLai(Integer soLuongConLai) { this.soLuongCon = soLuongConLai; }

    public LocalDate getNgaySanXuat() { return ngaySanXuat; }
    public void setNgaySanXuat(LocalDate ngaySanXuat) { this.ngaySanXuat = ngaySanXuat; }

    public LocalDate getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(LocalDate ngayNhap) { this.ngayNhap = ngayNhap; }

    public BigDecimal getGiaNhap() { return giaNhap; }
    public void setGiaNhap(BigDecimal giaNhap) { this.giaNhap = giaNhap; }

    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }
}

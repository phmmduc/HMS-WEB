package com.hms.entity;

import com.hms.entity.id.ChiTietDonThuocId;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chitietdonthuoc")
public class ChiTietDonThuoc {

    @EmbeddedId
    private ChiTietDonThuocId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idDt")
    @JoinColumn(name = "id_dt", nullable = false)
    private DonThuoc donThuoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idThuoc")
    @JoinColumn(name = "id_thuoc", nullable = false)
    private Thuoc thuoc;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "lieu_dung", length = 100)
    private String lieuDung;

    @Column(name = "cach_dung", length = 255)
    private String cachDung;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "don_gia", precision = 12, scale = 2)
    private BigDecimal donGia;

    public ChiTietDonThuoc() {
        this.id = new ChiTietDonThuocId();
    }

    public ChiTietDonThuoc(DonThuoc donThuoc, Thuoc thuoc, Integer soLuong, String lieuDung, String cachDung, String ghiChu, BigDecimal donGia) {
        this.donThuoc = donThuoc;
        this.thuoc = thuoc;
        this.id = new ChiTietDonThuocId(
                donThuoc != null ? donThuoc.getIdDt() : null,
                thuoc != null ? thuoc.getIdThuoc() : null
        );
        this.soLuong = soLuong;
        this.lieuDung = lieuDung;
        this.cachDung = cachDung;
        this.ghiChu = ghiChu;
        this.donGia = donGia;
    }

    public ChiTietDonThuocId getId() { return id; }
    public void setId(ChiTietDonThuocId id) { this.id = id; }

    public DonThuoc getDonThuoc() { return donThuoc; }
    public void setDonThuoc(DonThuoc donThuoc) {
        this.donThuoc = donThuoc;
        if (this.id == null) this.id = new ChiTietDonThuocId();
        if (donThuoc != null) this.id.setIdDt(donThuoc.getIdDt());
    }

    public Thuoc getThuoc() { return thuoc; }
    public void setThuoc(Thuoc thuoc) {
        this.thuoc = thuoc;
        if (this.id == null) this.id = new ChiTietDonThuocId();
        if (thuoc != null) this.id.setIdThuoc(thuoc.getIdThuoc());
    }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public String getLieuDung() { return lieuDung; }
    public void setLieuDung(String lieuDung) { this.lieuDung = lieuDung; }

    public String getCachDung() { return cachDung; }
    public void setCachDung(String cachDung) { this.cachDung = cachDung; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
}


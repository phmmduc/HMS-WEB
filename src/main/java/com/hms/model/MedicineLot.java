package com.hms.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MedicineLot {
    private String idLo;
    private String idThuoc;
    private String soLo;
    private String hanSuDung;
    private Integer soLuongNhap;
    private Integer soLuongTon;
    private Long giaNhap;
    private Long giaBan;

    // Joined
    private String tenThuoc;
    private String donVi;

    public MedicineLot() {}

    public MedicineLot(String idLo, String idThuoc, String soLo, String hanSuDung, Integer soLuongNhap, Integer soLuongTon, Long giaNhap, Long giaBan) {
        this.idLo = idLo;
        this.idThuoc = idThuoc;
        this.soLo = soLo;
        this.hanSuDung = hanSuDung;
        this.soLuongNhap = soLuongNhap;
        this.soLuongTon = soLuongTon;
        this.giaNhap = giaNhap;
        this.giaBan = giaBan;
    }

    public String getIdLo() { return idLo; }
    public void setIdLo(String idLo) { this.idLo = idLo; }

    public String getIdThuoc() { return idThuoc; }
    public void setIdThuoc(String idThuoc) { this.idThuoc = idThuoc; }

    public String getSoLo() { return soLo; }
    public void setSoLo(String soLo) { this.soLo = soLo; }

    public String getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(String hanSuDung) { this.hanSuDung = hanSuDung; }

    public Integer getSoLuongNhap() { return soLuongNhap; }
    public void setSoLuongNhap(Integer soLuongNhap) { this.soLuongNhap = soLuongNhap; }

    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }

    public Long getGiaNhap() { return giaNhap; }
    public void setGiaNhap(Long giaNhap) { this.giaNhap = giaNhap; }

    public Long getGiaBan() { return giaBan; }
    public void setGiaBan(Long giaBan) { this.giaBan = giaBan; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    public boolean isExpired() {
        if (hanSuDung == null || hanSuDung.isEmpty()) return false;
        try {
            return LocalDate.parse(hanSuDung).isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExpiringSoon() {
        if (hanSuDung == null || hanSuDung.isEmpty()) return false;
        try {
            LocalDate exp = LocalDate.parse(hanSuDung);
            long days = ChronoUnit.DAYS.between(LocalDate.now(), exp);
            return days >= 0 && days <= 60;
        } catch (Exception e) {
            return false;
        }
    }

    public String getExpiryBadgeClass() {
        if (isExpired()) return "pill-danger";
        if (isExpiringSoon()) return "pill-warning";
        return "pill-success";
    }

    public String getExpiryStatusText() {
        if (isExpired()) return "Hết hạn";
        if (isExpiringSoon()) return "Sắp hết hạn";
        return "Còn hạn";
    }
}


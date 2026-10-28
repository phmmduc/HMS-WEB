package com.hms.model;

public class Medicine {
    private String idThuoc;
    private String tenThuoc;
    private String nhaSanXuat;
    private Boolean canKeDon;
    private Integer soLuongTonKho;
    private String donVi; // "viên", "gói", "chai", v.v.

    public Medicine() {}

    public Medicine(String idThuoc, String tenThuoc, String nhaSanXuat, Boolean canKeDon, Integer soLuongTonKho, String donVi) {
        this.idThuoc = idThuoc;
        this.tenThuoc = tenThuoc;
        this.nhaSanXuat = nhaSanXuat;
        this.canKeDon = canKeDon;
        this.soLuongTonKho = soLuongTonKho;
        this.donVi = donVi;
    }

    public String getIdThuoc() { return idThuoc; }
    public void setIdThuoc(String idThuoc) { this.idThuoc = idThuoc; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public String getNhaSanXuat() { return nhaSanXuat; }
    public void setNhaSanXuat(String nhaSanXuat) { this.nhaSanXuat = nhaSanXuat; }

    public Boolean getCanKeDon() { return canKeDon; }
    public void setCanKeDon(Boolean canKeDon) { this.canKeDon = canKeDon; }

    public Integer getSoLuongTonKho() { return soLuongTonKho; }
    public void setSoLuongTonKho(Integer soLuongTonKho) { this.soLuongTonKho = soLuongTonKho; }

    public String getDonVi() { return donVi; }
    public void setDonVi(String donVi) { this.donVi = donVi; }

    public String getInventoryBadgeClass() {
        if (soLuongTonKho == null || soLuongTonKho < 30) return "pill-danger";
        if (soLuongTonKho < 50) return "pill-warning";
        return "pill-success";
    }

    public String getInventoryStatusText() {
        if (soLuongTonKho == null || soLuongTonKho < 30) return "Sắp hết";
        if (soLuongTonKho < 50) return "Cần bổ sung";
        return "Bình thường";
    }
}


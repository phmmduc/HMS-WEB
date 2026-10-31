package com.hms.dto;

public class StaffFormDto {
    private String idNv;
    private String hoTen;
    private String gioiTinh;
    private String ngaySinh;
    private String phone;
    private String email;
    private String vaiTro;
    private String trangThai = "DANG_LAM";
    private String specialtyDetail;
    private String idPk;

    public StaffFormDto() {}

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getSpecialtyDetail() { return specialtyDetail; }
    public void setSpecialtyDetail(String specialtyDetail) { this.specialtyDetail = specialtyDetail; }

    public String getIdPk() { return idPk; }
    public void setIdPk(String idPk) { this.idPk = idPk; }
}


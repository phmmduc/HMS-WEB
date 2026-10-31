package com.hms.model;

public class Invoice {
    private String idHoadon;
    private String idPhieukham;
    private String idBn;
    private String idNv;
    private Long soTien;
    private String loaiThu;
    private String phuongThucTt; // "Tiền mặt", "Chuyển khoản", "Thẻ", "Bảo hiểm"
    private String thoiGianThu;
    private String trangThai; // "Đã thanh toán", "Chưa thanh toán"

    // Joined fields
    private String patientName;
    private String patientPhone;
    private Long tienKham;
    private Long benhNhanTra;

    public Invoice() {}

    public Invoice(String idHoadon, String idPhieukham, Long soTien, String loaiThu, String phuongThucTt, String thoiGianThu, String trangThai) {
        this.idHoadon = idHoadon;
        this.idPhieukham = idPhieukham;
        this.soTien = soTien;
        this.loaiThu = loaiThu;
        this.phuongThucTt = phuongThucTt;
        this.thoiGianThu = thoiGianThu;
        this.trangThai = trangThai;
    }

    public String getIdHoadon() { return idHoadon; }
    public void setIdHoadon(String idHoadon) { this.idHoadon = idHoadon; }

    public String getIdPhieukham() { return idPhieukham; }
    public void setIdPhieukham(String idPhieukham) { this.idPhieukham = idPhieukham; }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public Long getSoTien() { return soTien; }
    public void setSoTien(Long soTien) { this.soTien = soTien; }

    public String getLoaiThu() { return loaiThu; }
    public void setLoaiThu(String loaiThu) { this.loaiThu = loaiThu; }

    public String getPhuongThucTt() { return phuongThucTt; }
    public void setPhuongThucTt(String phuongThucTt) { this.phuongThucTt = phuongThucTt; }

    public String getThoiGianThu() { return thoiGianThu; }
    public void setThoiGianThu(String thoiGianThu) { this.thoiGianThu = thoiGianThu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public Long getTienKham() { return tienKham; }
    public void setTienKham(Long tienKham) { this.tienKham = tienKham; }

    public Long getBenhNhanTra() { return benhNhanTra; }
    public void setBenhNhanTra(Long benhNhanTra) { this.benhNhanTra = benhNhanTra; }

    public boolean isPaid() {
        return "Đã thanh toán".equalsIgnoreCase(trangThai);
    }

    public String getEffectiveDate() {
        if (thoiGianThu != null && !thoiGianThu.isBlank()) {
            return thoiGianThu.trim();
        }
        return java.time.LocalDate.now().toString();
    }
}


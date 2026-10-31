package com.hms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "phieuthuvienphi")
public class PhieuThuVienPhi {

    @Id
    @Column(name = "id_hoadon", length = 15, nullable = false)
    private String idHoadon;

    @Column(name = "id_phieukham", length = 15, insertable = false, updatable = false)
    private String idPhieukham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieukham", nullable = false)
    private PhieuKham phieuKham;

    @Column(name = "id_nv", length = 10, insertable = false, updatable = false)
    private String idNv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nv", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "so_tien", precision = 12, scale = 2)
    private BigDecimal soTien;

    @Column(name = "loai_thu", length = 20)
    private String loaiThu; // TAM_UNG, THANH_TOAN, HOAN_TIEN

    @Column(name = "phuong_thuc_tt", length = 20)
    private String phuongThucTt; // TIEN_MAT, CHUYEN_KHOAN, THE

    @Column(name = "thoi_gian_thu")
    private LocalDateTime thoiGianThu;

    @Column(name = "trang_thai", length = 20)
    private String trangThai; // DA_THU, HUY

    public PhieuThuVienPhi() {}

    public PhieuThuVienPhi(String idHoadon, PhieuKham phieuKham, NhanVien nhanVien, BigDecimal soTien, String loaiThu, String phuongThucTt, LocalDateTime thoiGianThu, String trangThai) {
        this.idHoadon = idHoadon;
        this.phieuKham = phieuKham;
        if (phieuKham != null) this.idPhieukham = phieuKham.getIdPhieukham();
        this.nhanVien = nhanVien;
        if (nhanVien != null) this.idNv = nhanVien.getIdNv();
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

    public PhieuKham getPhieuKham() { return phieuKham; }
    public void setPhieuKham(PhieuKham phieuKham) {
        this.phieuKham = phieuKham;
        if (phieuKham != null) this.idPhieukham = phieuKham.getIdPhieukham();
    }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
        if (nhanVien != null) this.idNv = nhanVien.getIdNv();
    }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public String getLoaiThu() { return loaiThu; }
    public void setLoaiThu(String loaiThu) { this.loaiThu = loaiThu; }

    public String getPhuongThucTt() { return phuongThucTt; }
    public void setPhuongThucTt(String phuongThucTt) { this.phuongThucTt = phuongThucTt; }

    public LocalDateTime getThoiGianThu() { return thoiGianThu; }
    public void setThoiGianThu(LocalDateTime thoiGianThu) { this.thoiGianThu = thoiGianThu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}


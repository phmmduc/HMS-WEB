package com.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nhanvien")
public class NhanVien {

    @Id
    @Column(name = "id_nv", length = 10, nullable = false)
    private String idNv;

    @Column(name = "ho_ten", length = 100)
    private String hoTen;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "vai_tro", length = 20)
    private String vaiTro;

    @Column(name = "trang_thai", length = 20)
    private String trangThai; // DANG_LAM, NGHI_VIEC, TAM_NGHI

    @Transient
    private String email;

    @OneToMany(mappedBy = "nhanVien", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NhanVienSdt> danhSachSdt = new ArrayList<>();

    @Transient
    private BacSy bacSy;

    @Transient
    private YTa yTa;

    @Transient
    private DuocSi duocSi;

    public NhanVien() {}

    public NhanVien(String idNv, String hoTen, String gioiTinh, LocalDate ngaySinh, String vaiTro, String trangThai) {
        this.idNv = idNv;
        this.hoTen = hoTen;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
    }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getEmail() { return email != null ? email : (idNv + "@hms.local"); }
    public void setEmail(String email) { this.email = email; }

    public List<NhanVienSdt> getDanhSachSdt() { return danhSachSdt; }
    public void setDanhSachSdt(List<NhanVienSdt> danhSachSdt) { this.danhSachSdt = danhSachSdt; }

    public BacSy getBacSy() { return bacSy; }
    public void setBacSy(BacSy bacSy) { this.bacSy = bacSy; }

    public YTa getYTa() { return yTa; }
    public void setYTa(YTa yTa) { this.yTa = yTa; }

    public DuocSi getDuocSi() { return duocSi; }
    public void setDuocSi(DuocSi duocSi) { this.duocSi = duocSi; }

    public void addSdt(String sdt) {
        if (sdt != null && !sdt.isBlank()) {
            this.danhSachSdt.add(new NhanVienSdt(this, sdt.trim()));
        }
    }
}

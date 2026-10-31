package com.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "benhnhan")
public class BenhNhan {

    @Id
    @Column(name = "id_bn", length = 10, nullable = false)
    private String idBn;

    @Column(name = "ho_ten", length = 100, nullable = false)
    private String hoTen;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "cccd", length = 12)
    private String cccd;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Transient
    private String diaChi;

    @Transient
    private LocalDate ngayTao;

    @Transient
    private String trangThai = "Ổn định";

    @OneToMany(mappedBy = "benhNhan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BenhNhanSdt> danhSachSdt = new ArrayList<>();

    @OneToMany(mappedBy = "benhNhan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NguoiThan> danhSachNguoiThan = new ArrayList<>();

    @OneToMany(mappedBy = "benhNhan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BaoHiemYTe> danhSachBhyt = new ArrayList<>();

    @OneToMany(mappedBy = "benhNhan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BenhAn> danhSachBenhAn = new ArrayList<>();

    public BenhNhan() {}

    public BenhNhan(String idBn, String hoTen, LocalDate ngaySinh, String cccd, String gioiTinh) {
        this.idBn = idBn;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.cccd = cccd;
        this.gioiTinh = gioiTinh;
    }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public LocalDate getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDate ngayTao) { this.ngayTao = ngayTao; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public List<BenhNhanSdt> getDanhSachSdt() { return danhSachSdt; }
    public void setDanhSachSdt(List<BenhNhanSdt> danhSachSdt) { this.danhSachSdt = danhSachSdt; }

    public List<NguoiThan> getDanhSachNguoiThan() { return danhSachNguoiThan; }
    public void setDanhSachNguoiThan(List<NguoiThan> danhSachNguoiThan) { this.danhSachNguoiThan = danhSachNguoiThan; }

    public List<BaoHiemYTe> getDanhSachBhyt() { return danhSachBhyt; }
    public void setDanhSachBhyt(List<BaoHiemYTe> danhSachBhyt) { this.danhSachBhyt = danhSachBhyt; }

    public List<BenhAn> getDanhSachBenhAn() { return danhSachBenhAn; }
    public void setDanhSachBenhAn(List<BenhAn> danhSachBenhAn) { this.danhSachBenhAn = danhSachBenhAn; }

    public void addSdt(String sdt) {
        if (sdt != null && !sdt.isBlank()) {
            BenhNhanSdt entity = new BenhNhanSdt(this, sdt.trim());
            this.danhSachSdt.add(entity);
        }
    }
}

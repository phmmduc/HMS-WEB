package com.hms.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "thuoc")
public class Thuoc {

    @Id
    @Column(name = "id_thuoc", length = 10, nullable = false)
    private String idThuoc;

    @Column(name = "ten_thuoc", length = 150)
    private String tenThuoc;

    @Column(name = "nha_san_xuat", length = 150)
    private String nhaSanXuat;

    @Column(name = "can_ke_don")
    private Boolean canKeDon;

    @Column(name = "so_luong_ton_kho")
    private Integer soLuongTonKho;

    @Transient
    private String donVi = "Viên";

    @Transient
    private String hamLuong;

    @Transient
    private String hoatChat;

    @Transient
    private String danhMuc;

    @Transient
    private String trangThai = "Hoạt động";

    @OneToMany(mappedBy = "thuoc", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoThuoc> danhSachLoThuoc = new ArrayList<>();

    public Thuoc() {}

    public Thuoc(String idThuoc, String tenThuoc, String nhaSanXuat, Boolean canKeDon, Integer soLuongTonKho) {
        this.idThuoc = idThuoc;
        this.tenThuoc = tenThuoc;
        this.nhaSanXuat = nhaSanXuat;
        this.canKeDon = canKeDon;
        this.soLuongTonKho = soLuongTonKho;
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

    public String getHamLuong() { return hamLuong; }
    public void setHamLuong(String hamLuong) { this.hamLuong = hamLuong; }

    public String getHoatChat() { return hoatChat; }
    public void setHoatChat(String hoatChat) { this.hoatChat = hoatChat; }

    public String getDanhMuc() { return danhMuc; }
    public void setDanhMuc(String danhMuc) { this.danhMuc = danhMuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public List<LoThuoc> getDanhSachLoThuoc() { return danhSachLoThuoc; }
    public void setDanhSachLoThuoc(List<LoThuoc> danhSachLoThuoc) { this.danhSachLoThuoc = danhSachLoThuoc; }
}

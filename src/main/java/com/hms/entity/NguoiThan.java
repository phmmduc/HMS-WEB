package com.hms.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nguoithan")
public class NguoiThan {

    @Id
    @Column(name = "id_nt", length = 10, nullable = false)
    private String idNt;

    @Column(name = "id_bn", length = 10, insertable = false, updatable = false)
    private String idBn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bn", nullable = false)
    private BenhNhan benhNhan;

    @Column(name = "ho_ten", length = 100)
    private String hoTen;

    @Column(name = "moi_quan_he", length = 50)
    private String moiQuanHe;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @OneToMany(mappedBy = "nguoiThan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NguoiThanSdt> danhSachSdt = new ArrayList<>();

    public NguoiThan() {}

    public NguoiThan(String idNt, BenhNhan benhNhan, String hoTen, String moiQuanHe, String gioiTinh) {
        this.idNt = idNt;
        this.benhNhan = benhNhan;
        if (benhNhan != null) this.idBn = benhNhan.getIdBn();
        this.hoTen = hoTen;
        this.moiQuanHe = moiQuanHe;
        this.gioiTinh = gioiTinh;
    }

    public String getIdNt() { return idNt; }
    public void setIdNt(String idNt) { this.idNt = idNt; }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public BenhNhan getBenhNhan() { return benhNhan; }
    public void setBenhNhan(BenhNhan benhNhan) {
        this.benhNhan = benhNhan;
        if (benhNhan != null) this.idBn = benhNhan.getIdBn();
    }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getMoiQuanHe() { return moiQuanHe; }
    public void setMoiQuanHe(String moiQuanHe) { this.moiQuanHe = moiQuanHe; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public List<NguoiThanSdt> getDanhSachSdt() { return danhSachSdt; }
    public void setDanhSachSdt(List<NguoiThanSdt> danhSachSdt) { this.danhSachSdt = danhSachSdt; }

    public void addSdt(String sdt) {
        if (sdt != null && !sdt.isBlank()) {
            this.danhSachSdt.add(new NguoiThanSdt(this, sdt.trim()));
        }
    }
}


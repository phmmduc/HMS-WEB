package com.hms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "yta")
public class YTa {

    @Id
    @Column(name = "id_nv", length = 10, nullable = false)
    private String idNv;

    @Transient
    private NhanVien nhanVien;

    @Column(name = "cap_bac", length = 50)
    private String capBac;

    public YTa() {}

    public YTa(NhanVien nhanVien, String capBac) {
        this.nhanVien = nhanVien;
        if (nhanVien != null) this.idNv = nhanVien.getIdNv();
        this.capBac = capBac;
    }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getIdYta() { return idNv; }
    public void setIdYta(String idYta) { this.idNv = idYta; }

    public String getChungChiHanhNghe() { return capBac; }
    public void setChungChiHanhNghe(String cchn) { this.capBac = cchn; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
        if (nhanVien != null) this.idNv = nhanVien.getIdNv();
    }

    public String getCapBac() { return capBac; }
    public void setCapBac(String capBac) { this.capBac = capBac; }
}

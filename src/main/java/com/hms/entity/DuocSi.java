package com.hms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "duocsi")
public class DuocSi {

    @Id
    @Column(name = "id_nv", length = 10, nullable = false)
    private String idNv;

    @Transient
    private NhanVien nhanVien;

    @Column(name = "bang_cap", length = 100)
    private String bangCap;

    public DuocSi() {}

    public DuocSi(String idNv, String bangCap) {
        this.idNv = idNv;
        this.bangCap = bangCap;
    }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getIdDuocsi() { return idNv; }
    public void setIdDuocsi(String idDuocsi) { this.idNv = idDuocsi; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
        if (nhanVien != null) this.idNv = nhanVien.getIdNv();
    }

    public String getBangCap() { return bangCap; }
    public void setBangCap(String bangCap) { this.bangCap = bangCap; }

    public String getTrinhDo() { return bangCap; }
    public void setTrinhDo(String trinhDo) { this.bangCap = trinhDo; }
}

package com.hms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bacsy")
public class BacSy {

    @Id
    @Column(name = "id_nv", length = 10, nullable = false)
    private String idNv;

    @Transient
    private NhanVien nhanVien;

    @Column(name = "id_ck", length = 10, insertable = false, updatable = false)
    private String idCk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ck")
    private ChuyenKhoa chuyenKhoa;

    @Column(name = "hoc_vi", length = 50)
    private String hocVi;

    @Column(name = "so_cchn", length = 30, nullable = false, unique = true)
    private String soCchn;

    @Transient
    private String idPk;

    public BacSy() {}

    public BacSy(NhanVien nhanVien, ChuyenKhoa chuyenKhoa, String hocVi, String soCchn) {
        this.nhanVien = nhanVien;
        if (nhanVien != null) this.idNv = nhanVien.getIdNv();
        this.chuyenKhoa = chuyenKhoa;
        if (chuyenKhoa != null) this.idCk = chuyenKhoa.getIdCk();
        this.hocVi = hocVi;
        this.soCchn = soCchn;
    }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getIdBacSy() { return idNv; }
    public void setIdBacSy(String idBacSy) { this.idNv = idBacSy; }

    public String getTrinhDo() { return hocVi; }
    public void setTrinhDo(String trinhDo) { this.hocVi = trinhDo; }

    public String getIdPk() { return idPk; }
    public void setIdPk(String idPk) { this.idPk = idPk; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
        if (nhanVien != null) this.idNv = nhanVien.getIdNv();
    }

    public String getIdCk() { return idCk; }
    public void setIdCk(String idCk) { this.idCk = idCk; }

    public ChuyenKhoa getChuyenKhoa() { return chuyenKhoa; }
    public void setChuyenKhoa(ChuyenKhoa chuyenKhoa) {
        this.chuyenKhoa = chuyenKhoa;
        if (chuyenKhoa != null) this.idCk = chuyenKhoa.getIdCk();
    }

    public String getHocVi() { return hocVi; }
    public void setHocVi(String hocVi) { this.hocVi = hocVi; }

    public String getSoCchn() { return soCchn; }
    public void setSoCchn(String soCchn) { this.soCchn = soCchn; }
}

package com.hms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "phongkham")
public class PhongKham {

    @Id
    @Column(name = "id_pk", length = 10, nullable = false)
    private String idPk;

    @Column(name = "id_ck", length = 10, insertable = false, updatable = false)
    private String idCk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ck")
    private ChuyenKhoa chuyenKhoa;

    @Column(name = "ten_phong", length = 100)
    private String tenPhong;

    @Column(name = "so_phong", length = 10, unique = true)
    private String soPhong;

    @Column(name = "trang_thai", length = 20)
    private String trangThai; // HOAT_DONG, TAM_NGUNG, BAO_TRI

    public PhongKham() {}

    public PhongKham(String idPk, ChuyenKhoa chuyenKhoa, String tenPhong, String soPhong, String trangThai) {
        this.idPk = idPk;
        this.chuyenKhoa = chuyenKhoa;
        if (chuyenKhoa != null) this.idCk = chuyenKhoa.getIdCk();
        this.tenPhong = tenPhong;
        this.soPhong = soPhong;
        this.trangThai = trangThai;
    }

    public String getIdPk() { return idPk; }
    public void setIdPk(String idPk) { this.idPk = idPk; }

    public String getIdCk() { return idCk; }
    public void setIdCk(String idCk) { this.idCk = idCk; }

    public ChuyenKhoa getChuyenKhoa() { return chuyenKhoa; }
    public void setChuyenKhoa(ChuyenKhoa chuyenKhoa) {
        this.chuyenKhoa = chuyenKhoa;
        if (chuyenKhoa != null) this.idCk = chuyenKhoa.getIdCk();
    }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public String getTenPk() { return tenPhong; }
    public void setTenPk(String tenPk) { this.tenPhong = tenPk; }

    public String getSoPhong() { return soPhong; }
    public void setSoPhong(String soPhong) { this.soPhong = soPhong; }

    public String getViTri() { return soPhong; }
    public void setViTri(String viTri) { this.soPhong = viTri; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

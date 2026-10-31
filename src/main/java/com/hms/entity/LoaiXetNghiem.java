package com.hms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "loaixetnghiem")
public class LoaiXetNghiem {

    @Id
    @Column(name = "id_lxn", length = 10, nullable = false)
    private String idLxn;

    @Column(name = "ten_lxn", length = 100)
    private String tenLxn;

    @Column(name = "chi_phi", precision = 12, scale = 2)
    private BigDecimal chiPhi;

    @Column(name = "trang_thai", length = 20)
    private String trangThai; // HOAT_DONG, NGUNG

    public LoaiXetNghiem() {}

    public LoaiXetNghiem(String idLxn, String tenLxn, BigDecimal chiPhi, String trangThai) {
        this.idLxn = idLxn;
        this.tenLxn = tenLxn;
        this.chiPhi = chiPhi;
        this.trangThai = trangThai;
    }

    public String getIdLxn() { return idLxn; }
    public void setIdLxn(String idLxn) { this.idLxn = idLxn; }

    public String getIdLoaiXn() { return idLxn; }
    public void setIdLoaiXn(String idLoaiXn) { this.idLxn = idLoaiXn; }

    public String getTenLxn() { return tenLxn; }
    public void setTenLxn(String tenLxn) { this.tenLxn = tenLxn; }

    public String getTenLoai() { return tenLxn; }
    public void setTenLoai(String tenLoai) { this.tenLxn = tenLoai; }

    public BigDecimal getChiPhi() { return chiPhi; }
    public void setChiPhi(BigDecimal chiPhi) { this.chiPhi = chiPhi; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}

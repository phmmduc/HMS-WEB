package com.hms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;

@Entity
@Table(name = "v_donthuoc_tongtien")
@Immutable
public class VDonThuocTongTien {

    @Id
    @Column(name = "id_dt", length = 15)
    private String idDt;

    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    public VDonThuocTongTien() {}

    public String getIdDt() { return idDt; }
    public void setIdDt(String idDt) { this.idDt = idDt; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
}


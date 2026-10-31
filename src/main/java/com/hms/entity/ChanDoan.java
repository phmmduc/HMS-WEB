package com.hms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chandoan")
public class ChanDoan {

    @Id
    @Column(name = "id_icd", length = 10, nullable = false)
    private String idIcd;

    @Column(name = "ten_benh", length = 255)
    private String tenBenh;

    public ChanDoan() {}

    public ChanDoan(String idIcd, String tenBenh) {
        this.idIcd = idIcd;
        this.tenBenh = tenBenh;
    }

    public String getIdIcd() { return idIcd; }
    public void setIdIcd(String idIcd) { this.idIcd = idIcd; }

    public String getIdCd() { return idIcd; }
    public void setIdCd(String idCd) { this.idIcd = idCd; }

    public String getMaIcd() { return idIcd; }
    public void setMaIcd(String maIcd) { this.idIcd = maIcd; }

    public String getTenBenh() { return tenBenh; }
    public void setTenBenh(String tenBenh) { this.tenBenh = tenBenh; }
}

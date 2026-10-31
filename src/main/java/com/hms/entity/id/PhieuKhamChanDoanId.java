package com.hms.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PhieuKhamChanDoanId implements Serializable {

    @Column(name = "id_phieukham", length = 15)
    private String idPhieukham;

    @Column(name = "id_icd", length = 10)
    private String idIcd;

    public PhieuKhamChanDoanId() {}

    public PhieuKhamChanDoanId(String idPhieukham, String idIcd) {
        this.idPhieukham = idPhieukham;
        this.idIcd = idIcd;
    }

    public String getIdPhieukham() { return idPhieukham; }
    public void setIdPhieukham(String idPhieukham) { this.idPhieukham = idPhieukham; }

    public String getIdIcd() { return idIcd; }
    public void setIdIcd(String idIcd) { this.idIcd = idIcd; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhieuKhamChanDoanId that)) return false;
        return Objects.equals(idPhieukham, that.idPhieukham) && Objects.equals(idIcd, that.idIcd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPhieukham, idIcd);
    }
}


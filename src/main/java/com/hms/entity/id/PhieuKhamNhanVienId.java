package com.hms.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PhieuKhamNhanVienId implements Serializable {

    @Column(name = "id_phieukham", length = 15)
    private String idPhieukham;

    @Column(name = "id_nv", length = 10)
    private String idNv;

    public PhieuKhamNhanVienId() {}

    public PhieuKhamNhanVienId(String idPhieukham, String idNv) {
        this.idPhieukham = idPhieukham;
        this.idNv = idNv;
    }

    public String getIdPhieukham() { return idPhieukham; }
    public void setIdPhieukham(String idPhieukham) { this.idPhieukham = idPhieukham; }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhieuKhamNhanVienId that)) return false;
        return Objects.equals(idPhieukham, that.idPhieukham) && Objects.equals(idNv, that.idNv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPhieukham, idNv);
    }
}


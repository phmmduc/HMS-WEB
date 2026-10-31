package com.hms.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BenhNhanSdtId implements Serializable {

    @Column(name = "id_bn", length = 10)
    private String idBn;

    @Column(name = "sdt", length = 15)
    private String sdt;

    public BenhNhanSdtId() {}

    public BenhNhanSdtId(String idBn, String sdt) {
        this.idBn = idBn;
        this.sdt = sdt;
    }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BenhNhanSdtId that)) return false;
        return Objects.equals(idBn, that.idBn) && Objects.equals(sdt, that.sdt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idBn, sdt);
    }
}


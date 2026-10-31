package com.hms.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class NguoiThanSdtId implements Serializable {

    @Column(name = "id_nt", length = 10)
    private String idNt;

    @Column(name = "sdt", length = 15)
    private String sdt;

    public NguoiThanSdtId() {}

    public NguoiThanSdtId(String idNt, String sdt) {
        this.idNt = idNt;
        this.sdt = sdt;
    }

    public String getIdNt() { return idNt; }
    public void setIdNt(String idNt) { this.idNt = idNt; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NguoiThanSdtId that)) return false;
        return Objects.equals(idNt, that.idNt) && Objects.equals(sdt, that.sdt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNt, sdt);
    }
}


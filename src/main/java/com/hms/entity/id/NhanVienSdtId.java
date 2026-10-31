package com.hms.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class NhanVienSdtId implements Serializable {

    @Column(name = "id_nv", length = 10)
    private String idNv;

    @Column(name = "sdt", length = 15)
    private String sdt;

    public NhanVienSdtId() {}

    public NhanVienSdtId(String idNv, String sdt) {
        this.idNv = idNv;
        this.sdt = sdt;
    }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NhanVienSdtId that)) return false;
        return Objects.equals(idNv, that.idNv) && Objects.equals(sdt, that.sdt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNv, sdt);
    }
}


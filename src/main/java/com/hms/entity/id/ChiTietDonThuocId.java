package com.hms.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ChiTietDonThuocId implements Serializable {

    @Column(name = "id_dt", length = 15)
    private String idDt;

    @Column(name = "id_thuoc", length = 10)
    private String idThuoc;

    public ChiTietDonThuocId() {}

    public ChiTietDonThuocId(String idDt, String idThuoc) {
        this.idDt = idDt;
        this.idThuoc = idThuoc;
    }

    public String getIdDt() { return idDt; }
    public void setIdDt(String idDt) { this.idDt = idDt; }

    public String getIdThuoc() { return idThuoc; }
    public void setIdThuoc(String idThuoc) { this.idThuoc = idThuoc; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietDonThuocId that)) return false;
        return Objects.equals(idDt, that.idDt) && Objects.equals(idThuoc, that.idThuoc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDt, idThuoc);
    }
}


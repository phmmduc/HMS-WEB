package com.hms.entity;

import com.hms.entity.id.BenhNhanSdtId;
import jakarta.persistence.*;

@Entity
@Table(name = "benhnhan_sdt")
public class BenhNhanSdt {

    @EmbeddedId
    private BenhNhanSdtId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idBn")
    @JoinColumn(name = "id_bn", nullable = false)
    private BenhNhan benhNhan;

    public BenhNhanSdt() {
        this.id = new BenhNhanSdtId();
    }

    public BenhNhanSdt(BenhNhan benhNhan, String sdt) {
        this.benhNhan = benhNhan;
        this.id = new BenhNhanSdtId(benhNhan != null ? benhNhan.getIdBn() : null, sdt);
    }

    public BenhNhanSdt(BenhNhanSdtId id, BenhNhan benhNhan) {
        this.id = id;
        this.benhNhan = benhNhan;
    }

    public BenhNhanSdtId getId() { return id; }
    public void setId(BenhNhanSdtId id) { this.id = id; }

    public BenhNhan getBenhNhan() { return benhNhan; }
    public void setBenhNhan(BenhNhan benhNhan) {
        this.benhNhan = benhNhan;
        if (this.id == null) this.id = new BenhNhanSdtId();
        if (benhNhan != null) this.id.setIdBn(benhNhan.getIdBn());
    }

    public String getSdt() {
        return id != null ? id.getSdt() : null;
    }

    public void setSdt(String sdt) {
        if (this.id == null) this.id = new BenhNhanSdtId();
        this.id.setSdt(sdt);
    }
}

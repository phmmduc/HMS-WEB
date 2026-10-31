package com.hms.entity;

import com.hms.entity.id.NguoiThanSdtId;
import jakarta.persistence.*;

@Entity
@Table(name = "nguoithan_sdt")
public class NguoiThanSdt {

    @EmbeddedId
    private NguoiThanSdtId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idNt")
    @JoinColumn(name = "id_nt", nullable = false)
    private NguoiThan nguoiThan;

    public NguoiThanSdt() {
        this.id = new NguoiThanSdtId();
    }

    public NguoiThanSdt(NguoiThan nguoiThan, String sdt) {
        this.nguoiThan = nguoiThan;
        this.id = new NguoiThanSdtId(nguoiThan != null ? nguoiThan.getIdNt() : null, sdt);
    }

    public NguoiThanSdt(NguoiThanSdtId id, NguoiThan nguoiThan) {
        this.id = id;
        this.nguoiThan = nguoiThan;
    }

    public NguoiThanSdtId getId() { return id; }
    public void setId(NguoiThanSdtId id) { this.id = id; }

    public NguoiThan getNguoiThan() { return nguoiThan; }
    public void setNguoiThan(NguoiThan nguoiThan) {
        this.nguoiThan = nguoiThan;
        if (this.id == null) this.id = new NguoiThanSdtId();
        if (nguoiThan != null) this.id.setIdNt(nguoiThan.getIdNt());
    }

    public String getSdt() {
        return id != null ? id.getSdt() : null;
    }

    public void setSdt(String sdt) {
        if (this.id == null) this.id = new NguoiThanSdtId();
        this.id.setSdt(sdt);
    }
}

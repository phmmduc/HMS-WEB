package com.hms.entity;

import com.hms.entity.id.NhanVienSdtId;
import jakarta.persistence.*;

@Entity
@Table(name = "nhanvien_sdt")
public class NhanVienSdt {

    @EmbeddedId
    private NhanVienSdtId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idNv")
    @JoinColumn(name = "id_nv", nullable = false)
    private NhanVien nhanVien;

    public NhanVienSdt() {
        this.id = new NhanVienSdtId();
    }

    public NhanVienSdt(NhanVien nhanVien, String sdt) {
        this.nhanVien = nhanVien;
        this.id = new NhanVienSdtId(nhanVien != null ? nhanVien.getIdNv() : null, sdt);
    }

    public NhanVienSdt(NhanVienSdtId id, NhanVien nhanVien) {
        this.id = id;
        this.nhanVien = nhanVien;
    }

    public NhanVienSdtId getId() { return id; }
    public void setId(NhanVienSdtId id) { this.id = id; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
        if (this.id == null) this.id = new NhanVienSdtId();
        if (nhanVien != null) this.id.setIdNv(nhanVien.getIdNv());
    }

    public String getSdt() {
        return id != null ? id.getSdt() : null;
    }

    public void setSdt(String sdt) {
        if (this.id == null) this.id = new NhanVienSdtId();
        this.id.setSdt(sdt);
    }
}

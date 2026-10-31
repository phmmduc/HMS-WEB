package com.hms.entity;

import com.hms.entity.id.PhieuKhamChanDoanId;
import jakarta.persistence.*;

@Entity
@Table(name = "phieukham_chandoan")
public class PhieuKhamChanDoan {

    @EmbeddedId
    private PhieuKhamChanDoanId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPhieukham")
    @JoinColumn(name = "id_phieukham", nullable = false)
    private PhieuKham phieuKham;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idIcd")
    @JoinColumn(name = "id_icd", nullable = false)
    private ChanDoan chanDoan;

    @Column(name = "loai", length = 10)
    private String loai; // CHINH, PHU

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    public PhieuKhamChanDoan() {
        this.id = new PhieuKhamChanDoanId();
    }

    public PhieuKhamChanDoan(PhieuKham phieuKham, ChanDoan chanDoan, String loai, String ghiChu) {
        this.phieuKham = phieuKham;
        this.chanDoan = chanDoan;
        this.id = new PhieuKhamChanDoanId(
                phieuKham != null ? phieuKham.getIdPhieukham() : null,
                chanDoan != null ? chanDoan.getIdIcd() : null
        );
        this.loai = loai;
        this.ghiChu = ghiChu;
    }

    public PhieuKhamChanDoanId getId() { return id; }
    public void setId(PhieuKhamChanDoanId id) { this.id = id; }

    public PhieuKham getPhieuKham() { return phieuKham; }
    public void setPhieuKham(PhieuKham phieuKham) {
        this.phieuKham = phieuKham;
        if (this.id == null) this.id = new PhieuKhamChanDoanId();
        if (phieuKham != null) this.id.setIdPhieukham(phieuKham.getIdPhieukham());
    }

    public ChanDoan getChanDoan() { return chanDoan; }
    public void setChanDoan(ChanDoan chanDoan) {
        this.chanDoan = chanDoan;
        if (this.id == null) this.id = new PhieuKhamChanDoanId();
        if (chanDoan != null) this.id.setIdIcd(chanDoan.getIdIcd());
    }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}


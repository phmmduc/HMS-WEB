package com.hms.entity;

import com.hms.entity.id.PhieuKhamNhanVienId;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "phieukham_nhanvien")
public class PhieuKhamNhanVien {

    @EmbeddedId
    private PhieuKhamNhanVienId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPhieukham")
    @JoinColumn(name = "id_phieukham", nullable = false)
    private PhieuKham phieuKham;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idNv")
    @JoinColumn(name = "id_nv", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "thoi_gian_tiep_nhan")
    private LocalDateTime thoiGianTiepNhan;

    public PhieuKhamNhanVien() {
        this.id = new PhieuKhamNhanVienId();
    }

    public PhieuKhamNhanVien(PhieuKham phieuKham, NhanVien nhanVien, LocalDateTime thoiGianTiepNhan) {
        this.phieuKham = phieuKham;
        this.nhanVien = nhanVien;
        this.id = new PhieuKhamNhanVienId(
                phieuKham != null ? phieuKham.getIdPhieukham() : null,
                nhanVien != null ? nhanVien.getIdNv() : null
        );
        this.thoiGianTiepNhan = thoiGianTiepNhan;
    }

    public PhieuKhamNhanVienId getId() { return id; }
    public void setId(PhieuKhamNhanVienId id) { this.id = id; }

    public PhieuKham getPhieuKham() { return phieuKham; }
    public void setPhieuKham(PhieuKham phieuKham) {
        this.phieuKham = phieuKham;
        if (this.id == null) this.id = new PhieuKhamNhanVienId();
        if (phieuKham != null) this.id.setIdPhieukham(phieuKham.getIdPhieukham());
    }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
        if (this.id == null) this.id = new PhieuKhamNhanVienId();
        if (nhanVien != null) this.id.setIdNv(nhanVien.getIdNv());
    }

    public LocalDateTime getThoiGianTiepNhan() { return thoiGianTiepNhan; }
    public void setThoiGianTiepNhan(LocalDateTime thoiGianTiepNhan) { this.thoiGianTiepNhan = thoiGianTiepNhan; }
}


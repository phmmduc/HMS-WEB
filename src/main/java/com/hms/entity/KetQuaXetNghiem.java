package com.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ketquaxetnghiem")
public class KetQuaXetNghiem {

    @Id
    @Column(name = "id_kqxn", length = 15, nullable = false)
    private String idKqxn;

    @Column(name = "id_phieukham", length = 15, insertable = false, updatable = false)
    private String idPhieukham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieukham", nullable = false)
    private PhieuKham phieuKham;

    @Column(name = "id_lxn", length = 10, insertable = false, updatable = false)
    private String idLxn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lxn", nullable = false)
    private LoaiXetNghiem loaiXetNghiem;

    @Column(name = "ket_qua", columnDefinition = "TEXT")
    private String ketQua;

    @Column(name = "ket_luan", columnDefinition = "TEXT")
    private String ketLuan;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @Column(name = "trang_thai", length = 20)
    private String trangThai; // CHO_KET_QUA, DA_CO_KET_QUA, HUY

    public KetQuaXetNghiem() {}

    public KetQuaXetNghiem(String idKqxn, PhieuKham phieuKham, LoaiXetNghiem loaiXetNghiem, String ketQua, String ketLuan, LocalDateTime thoiGian, String trangThai) {
        this.idKqxn = idKqxn;
        this.phieuKham = phieuKham;
        if (phieuKham != null) this.idPhieukham = phieuKham.getIdPhieukham();
        this.loaiXetNghiem = loaiXetNghiem;
        if (loaiXetNghiem != null) this.idLxn = loaiXetNghiem.getIdLxn();
        this.ketQua = ketQua;
        this.ketLuan = ketLuan;
        this.thoiGian = thoiGian;
        this.trangThai = trangThai;
    }

    public String getIdKqxn() { return idKqxn; }
    public void setIdKqxn(String idKqxn) { this.idKqxn = idKqxn; }

    public String getIdPhieukham() { return idPhieukham; }
    public void setIdPhieukham(String idPhieukham) { this.idPhieukham = idPhieukham; }

    public PhieuKham getPhieuKham() { return phieuKham; }
    public void setPhieuKham(PhieuKham phieuKham) {
        this.phieuKham = phieuKham;
        if (phieuKham != null) this.idPhieukham = phieuKham.getIdPhieukham();
    }

    public String getIdLxn() { return idLxn; }
    public void setIdLxn(String idLxn) { this.idLxn = idLxn; }

    public LoaiXetNghiem getLoaiXetNghiem() { return loaiXetNghiem; }
    public void setLoaiXetNghiem(LoaiXetNghiem loaiXetNghiem) {
        this.loaiXetNghiem = loaiXetNghiem;
        if (loaiXetNghiem != null) this.idLxn = loaiXetNghiem.getIdLxn();
    }

    public String getKetQua() { return ketQua; }
    public void setKetQua(String ketQua) { this.ketQua = ketQua; }

    public String getKetLuan() { return ketLuan; }
    public void setKetLuan(String ketLuan) { this.ketLuan = ketLuan; }

    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getIdKq() { return idKqxn; }
    public void setIdKq(String idKq) { this.idKqxn = idKq; }

    public String getGiaTriKq() { return ketQua; }
    public void setGiaTriKq(String giaTriKq) { this.ketQua = giaTriKq; }

    public LocalDateTime getThoiGianXn() { return thoiGian; }
    public void setThoiGianXn(LocalDateTime thoiGianXn) { this.thoiGian = thoiGianXn; }
}


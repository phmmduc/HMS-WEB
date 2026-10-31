package com.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "benhan")
public class BenhAn {

    @Id
    @Column(name = "id_benhan", length = 10, nullable = false)
    private String idBenhan;

    @Column(name = "id_bn", length = 10, insertable = false, updatable = false)
    private String idBn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bn", nullable = false)
    private BenhNhan benhNhan;

    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @Column(name = "tien_su_benh", columnDefinition = "TEXT")
    private String tienSuBenh;

    @Column(name = "trang_thai", length = 20)
    private String trangThai; // DANG_MO, DONG

    @OneToMany(mappedBy = "benhAn", fetch = FetchType.LAZY)
    private List<PhieuKham> danhSachPhieuKham = new ArrayList<>();

    public BenhAn() {}

    public BenhAn(String idBenhan, BenhNhan benhNhan, LocalDate ngayTao, String tienSuBenh, String trangThai) {
        this.idBenhan = idBenhan;
        this.benhNhan = benhNhan;
        if (benhNhan != null) this.idBn = benhNhan.getIdBn();
        this.ngayTao = ngayTao;
        this.tienSuBenh = tienSuBenh;
        this.trangThai = trangThai;
    }

    public String getIdBenhan() { return idBenhan; }
    public void setIdBenhan(String idBenhan) { this.idBenhan = idBenhan; }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public BenhNhan getBenhNhan() { return benhNhan; }
    public void setBenhNhan(BenhNhan benhNhan) {
        this.benhNhan = benhNhan;
        if (benhNhan != null) this.idBn = benhNhan.getIdBn();
    }

    public LocalDate getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDate ngayTao) { this.ngayTao = ngayTao; }

    public String getTienSuBenh() { return tienSuBenh; }
    public void setTienSuBenh(String tienSuBenh) { this.tienSuBenh = tienSuBenh; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public List<PhieuKham> getDanhSachPhieuKham() { return danhSachPhieuKham; }
    public void setDanhSachPhieuKham(List<PhieuKham> danhSachPhieuKham) { this.danhSachPhieuKham = danhSachPhieuKham; }
}


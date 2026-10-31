package com.hms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "v_phieukham_tongquan")
@Immutable
public class VPhieuKhamTongQuan {

    @Id
    @Column(name = "id_phieukham", length = 15)
    private String idPhieukham;

    @Column(name = "id_benhan", length = 10)
    private String idBenhan;

    @Column(name = "id_bn", length = 10)
    private String idBn;

    @Column(name = "ho_ten_bn", length = 100)
    private String hoTenBn;

    @Column(name = "id_pk", length = 10)
    private String idPk;

    @Column(name = "ten_phong", length = 100)
    private String tenPhong;

    @Column(name = "id_bacsy", length = 10)
    private String idBacsy;

    @Column(name = "ho_ten_bacsy", length = 100)
    private String hoTenBacsy;

    @Column(name = "thoi_gian_dky")
    private LocalDateTime thoiGianDky;

    @Column(name = "thoi_gian_kham")
    private LocalDateTime thoiGianKham;

    @Column(name = "phi_kham")
    private BigDecimal phiKham;

    @Column(name = "trang_thai", length = 20)
    private String trangThai;

    @Column(name = "co_xn")
    private Boolean coXn;

    @Column(name = "ke_don")
    private Boolean keDon;

    public VPhieuKhamTongQuan() {}

    public String getIdPhieukham() { return idPhieukham; }
    public void setIdPhieukham(String idPhieukham) { this.idPhieukham = idPhieukham; }

    public String getIdBenhan() { return idBenhan; }
    public void setIdBenhan(String idBenhan) { this.idBenhan = idBenhan; }

    public String getIdBn() { return idBn; }
    public void setIdBn(String idBn) { this.idBn = idBn; }

    public String getHoTenBn() { return hoTenBn; }
    public void setHoTenBn(String hoTenBn) { this.hoTenBn = hoTenBn; }

    public String getIdPk() { return idPk; }
    public void setIdPk(String idPk) { this.idPk = idPk; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public String getIdBacsy() { return idBacsy; }
    public void setIdBacsy(String idBacsy) { this.idBacsy = idBacsy; }

    public String getHoTenBacsy() { return hoTenBacsy; }
    public void setHoTenBacsy(String hoTenBacsy) { this.hoTenBacsy = hoTenBacsy; }

    public LocalDateTime getThoiGianDky() { return thoiGianDky; }
    public void setThoiGianDky(LocalDateTime thoiGianDky) { this.thoiGianDky = thoiGianDky; }

    public LocalDateTime getThoiGianKham() { return thoiGianKham; }
    public void setThoiGianKham(LocalDateTime thoiGianKham) { this.thoiGianKham = thoiGianKham; }

    public BigDecimal getPhiKham() { return phiKham; }
    public void setPhiKham(BigDecimal phiKham) { this.phiKham = phiKham; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Boolean getCoXn() { return coXn; }
    public void setCoXn(Boolean coXn) { this.coXn = coXn; }

    public Boolean getKeDon() { return keDon; }
    public void setKeDon(Boolean keDon) { this.keDon = keDon; }
}


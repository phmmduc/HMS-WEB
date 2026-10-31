package com.hms.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chuyenkhoa")
public class ChuyenKhoa {

    @Id
    @Column(name = "id_ck", length = 10, nullable = false)
    private String idCk;

    @Column(name = "ten_ck", length = 100, nullable = false, unique = true)
    private String tenCk;

    @OneToMany(mappedBy = "chuyenKhoa", fetch = FetchType.LAZY)
    private List<PhongKham> danhSachPhongKham = new ArrayList<>();

    @OneToMany(mappedBy = "chuyenKhoa", fetch = FetchType.LAZY)
    private List<BacSy> danhSachBacSy = new ArrayList<>();

    public ChuyenKhoa() {}

    public ChuyenKhoa(String idCk, String tenCk) {
        this.idCk = idCk;
        this.tenCk = tenCk;
    }

    public String getIdCk() { return idCk; }
    public void setIdCk(String idCk) { this.idCk = idCk; }

    public String getTenCk() { return tenCk; }
    public void setTenCk(String tenCk) { this.tenCk = tenCk; }

    public List<PhongKham> getDanhSachPhongKham() { return danhSachPhongKham; }
    public void setDanhSachPhongKham(List<PhongKham> danhSachPhongKham) { this.danhSachPhongKham = danhSachPhongKham; }

    public List<BacSy> getDanhSachBacSy() { return danhSachBacSy; }
    public void setDanhSachBacSy(List<BacSy> danhSachBacSy) { this.danhSachBacSy = danhSachBacSy; }
}


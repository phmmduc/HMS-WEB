package com.hms.repository;

import com.hms.entity.PhieuKhamNhanVien;
import com.hms.entity.id.PhieuKhamNhanVienId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhieuKhamNhanVienRepository extends JpaRepository<PhieuKhamNhanVien, PhieuKhamNhanVienId> {
    List<PhieuKhamNhanVien> findByIdIdPhieukham(String idPhieukham);
    List<PhieuKhamNhanVien> findByIdIdNv(String idNv);
}


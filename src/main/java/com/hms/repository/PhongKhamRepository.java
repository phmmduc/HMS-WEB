package com.hms.repository;

import com.hms.entity.PhongKham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhongKhamRepository extends JpaRepository<PhongKham, String> {
    List<PhongKham> findByIdCk(String idCk);
    List<PhongKham> findByChuyenKhoaIdCk(String idCk);
    List<PhongKham> findByTrangThai(String trangThai);
}

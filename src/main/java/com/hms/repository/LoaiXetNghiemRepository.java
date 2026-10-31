package com.hms.repository;

import com.hms.entity.LoaiXetNghiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoaiXetNghiemRepository extends JpaRepository<LoaiXetNghiem, String> {
    List<LoaiXetNghiem> findByTrangThai(String trangThai);
}


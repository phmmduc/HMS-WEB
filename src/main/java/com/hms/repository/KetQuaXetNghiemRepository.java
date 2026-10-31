package com.hms.repository;

import com.hms.entity.KetQuaXetNghiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KetQuaXetNghiemRepository extends JpaRepository<KetQuaXetNghiem, String> {
    List<KetQuaXetNghiem> findByIdPhieukham(String idPhieukham);
    List<KetQuaXetNghiem> findByPhieuKhamIdPhieukham(String idPhieukham);
    List<KetQuaXetNghiem> findByIdLxn(String idLxn);
    List<KetQuaXetNghiem> findByTrangThai(String trangThai);
}


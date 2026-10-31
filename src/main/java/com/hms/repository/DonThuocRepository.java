package com.hms.repository;

import com.hms.entity.DonThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonThuocRepository extends JpaRepository<DonThuoc, String> {
    Optional<DonThuoc> findByIdPhieukham(String idPhieukham);
    List<DonThuoc> findByTrangThai(String trangThai);
    List<DonThuoc> findByIdDuocsi(String idDuocsi);
}


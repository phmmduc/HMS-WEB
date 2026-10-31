package com.hms.repository;

import com.hms.entity.ChiTietDonThuoc;
import com.hms.entity.id.ChiTietDonThuocId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietDonThuocRepository extends JpaRepository<ChiTietDonThuoc, ChiTietDonThuocId> {
    List<ChiTietDonThuoc> findByIdIdDt(String idDt);
}


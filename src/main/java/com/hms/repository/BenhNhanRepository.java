package com.hms.repository;

import com.hms.entity.BenhNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BenhNhanRepository extends JpaRepository<BenhNhan, String> {

    @Query("SELECT b FROM BenhNhan b WHERE LOWER(b.hoTen) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(b.idBn) LIKE LOWER(CONCAT('%', :kw, '%')) OR (b.cccd IS NOT NULL AND b.cccd LIKE CONCAT('%', :kw, '%'))")
    List<BenhNhan> searchPatients(@Param("kw") String keyword);
}


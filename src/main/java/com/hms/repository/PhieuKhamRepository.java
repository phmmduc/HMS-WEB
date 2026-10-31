package com.hms.repository;

import com.hms.entity.PhieuKham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhieuKhamRepository extends JpaRepository<PhieuKham, String> {
    List<PhieuKham> findByIdBenhan(String idBenhan);
    List<PhieuKham> findByIdPk(String idPk);
    List<PhieuKham> findByIdBacsy(String idBacsy);
    List<PhieuKham> findByTrangThai(String trangThai);

    @Query("SELECT p FROM PhieuKham p WHERE (:status IS NULL OR p.trangThai = :status) AND (:roomId IS NULL OR p.idPk = :roomId)")
    List<PhieuKham> filterExams(@Param("status") String status, @Param("roomId") String roomId);
}


package com.hms.repository;

import com.hms.entity.LoThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoThuocRepository extends JpaRepository<LoThuoc, String> {
    List<LoThuoc> findByIdThuoc(String idThuoc);
    Optional<LoThuoc> findByIdThuocAndSoLo(String idThuoc, String soLo);
    List<LoThuoc> findByHanSuDungBefore(LocalDate date);

    @Query("SELECT COALESCE(SUM(l.soLuongCon), 0) FROM LoThuoc l WHERE l.idThuoc = :idThuoc")
    Integer sumStockByMedicine(@Param("idThuoc") String idThuoc);

    void deleteByIdThuoc(String idThuoc);
}

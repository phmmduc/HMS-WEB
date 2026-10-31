package com.hms.repository;

import com.hms.entity.Thuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ThuocRepository extends JpaRepository<Thuoc, String> {

    @Query("SELECT t FROM Thuoc t WHERE LOWER(t.tenThuoc) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(t.idThuoc) LIKE LOWER(CONCAT('%', :kw, '%'))")
    List<Thuoc> searchMedicines(@Param("kw") String keyword);
}


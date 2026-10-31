package com.hms.repository;

import com.hms.entity.BaoHiemYTe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BaoHiemYTeRepository extends JpaRepository<BaoHiemYTe, String> {
    Optional<BaoHiemYTe> findByIdBn(String idBn);
    void deleteByIdBn(String idBn);
}

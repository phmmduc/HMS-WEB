package com.hms.repository;

import com.hms.entity.BenhAn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BenhAnRepository extends JpaRepository<BenhAn, String> {
    List<BenhAn> findByIdBn(String idBn);
    Optional<BenhAn> findFirstByIdBnAndTrangThaiOrderByNgayTaoDesc(String idBn, String trangThai);
}


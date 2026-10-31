package com.hms.repository;

import com.hms.entity.VPhieuKhamTongQuan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VPhieuKhamTongQuanRepository extends JpaRepository<VPhieuKhamTongQuan, String> {
}


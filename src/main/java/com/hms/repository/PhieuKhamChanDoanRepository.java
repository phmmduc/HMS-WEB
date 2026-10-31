package com.hms.repository;

import com.hms.entity.PhieuKhamChanDoan;
import com.hms.entity.id.PhieuKhamChanDoanId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhieuKhamChanDoanRepository extends JpaRepository<PhieuKhamChanDoan, PhieuKhamChanDoanId> {
    List<PhieuKhamChanDoan> findByIdIdPhieukham(String idPhieukham);
}


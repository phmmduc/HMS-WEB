package com.hms.repository;

import com.hms.entity.ChanDoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChanDoanRepository extends JpaRepository<ChanDoan, String> {
}


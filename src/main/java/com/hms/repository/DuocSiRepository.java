package com.hms.repository;

import com.hms.entity.DuocSi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DuocSiRepository extends JpaRepository<DuocSi, String> {
}


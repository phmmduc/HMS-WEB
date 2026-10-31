package com.hms.repository;

import com.hms.entity.YTa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YTaRepository extends JpaRepository<YTa, String> {
}


package com.hms.repository;

import com.hms.entity.ChuyenKhoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChuyenKhoaRepository extends JpaRepository<ChuyenKhoa, String> {
}


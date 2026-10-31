package com.hms.repository;

import com.hms.entity.VDonThuocTongTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VDonThuocTongTienRepository extends JpaRepository<VDonThuocTongTien, String> {
}


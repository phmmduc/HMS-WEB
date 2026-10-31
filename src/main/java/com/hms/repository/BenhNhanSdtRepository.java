package com.hms.repository;

import com.hms.entity.BenhNhanSdt;
import com.hms.entity.id.BenhNhanSdtId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BenhNhanSdtRepository extends JpaRepository<BenhNhanSdt, BenhNhanSdtId> {
    List<BenhNhanSdt> findByIdIdBn(String idBn);
    void deleteByIdIdBn(String idBn);
}

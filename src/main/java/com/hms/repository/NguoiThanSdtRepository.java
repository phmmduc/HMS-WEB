package com.hms.repository;

import com.hms.entity.NguoiThanSdt;
import com.hms.entity.id.NguoiThanSdtId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NguoiThanSdtRepository extends JpaRepository<NguoiThanSdt, NguoiThanSdtId> {
    List<NguoiThanSdt> findByIdIdNt(String idNt);
    void deleteByIdIdNt(String idNt);
}

package com.hms.repository;

import com.hms.entity.NhanVienSdt;
import com.hms.entity.id.NhanVienSdtId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NhanVienSdtRepository extends JpaRepository<NhanVienSdt, NhanVienSdtId> {
    List<NhanVienSdt> findByIdIdNv(String idNv);
    void deleteByIdIdNv(String idNv);
}

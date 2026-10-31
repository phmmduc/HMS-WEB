package com.hms.repository;

import com.hms.entity.NguoiThan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NguoiThanRepository extends JpaRepository<NguoiThan, String> {
    List<NguoiThan> findByIdBn(String idBn);
    void deleteByIdBn(String idBn);
}

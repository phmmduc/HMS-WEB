package com.hms.service;

import com.hms.entity.*;
import com.hms.entity.id.NhanVienSdtId;
import com.hms.model.Staff;
import com.hms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class StaffDbService {

    private final NhanVienRepository nhanVienRepo;
    private final NhanVienSdtRepository nhanVienSdtRepo;
    private final BacSyRepository bacSyRepo;
    private final YTaRepository yTaRepo;
    private final DuocSiRepository duocSiRepo;
    private final ChuyenKhoaRepository chuyenKhoaRepo;
    private final EntityModelMapper mapper;

    public StaffDbService(NhanVienRepository nhanVienRepo,
                          NhanVienSdtRepository nhanVienSdtRepo,
                          BacSyRepository bacSyRepo,
                          YTaRepository yTaRepo,
                          DuocSiRepository duocSiRepo,
                          ChuyenKhoaRepository chuyenKhoaRepo,
                          EntityModelMapper mapper) {
        this.nhanVienRepo = nhanVienRepo;
        this.nhanVienSdtRepo = nhanVienSdtRepo;
        this.bacSyRepo = bacSyRepo;
        this.yTaRepo = yTaRepo;
        this.duocSiRepo = duocSiRepo;
        this.chuyenKhoaRepo = chuyenKhoaRepo;
        this.mapper = mapper;
    }

    public List<Staff> findAllStaff() {
        List<NhanVien> list = nhanVienRepo.findAll();
        List<Staff> result = new ArrayList<>();
        for (NhanVien nv : list) {
            result.add(loadStaffModel(nv));
        }
        return result;
    }

    public Optional<Staff> findStaffById(String idNv) {
        if (idNv == null) return Optional.empty();
        return nhanVienRepo.findById(idNv).map(this::loadStaffModel);
    }

    private Staff loadStaffModel(NhanVien nv) {
        List<NhanVienSdt> phones = nhanVienSdtRepo.findByIdIdNv(nv.getIdNv());
        BacSy bacSy = bacSyRepo.findById(nv.getIdNv()).orElse(null);
        YTa yTa = yTaRepo.findById(nv.getIdNv()).orElse(null);
        DuocSi duocSi = duocSiRepo.findById(nv.getIdNv()).orElse(null);
        return mapper.toStaffModel(nv, phones, bacSy, yTa, duocSi);
    }

    @Transactional
    public Staff saveStaff(Staff staff) {
        if (staff == null || staff.getIdNv() == null) return null;

        NhanVien nv = mapper.toNhanVienEntity(staff);
        nhanVienRepo.save(nv);

        // Phone numbers
        nhanVienSdtRepo.deleteByIdIdNv(staff.getIdNv());
        if (staff.getPhoneNumbers() != null) {
            for (String phone : staff.getPhoneNumbers()) {
                if (phone != null && !phone.isBlank()) {
                    NhanVienSdt sdt = new NhanVienSdt(new NhanVienSdtId(staff.getIdNv(), phone.trim()), nv);
                    nhanVienSdtRepo.save(sdt);
                }
            }
        }

        // Subtypes based on vaiTro
        String role = staff.getVaiTro() != null ? staff.getVaiTro().trim() : "Quản lý";
        if ("Bác sĩ".equalsIgnoreCase(role)) {
            BacSy bs = bacSyRepo.findById(staff.getIdNv()).orElse(new BacSy());
            bs.setIdNv(staff.getIdNv());
            bs.setNhanVien(nv);
            bs.setHocVi(staff.getChucDanh() != null ? staff.getChucDanh() : "Bác sĩ CKI");
            bs.setSoCchn("CCHN-" + staff.getIdNv());
            if (bs.getChuyenKhoa() == null) {
                chuyenKhoaRepo.findById("CK01").ifPresent(bs::setChuyenKhoa);
            }
            bacSyRepo.save(bs);
            yTaRepo.deleteById(staff.getIdNv());
            duocSiRepo.deleteById(staff.getIdNv());
        } else if ("Y tá".equalsIgnoreCase(role)) {
            YTa y = yTaRepo.findById(staff.getIdNv()).orElse(new YTa());
            y.setIdNv(staff.getIdNv());
            y.setNhanVien(nv);
            y.setCapBac(staff.getChucDanh() != null ? staff.getChucDanh() : "Điều dưỡng");
            yTaRepo.save(y);
            bacSyRepo.deleteById(staff.getIdNv());
            duocSiRepo.deleteById(staff.getIdNv());
        } else if ("Dược sĩ".equalsIgnoreCase(role)) {
            DuocSi ds = duocSiRepo.findById(staff.getIdNv()).orElse(new DuocSi());
            ds.setIdNv(staff.getIdNv());
            ds.setNhanVien(nv);
            ds.setBangCap(staff.getChucDanh() != null ? staff.getChucDanh() : "Dược sĩ đại học");
            duocSiRepo.save(ds);
            bacSyRepo.deleteById(staff.getIdNv());
            yTaRepo.deleteById(staff.getIdNv());
        } else {
            bacSyRepo.deleteById(staff.getIdNv());
            yTaRepo.deleteById(staff.getIdNv());
            duocSiRepo.deleteById(staff.getIdNv());
        }

        return findStaffById(staff.getIdNv()).orElse(staff);
    }

    @Transactional
    public void deleteStaff(String idNv) {
        if (idNv == null) return;
        nhanVienSdtRepo.deleteByIdIdNv(idNv);
        bacSyRepo.deleteById(idNv);
        yTaRepo.deleteById(idNv);
        duocSiRepo.deleteById(idNv);
        nhanVienRepo.deleteById(idNv);
    }
}

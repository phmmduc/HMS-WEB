package com.hms.service;

import com.hms.entity.*;
import com.hms.model.Examination;
import com.hms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ExaminationDbService {

    private final PhieuKhamRepository phieuKhamRepo;
    private final PhieuKhamNhanVienRepository phieuKhamNhanVienRepo;
    private final PhieuKhamChanDoanRepository phieuKhamChanDoanRepo;
    private final BenhAnRepository benhAnRepo;
    private final PhongKhamRepository phongKhamRepo;
    private final BacSyRepository bacSyRepo;
    private final ChanDoanRepository chanDoanRepo;
    private final EntityModelMapper mapper;

    public ExaminationDbService(PhieuKhamRepository phieuKhamRepo,
                               PhieuKhamNhanVienRepository phieuKhamNhanVienRepo,
                               PhieuKhamChanDoanRepository phieuKhamChanDoanRepo,
                               BenhAnRepository benhAnRepo,
                               PhongKhamRepository phongKhamRepo,
                               BacSyRepository bacSyRepo,
                               ChanDoanRepository chanDoanRepo,
                               EntityModelMapper mapper) {
        this.phieuKhamRepo = phieuKhamRepo;
        this.phieuKhamNhanVienRepo = phieuKhamNhanVienRepo;
        this.phieuKhamChanDoanRepo = phieuKhamChanDoanRepo;
        this.benhAnRepo = benhAnRepo;
        this.phongKhamRepo = phongKhamRepo;
        this.bacSyRepo = bacSyRepo;
        this.chanDoanRepo = chanDoanRepo;
        this.mapper = mapper;
    }

    public List<Examination> findAllExaminations() {
        return phieuKhamRepo.findAll().stream()
                .map(mapper::toExaminationModel)
                .collect(Collectors.toList());
    }

    public Optional<Examination> findExaminationById(String id) {
        if (id == null) return Optional.empty();
        return phieuKhamRepo.findById(id).map(mapper::toExaminationModel);
    }

    @Transactional
    public Examination saveExamination(Examination exam) {
        if (exam == null || exam.getIdPhieukham() == null) return null;

        BenhAn benhAn = null;
        if (exam.getIdBn() != null) {
            List<BenhAn> records = benhAnRepo.findByIdBn(exam.getIdBn());
            if (!records.isEmpty()) {
                benhAn = records.get(0);
            } else {
                benhAn = new BenhAn();
                benhAn.setIdBenhan("BA" + exam.getIdBn().replace("BN", ""));
                benhAn.setIdBn(exam.getIdBn());
                benhAn.setNgayTao(LocalDate.now());
                benhAn.setTrangThai("DANG_MO");
                benhAnRepo.save(benhAn);
            }
        }

        PhongKham pk = null;
        if (exam.getIdPk() != null) {
            pk = phongKhamRepo.findById(exam.getIdPk()).orElse(null);
        }

        BacSy bs = null;
        if (exam.getIdNv() != null) {
            bs = bacSyRepo.findById(exam.getIdNv()).orElse(null);
        }

        PhieuKham entity = phieuKhamRepo.findById(exam.getIdPhieukham()).orElse(new PhieuKham());
        entity.setIdPhieukham(exam.getIdPhieukham());
        entity.setBenhAn(benhAn);
        entity.setPhongKham(pk);
        entity.setBacSy(bs);
        entity.setThoiGianKham(LocalDateTime.now());
        entity.setTrieuChung(exam.getTrieuChung());
        entity.setTrangThai(exam.getTrangThai() != null ? exam.getTrangThai() : "CHO_KHAM");
        entity.setPhiKham(BigDecimal.valueOf(exam.getPhiKham() != null ? exam.getPhiKham() : 150000L));

        phieuKhamRepo.save(entity);
        return findExaminationById(exam.getIdPhieukham()).orElse(exam);
    }

    @Transactional
    public void updateStatus(String idPhieukham, String newStatus) {
        if (idPhieukham == null || newStatus == null) return;
        phieuKhamRepo.findById(idPhieukham).ifPresent(pk -> {
            pk.setTrangThai(newStatus);
            phieuKhamRepo.save(pk);
        });
    }
}

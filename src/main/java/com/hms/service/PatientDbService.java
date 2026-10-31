package com.hms.service;

import com.hms.entity.*;
import com.hms.entity.id.BenhNhanSdtId;
import com.hms.entity.id.NguoiThanSdtId;
import com.hms.model.HealthInsurance;
import com.hms.model.Patient;
import com.hms.model.PatientRelative;
import com.hms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PatientDbService {

    private final BenhNhanRepository benhNhanRepo;
    private final BenhNhanSdtRepository benhNhanSdtRepo;
    private final BaoHiemYTeRepository baoHiemYTeRepo;
    private final NguoiThanRepository nguoiThanRepo;
    private final NguoiThanSdtRepository nguoiThanSdtRepo;
    private final BenhAnRepository benhAnRepo;
    private final EntityModelMapper mapper;

    public PatientDbService(BenhNhanRepository benhNhanRepo,
                            BenhNhanSdtRepository benhNhanSdtRepo,
                            BaoHiemYTeRepository baoHiemYTeRepo,
                            NguoiThanRepository nguoiThanRepo,
                            NguoiThanSdtRepository nguoiThanSdtRepo,
                            BenhAnRepository benhAnRepo,
                            EntityModelMapper mapper) {
        this.benhNhanRepo = benhNhanRepo;
        this.benhNhanSdtRepo = benhNhanSdtRepo;
        this.baoHiemYTeRepo = baoHiemYTeRepo;
        this.nguoiThanRepo = nguoiThanRepo;
        this.nguoiThanSdtRepo = nguoiThanSdtRepo;
        this.benhAnRepo = benhAnRepo;
        this.mapper = mapper;
    }

    public List<Patient> findAllPatients() {
        List<BenhNhan> list = benhNhanRepo.findAll();
        List<Patient> result = new ArrayList<>();
        for (BenhNhan bn : list) {
            result.add(loadPatientModel(bn));
        }
        return result;
    }

    public Optional<Patient> findPatientById(String idBn) {
        if (idBn == null) return Optional.empty();
        return benhNhanRepo.findById(idBn).map(this::loadPatientModel);
    }

    private Patient loadPatientModel(BenhNhan bn) {
        List<BenhNhanSdt> phones = benhNhanSdtRepo.findByIdIdBn(bn.getIdBn());
        List<NguoiThan> relatives = nguoiThanRepo.findByIdBn(bn.getIdBn());
        NguoiThan rel = relatives.isEmpty() ? null : relatives.get(0);
        List<NguoiThanSdt> relPhones = rel != null ? nguoiThanSdtRepo.findByIdIdNt(rel.getIdNt()) : Collections.emptyList();
        BaoHiemYTe ins = baoHiemYTeRepo.findByIdBn(bn.getIdBn()).orElse(null);
        return mapper.toPatientModel(bn, phones, rel, relPhones, ins);
    }

    @Transactional
    public Patient savePatient(Patient patient) {
        if (patient == null || patient.getIdBn() == null) return null;

        BenhNhan bn = mapper.toBenhNhanEntity(patient);
        benhNhanRepo.save(bn);

        // Phone numbers
        benhNhanSdtRepo.deleteByIdIdBn(patient.getIdBn());
        if (patient.getSdt() != null) {
            for (String phone : patient.getSdt()) {
                if (phone != null && !phone.isBlank()) {
                    BenhNhanSdt pEntity = new BenhNhanSdt(new BenhNhanSdtId(patient.getIdBn(), phone.trim()), bn);
                    benhNhanSdtRepo.save(pEntity);
                }
            }
        }

        // Relative
        if (patient.getRelative() != null && patient.getRelative().getHoTen() != null && !patient.getRelative().getHoTen().isBlank()) {
            PatientRelative rModel = patient.getRelative();
            String idNt = rModel.getIdNt() != null ? rModel.getIdNt() : ("NT" + System.currentTimeMillis() % 100000);
            rModel.setIdNt(idNt);
            rModel.setIdBn(patient.getIdBn());

            NguoiThan nt = new NguoiThan();
            nt.setIdNt(idNt);
            nt.setIdBn(patient.getIdBn());
            nt.setHoTen(rModel.getHoTen());
            nt.setMoiQuanHe(rModel.getMoiQuanHe());
            nt.setGioiTinh(rModel.getGioiTinh());
            nguoiThanRepo.save(nt);

            nguoiThanSdtRepo.deleteByIdIdNt(idNt);
            if (rModel.getSdt() != null) {
                for (String rp : rModel.getSdt()) {
                    if (rp != null && !rp.isBlank()) {
                        NguoiThanSdt ntPhone = new NguoiThanSdt(new NguoiThanSdtId(idNt, rp.trim()), nt);
                        nguoiThanSdtRepo.save(ntPhone);
                    }
                }
            }
        }

        // Insurance
        if (patient.getInsurance() != null && patient.getInsurance().getSoBhyt() != null && !patient.getInsurance().getSoBhyt().isBlank()) {
            HealthInsurance insModel = patient.getInsurance();
            BaoHiemYTe ins = new BaoHiemYTe();
            ins.setSobhyt(insModel.getSoBhyt().trim());
            ins.setIdBn(patient.getIdBn());
            ins.setMucHuong(insModel.getMucHuong() != null ? insModel.getMucHuong() : 80);
            ins.setNhomDoiTuong(insModel.getNhomDoiTuong());
            ins.setNoiDangKy(insModel.getNoiDangKy());
            if (insModel.getNgayBatDau() != null && !insModel.getNgayBatDau().isBlank()) {
                try { ins.setNgayBatDau(java.time.LocalDate.parse(insModel.getNgayBatDau().trim())); } catch (Exception ignored) {}
            }
            if (insModel.getNgayKetThuc() != null && !insModel.getNgayKetThuc().isBlank()) {
                try { ins.setNgayKetThuc(java.time.LocalDate.parse(insModel.getNgayKetThuc().trim())); } catch (Exception ignored) {}
            }
            baoHiemYTeRepo.save(ins);
        }

        return findPatientById(patient.getIdBn()).orElse(patient);
    }

    @Transactional
    public void deletePatient(String idBn) {
        if (idBn == null) return;
        benhNhanSdtRepo.deleteByIdIdBn(idBn);
        baoHiemYTeRepo.deleteByIdBn(idBn);
        List<NguoiThan> rels = nguoiThanRepo.findByIdBn(idBn);
        for (NguoiThan r : rels) {
            nguoiThanSdtRepo.deleteByIdIdNt(r.getIdNt());
            nguoiThanRepo.delete(r);
        }
        benhNhanRepo.deleteById(idBn);
    }
}

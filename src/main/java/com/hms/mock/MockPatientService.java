package com.hms.mock;

import com.hms.dto.PatientFormDto;
import com.hms.model.HealthInsurance;
import com.hms.model.MedicalRecord;
import com.hms.model.Patient;
import com.hms.model.PatientRelative;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MockPatientService {

    private final MockDataStore store;

    public MockPatientService(MockDataStore store) {
        this.store = store;
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(store.patients.values()).stream()
                .sorted((a, b) -> b.getIdBn().compareTo(a.getIdBn()))
                .collect(Collectors.toList());
    }

    public Patient getPatientById(String id) {
        if (id == null) return null;
        return store.patients.get(id);
    }

    public List<Patient> searchPatients(String keyword, String status) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return getAllPatients().stream()
                .filter(p -> {
                    boolean matchesStatus = (status == null || status.isEmpty() || "all".equalsIgnoreCase(status) || status.equalsIgnoreCase(p.getTrangThai()));
                    if (!matchesStatus) return false;
                    if (kw.isEmpty()) return true;
                    String target = (p.getIdBn() + " " + p.getHoTen() + " " + (p.getCccd() != null ? p.getCccd() : "") + " " + p.getPhoneDisplay() + " " + (p.getDiaChi() != null ? p.getDiaChi() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }

    public Patient savePatient(PatientFormDto dto) {
        String patientId = dto.getIdBn();
        boolean isEdit = patientId != null && !patientId.trim().isEmpty() && store.patients.containsKey(patientId);
        if (!isEdit) {
            patientId = store.nextPatientId();
        }

        List<String> phoneList = new ArrayList<>();
        if (dto.getSdt() != null && !dto.getSdt().trim().isEmpty()) {
            for (String part : dto.getSdt().split("[,;\\n]+")) {
                String clean = part.trim();
                if (!clean.isEmpty()) phoneList.add(clean);
            }
        }

        Patient patient = isEdit ? store.patients.get(patientId) : new Patient();
        patient.setIdBn(patientId);
        patient.setHoTen(dto.getHoTen() != null ? dto.getHoTen().trim() : "Bệnh nhân mới");
        patient.setCccd(dto.getCccd() != null ? dto.getCccd().trim() : "");
        patient.setNgaySinh(dto.getNgaySinh());
        patient.setGioiTinh(dto.getGioiTinh() != null ? dto.getGioiTinh() : "Nam");
        patient.setSdt(phoneList);
        patient.setDiaChi(dto.getDiaChi() != null ? dto.getDiaChi().trim() : "");
        patient.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : "Ổn định");
        if (!isEdit) {
            patient.setNgayTao(LocalDate.now().toString());
        }

        // Relative
        if (dto.getRelHoTen() != null && !dto.getRelHoTen().trim().isEmpty()) {
            PatientRelative rel = patient.getRelative() != null ? patient.getRelative() : new PatientRelative();
            if (rel.getIdNt() == null) rel.setIdNt("NT" + System.currentTimeMillis() % 10000);
            rel.setIdBn(patientId);
            rel.setHoTen(dto.getRelHoTen().trim());
            rel.setMoiQuanHe(dto.getRelMoiQuanHe() != null ? dto.getRelMoiQuanHe().trim() : "Người nhà");
            rel.setGioiTinh(dto.getRelGioiTinh());
            List<String> relPhones = new ArrayList<>();
            if (dto.getRelSdt() != null && !dto.getRelSdt().trim().isEmpty()) {
                relPhones.add(dto.getRelSdt().trim());
            }
            rel.setSdt(relPhones);
            store.relatives.put(rel.getIdNt(), rel);
            patient.setRelative(rel);
        }

        // Insurance
        if (dto.getSoBhyt() != null && !dto.getSoBhyt().trim().isEmpty()) {
            HealthInsurance ins = patient.getInsurance() != null ? patient.getInsurance() : new HealthInsurance();
            if (ins.getIdBhyt() == null) ins.setIdBhyt("BHYT" + System.currentTimeMillis() % 10000);
            ins.setIdBn(patientId);
            ins.setSoBhyt(dto.getSoBhyt().trim());
            ins.setMucHuong(dto.getMucHuong() != null ? dto.getMucHuong() : 80);
            ins.setNgayBatDau(dto.getNgayBatDau());
            ins.setNgayKetThuc(dto.getNgayKetThuc());
            ins.setNoiDangKy(dto.getNoiDangKy());
            ins.setNhomDoiTuong(dto.getNhomDoiTuong());
            store.insurances.put(ins.getIdBhyt(), ins);
            patient.setInsurance(ins);
        }

        store.patients.put(patientId, patient);
        store.linkAll();
        return patient;
    }

    public List<MedicalRecord> getMedicalRecordsByPatient(String patientId) {
        if (patientId == null) return Collections.emptyList();
        return store.medicalRecords.values().stream()
                .filter(mr -> patientId.equals(mr.getIdBn()))
                .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao()))
                .collect(Collectors.toList());
    }

    public List<Patient> getPatientsForStaff(com.hms.model.Staff staff, String keyword, String status) {
        if (staff == null || !"Bác sĩ".equalsIgnoreCase(staff.getVaiTro())) {
            return searchPatients(keyword, status);
        }

        String docId = staff.getIdNv();
        String docRoomId = staff.getIdPk();

        Set<String> doctorPatientIds = store.examinations.values().stream()
                .filter(ex -> (docId != null && docId.equals(ex.getIdNv()))
                        || (docRoomId != null && docRoomId.equals(ex.getIdPk())))
                .map(com.hms.model.Examination::getIdBn)
                .collect(Collectors.toSet());

        return searchPatients(keyword, status).stream()
                .filter(p -> doctorPatientIds.contains(p.getIdBn()))
                .collect(Collectors.toList());
    }
}


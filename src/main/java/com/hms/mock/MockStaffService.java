package com.hms.mock;

import com.hms.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MockStaffService {

    private final MockDataStore store;

    public MockStaffService(MockDataStore store) {
        this.store = store;
    }

    public List<Staff> getAllStaff() {
        return new ArrayList<>(store.staffMembers.values()).stream()
                .sorted((a, b) -> a.getIdNv().compareTo(b.getIdNv()))
                .collect(Collectors.toList());
    }

    public List<Staff> getDoctors() {
        return getAllStaff().stream()
                .filter(s -> "Bác sĩ".equalsIgnoreCase(s.getVaiTro()))
                .collect(Collectors.toList());
    }

    public Staff getStaffById(String id) {
        if (id == null) return null;
        return store.staffMembers.get(id);
    }

    public List<Specialty> getAllSpecialties() {
        return new ArrayList<>(store.specialties.values()).stream()
                .sorted((a, b) -> a.getIdCk().compareTo(b.getIdCk()))
                .collect(Collectors.toList());
    }

    public List<ClinicRoom> getAllRooms() {
        return new ArrayList<>(store.rooms.values()).stream()
                .sorted((a, b) -> a.getIdPk().compareTo(b.getIdPk()))
                .collect(Collectors.toList());
    }

    public List<LabType> getAllLabTypes() {
        return new ArrayList<>(store.labTypes.values()).stream()
                .sorted((a, b) -> a.getIdLxn().compareTo(b.getIdLxn()))
                .collect(Collectors.toList());
    }

    public List<Diagnosis> getAllDiagnoses() {
        return new ArrayList<>(store.diagnoses.values()).stream()
                .sorted((a, b) -> a.getMaIcd().compareTo(b.getMaIcd()))
                .collect(Collectors.toList());
    }

    public List<LabResult> getAllLabResults() {
        store.linkAll();
        return new ArrayList<>(store.labResults.values()).stream()
                .sorted((a, b) -> b.getIdKqxn().compareTo(a.getIdKqxn()))
                .collect(Collectors.toList());
    }

    public boolean updateLabResult(String kqId, String result, String conclusion) {
        LabResult lr = store.labResults.get(kqId);
        if (lr == null) return false;
        lr.setKetQua(result != null ? result.trim() : "");
        lr.setKetLuan(conclusion != null ? conclusion.trim() : "");
        lr.setTrangThai("Hoàn thành");
        store.linkAll();
        return true;
    }
}


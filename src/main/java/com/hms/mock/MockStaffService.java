package com.hms.mock;

import com.hms.dto.StaffFormDto;
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

    public List<Staff> getStaffByRole(String roleName) {
        if (roleName == null || roleName.isBlank()) return getAllStaff();
        return getAllStaff().stream()
                .filter(s -> roleName.equalsIgnoreCase(s.getVaiTro()))
                .collect(Collectors.toList());
    }

    public Staff getStaffById(String id) {
        if (id == null) return null;
        return store.staffMembers.get(id);
    }

    public Staff createStaff(StaffFormDto dto) {
        String idNv = store.nextStaffId();
        List<String> phones = (dto.getPhone() != null && !dto.getPhone().isBlank())
                ? List.of(dto.getPhone().trim())
                : new ArrayList<>();

        Staff staff = new Staff(
                idNv,
                dto.getHoTen() != null ? dto.getHoTen().trim() : "Chưa đặt tên",
                dto.getGioiTinh() != null ? dto.getGioiTinh().trim() : "Khác",
                dto.getNgaySinh() != null ? dto.getNgaySinh().trim() : "",
                phones,
                dto.getEmail() != null ? dto.getEmail().trim() : "",
                dto.getVaiTro() != null ? dto.getVaiTro().trim() : "Bác sĩ",
                dto.getTrangThai() != null ? dto.getTrangThai().trim() : "Đang làm việc",
                dto.getSpecialtyDetail() != null ? dto.getSpecialtyDetail().trim() : ""
        );
        store.staffMembers.put(idNv, staff);
        return staff;
    }

    public Staff updateStaff(StaffFormDto dto) {
        Staff staff = store.staffMembers.get(dto.getIdNv());
        if (staff == null) return null;

        if (dto.getHoTen() != null && !dto.getHoTen().isBlank()) staff.setHoTen(dto.getHoTen().trim());
        if (dto.getGioiTinh() != null) staff.setGioiTinh(dto.getGioiTinh().trim());
        if (dto.getNgaySinh() != null) staff.setNgaySinh(dto.getNgaySinh().trim());
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            staff.setSdt(List.of(dto.getPhone().trim()));
        }
        if (dto.getEmail() != null) staff.setEmail(dto.getEmail().trim());
        if (dto.getVaiTro() != null && !dto.getVaiTro().isBlank()) staff.setVaiTro(dto.getVaiTro().trim());
        if (dto.getTrangThai() != null && !dto.getTrangThai().isBlank()) staff.setTrangThai(dto.getTrangThai().trim());
        if (dto.getSpecialtyDetail() != null) staff.setSpecialtyDetail(dto.getSpecialtyDetail().trim());

        return staff;
    }

    public void deleteStaff(String idNv) {
        if (idNv == null || idNv.isBlank()) return;

        // Constraint 1: Check examinations
        boolean hasExams = store.examinations.values().stream()
                .anyMatch(ex -> idNv.equals(ex.getIdNv()));
        if (hasExams) {
            throw new IllegalStateException("Không thể xóa nhân sự " + idNv + " do đang phụ trách hồ sơ khám bệnh!");
        }

        // Constraint 2: Check prescriptions
        boolean hasRx = store.prescriptions.values().stream()
                .anyMatch(rx -> idNv.equals(rx.getIdNv()));
        if (hasRx) {
            throw new IllegalStateException("Không thể xóa nhân sự " + idNv + " do đã ký kê đơn thuốc trong hệ thống!");
        }

        store.staffMembers.remove(idNv);
    }

    // SPECIALTIES
    public List<Specialty> getAllSpecialties() {
        return new ArrayList<>(store.specialties.values()).stream()
                .sorted((a, b) -> a.getIdCk().compareTo(b.getIdCk()))
                .collect(Collectors.toList());
    }

    public Specialty createSpecialty(Specialty specialty) {
        String idCk = (specialty.getIdCk() != null && !specialty.getIdCk().isBlank())
                ? specialty.getIdCk().trim().toUpperCase()
                : store.nextSpecialtyId();
        Specialty sp = new Specialty(idCk, specialty.getTenCk() != null ? specialty.getTenCk().trim() : "Chuyên khoa mới");
        store.specialties.put(idCk, sp);
        return sp;
    }

    public Specialty updateSpecialty(Specialty specialty) {
        Specialty sp = store.specialties.get(specialty.getIdCk());
        if (sp == null) return null;
        sp.setTenCk(specialty.getTenCk() != null ? specialty.getTenCk().trim() : sp.getTenCk());
        // Sync related rooms
        for (ClinicRoom r : store.rooms.values()) {
            if (sp.getIdCk().equals(r.getIdCk())) {
                r.setTenChuyenKhoa(sp.getTenCk());
            }
        }
        return sp;
    }

    public void deleteSpecialty(String idCk) {
        if (idCk == null || idCk.isBlank()) return;

        boolean hasRooms = store.rooms.values().stream()
                .anyMatch(r -> idCk.equals(r.getIdCk()));
        if (hasRooms) {
            throw new IllegalStateException("Không thể xóa chuyên khoa " + idCk + " vì đang có phòng khám trực thuộc!");
        }

        store.specialties.remove(idCk);
    }

    // CLINIC ROOMS
    public List<ClinicRoom> getAllRooms() {
        return new ArrayList<>(store.rooms.values()).stream()
                .sorted((a, b) -> a.getIdPk().compareTo(b.getIdPk()))
                .collect(Collectors.toList());
    }

    public ClinicRoom createRoom(ClinicRoom room) {
        String idPk = (room.getIdPk() != null && !room.getIdPk().isBlank())
                ? room.getIdPk().trim().toUpperCase()
                : store.nextRoomId();
        room.setIdPk(idPk);
        if (room.getIdCk() != null) {
            Specialty sp = store.specialties.get(room.getIdCk());
            if (sp != null) room.setTenChuyenKhoa(sp.getTenCk());
        }
        if (room.getTrangThai() == null || room.getTrangThai().isBlank()) {
            room.setTrangThai("Hoạt động");
        }
        store.rooms.put(idPk, room);
        return room;
    }

    public ClinicRoom updateRoom(ClinicRoom room) {
        ClinicRoom r = store.rooms.get(room.getIdPk());
        if (r == null) return null;
        if (room.getTenPhong() != null) r.setTenPhong(room.getTenPhong().trim());
        if (room.getSoPhong() != null) r.setSoPhong(room.getSoPhong().trim());
        if (room.getTrangThai() != null) r.setTrangThai(room.getTrangThai().trim());
        if (room.getIdCk() != null) {
            r.setIdCk(room.getIdCk());
            Specialty sp = store.specialties.get(room.getIdCk());
            if (sp != null) r.setTenChuyenKhoa(sp.getTenCk());
        }
        return r;
    }

    public void deleteRoom(String idPk) {
        if (idPk == null || idPk.isBlank()) return;

        boolean hasExams = store.examinations.values().stream()
                .anyMatch(ex -> idPk.equals(ex.getIdPk()));
        if (hasExams) {
            throw new IllegalStateException("Không thể xóa phòng khám " + idPk + " vì đang có phiếu khám liên kết!");
        }

        store.rooms.remove(idPk);
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

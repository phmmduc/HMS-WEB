package com.hms.mock;

import com.hms.dto.StaffFormDto;
import com.hms.model.*;
import com.hms.service.DepartmentRoomDbService;
import com.hms.service.StaffDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MockStaffService {

    private final MockDataStore store;

    @Autowired(required = false)
    private StaffDbService staffDbService;

    @Autowired(required = false)
    private DepartmentRoomDbService departmentRoomDbService;

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

    public List<Staff> getDoctorsByRoom(String idPk) {
        if (idPk == null || idPk.isBlank()) return Collections.emptyList();
        return getDoctors().stream()
                .filter(s -> idPk.equalsIgnoreCase(s.getIdPk()))
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
                Staff.normalizeTrangThai(dto.getTrangThai()),
                dto.getSpecialtyDetail() != null ? dto.getSpecialtyDetail().trim() : "",
                dto.getIdPk() != null && !dto.getIdPk().isBlank() ? dto.getIdPk().trim() : null
        );
        store.staffMembers.put(idNv, staff);
        if (staffDbService != null) {
            try {
                staffDbService.saveStaff(staff);
            } catch (Exception e) {
                // Log and allow graceful operation
            }
        }
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
        if (dto.getTrangThai() != null && !dto.getTrangThai().isBlank()) {
            staff.setTrangThai(Staff.normalizeTrangThai(dto.getTrangThai()));
        }
        if (dto.getSpecialtyDetail() != null) staff.setSpecialtyDetail(dto.getSpecialtyDetail().trim());
        if (dto.getIdPk() != null) {
            staff.setIdPk(dto.getIdPk().isBlank() ? null : dto.getIdPk().trim());
        }

        if (staffDbService != null) {
            try {
                staffDbService.saveStaff(staff);
            } catch (Exception e) {
                // Log and allow graceful operation
            }
        }

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
        if (staffDbService != null) {
            try { staffDbService.deleteStaff(idNv); } catch (Exception ignored) {}
        }
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
        if (departmentRoomDbService != null) {
            try { departmentRoomDbService.saveSpecialty(sp); } catch (Exception ignored) {}
        }
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
        if (departmentRoomDbService != null) {
            try { departmentRoomDbService.saveSpecialty(sp); } catch (Exception ignored) {}
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
        if (departmentRoomDbService != null) {
            try { departmentRoomDbService.deleteSpecialty(idCk); } catch (Exception ignored) {}
        }
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
        if (departmentRoomDbService != null) {
            try { departmentRoomDbService.saveRoom(room); } catch (Exception ignored) {}
        }
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
        if (departmentRoomDbService != null) {
            try { departmentRoomDbService.saveRoom(r); } catch (Exception ignored) {}
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
        if (departmentRoomDbService != null) {
            try { departmentRoomDbService.deleteRoom(idPk); } catch (Exception ignored) {}
        }
    }

    public List<LabType> getAllLabTypes() {
        return new ArrayList<>(store.labTypes.values()).stream()
                .sorted((a, b) -> a.getIdLxn().compareTo(b.getIdLxn()))
                .collect(Collectors.toList());
    }

    public List<LabType> getActiveLabTypes() {
        return getAllLabTypes().stream()
                .filter(lt -> lt.getTrangThai() == null || "Hoạt động".equalsIgnoreCase(lt.getTrangThai()))
                .collect(Collectors.toList());
    }

    public LabType getLabTypeById(String idLxn) {
        if (idLxn == null) return null;
        return store.labTypes.get(idLxn.trim().toUpperCase());
    }

    public LabType createLabType(LabType lt) {
        if (lt.getTenLxn() == null || lt.getTenLxn().isBlank()) {
            throw new IllegalArgumentException("Tên loại xét nghiệm không được để trống!");
        }
        if (lt.getChiPhi() == null || lt.getChiPhi() < 0) {
            throw new IllegalArgumentException("Chi phí xét nghiệm phải lớn hơn hoặc bằng 0!");
        }

        String id = (lt.getIdLxn() != null && !lt.getIdLxn().isBlank())
                ? lt.getIdLxn().trim().toUpperCase()
                : store.nextLabTypeId();

        if (store.labTypes.containsKey(id)) {
            throw new IllegalStateException("Mã loại xét nghiệm [" + id + "] đã tồn tại trong hệ thống!");
        }

        String status = (lt.getTrangThai() != null && !lt.getTrangThai().isBlank())
                ? lt.getTrangThai().trim()
                : "Hoạt động";

        LabType created = new LabType(id, lt.getTenLxn().trim(), lt.getChiPhi(), status);
        store.labTypes.put(id, created);
        return created;
    }

    public void deleteLabType(String idLxn) {
        if (idLxn == null || idLxn.isBlank()) return;
        String targetId = idLxn.trim().toUpperCase();

        boolean isReferenced = store.labResults.values().stream()
                .anyMatch(lr -> targetId.equalsIgnoreCase(lr.getIdLxn()));
        if (isReferenced) {
            throw new IllegalStateException("Không thể xóa loại xét nghiệm " + targetId + " vì đã được sử dụng trong phiếu chỉ định / kết quả xét nghiệm!");
        }

        store.labTypes.remove(targetId);
    }

    public LabType toggleLabTypeStatus(String idLxn) {
        if (idLxn == null) return null;
        LabType lt = store.labTypes.get(idLxn.trim().toUpperCase());
        if (lt != null) {
            if ("Hoạt động".equalsIgnoreCase(lt.getTrangThai())) {
                lt.setTrangThai("Ngừng hoạt động");
            } else {
                lt.setTrangThai("Hoạt động");
            }
        }
        return lt;
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

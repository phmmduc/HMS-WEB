package com.hms.service;

import com.hms.entity.ChuyenKhoa;
import com.hms.entity.PhongKham;
import com.hms.model.ClinicRoom;
import com.hms.model.Specialty;
import com.hms.repository.ChuyenKhoaRepository;
import com.hms.repository.PhongKhamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DepartmentRoomDbService {

    private final ChuyenKhoaRepository chuyenKhoaRepo;
    private final PhongKhamRepository phongKhamRepo;
    private final EntityModelMapper mapper;

    public DepartmentRoomDbService(ChuyenKhoaRepository chuyenKhoaRepo,
                                  PhongKhamRepository phongKhamRepo,
                                  EntityModelMapper mapper) {
        this.chuyenKhoaRepo = chuyenKhoaRepo;
        this.phongKhamRepo = phongKhamRepo;
        this.mapper = mapper;
    }

    // Specialties
    public List<Specialty> findAllSpecialties() {
        return chuyenKhoaRepo.findAll().stream()
                .map(mapper::toSpecialtyModel)
                .collect(Collectors.toList());
    }

    public Optional<Specialty> findSpecialtyById(String idCk) {
        if (idCk == null) return Optional.empty();
        return chuyenKhoaRepo.findById(idCk).map(mapper::toSpecialtyModel);
    }

    @Transactional
    public Specialty saveSpecialty(Specialty specialty) {
        if (specialty == null || specialty.getIdCk() == null) return null;
        ChuyenKhoa ck = mapper.toChuyenKhoaEntity(specialty);
        chuyenKhoaRepo.save(ck);
        return specialty;
    }

    @Transactional
    public void deleteSpecialty(String idCk) {
        if (idCk != null) {
            chuyenKhoaRepo.deleteById(idCk);
        }
    }

    // Rooms
    public List<ClinicRoom> findAllRooms() {
        return phongKhamRepo.findAll().stream()
                .map(mapper::toClinicRoomModel)
                .collect(Collectors.toList());
    }

    public Optional<ClinicRoom> findRoomById(String idPk) {
        if (idPk == null) return Optional.empty();
        return phongKhamRepo.findById(idPk).map(mapper::toClinicRoomModel);
    }

    @Transactional
    public ClinicRoom saveRoom(ClinicRoom room) {
        if (room == null || room.getIdPk() == null) return null;
        ChuyenKhoa ck = null;
        if (room.getIdCk() != null) {
            ck = chuyenKhoaRepo.findById(room.getIdCk()).orElse(null);
        }
        PhongKham pk = mapper.toPhongKhamEntity(room, ck);
        phongKhamRepo.save(pk);
        return findRoomById(room.getIdPk()).orElse(room);
    }

    @Transactional
    public void deleteRoom(String idPk) {
        if (idPk != null) {
            phongKhamRepo.deleteById(idPk);
        }
    }
}


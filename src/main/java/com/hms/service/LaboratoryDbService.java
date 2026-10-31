package com.hms.service;

import com.hms.entity.LoaiXetNghiem;
import com.hms.model.LabResult;
import com.hms.model.LabType;
import com.hms.repository.KetQuaXetNghiemRepository;
import com.hms.repository.LoaiXetNghiemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LaboratoryDbService {

    private final LoaiXetNghiemRepository loaiXnRepo;
    private final KetQuaXetNghiemRepository ketQuaRepo;
    private final EntityModelMapper mapper;

    public LaboratoryDbService(LoaiXetNghiemRepository loaiXnRepo,
                              KetQuaXetNghiemRepository ketQuaRepo,
                              EntityModelMapper mapper) {
        this.loaiXnRepo = loaiXnRepo;
        this.ketQuaRepo = ketQuaRepo;
        this.mapper = mapper;
    }

    public List<LabType> findAllLabTypes() {
        return loaiXnRepo.findAll().stream()
                .map(mapper::toLabTypeModel)
                .collect(Collectors.toList());
    }

    public Optional<LabType> findLabTypeById(String id) {
        if (id == null) return Optional.empty();
        return loaiXnRepo.findById(id).map(mapper::toLabTypeModel);
    }

    @Transactional
    public LabType saveLabType(LabType labType) {
        if (labType == null || labType.getIdLoaiXn() == null) return null;
        LoaiXetNghiem entity = mapper.toLoaiXetNghiemEntity(labType);
        loaiXnRepo.save(entity);
        return labType;
    }

    @Transactional
    public void deleteLabType(String id) {
        if (id != null) {
            loaiXnRepo.deleteById(id);
        }
    }

    public List<LabResult> findResultsByExamination(String idPhieuKham) {
        if (idPhieuKham == null) return List.of();
        return ketQuaRepo.findByPhieuKhamIdPhieukham(idPhieuKham).stream()
                .map(mapper::toLabResultModel)
                .collect(Collectors.toList());
    }
}


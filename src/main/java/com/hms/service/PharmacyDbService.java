package com.hms.service;

import com.hms.entity.LoThuoc;
import com.hms.entity.Thuoc;
import com.hms.model.Medicine;
import com.hms.model.MedicineLot;
import com.hms.repository.ChiTietDonThuocRepository;
import com.hms.repository.DonThuocRepository;
import com.hms.repository.LoThuocRepository;
import com.hms.repository.ThuocRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PharmacyDbService {

    private final ThuocRepository thuocRepo;
    private final LoThuocRepository loThuocRepo;
    private final DonThuocRepository donThuocRepo;
    private final ChiTietDonThuocRepository chiTietDonThuocRepo;
    private final EntityModelMapper mapper;

    public PharmacyDbService(ThuocRepository thuocRepo,
                            LoThuocRepository loThuocRepo,
                            DonThuocRepository donThuocRepo,
                            ChiTietDonThuocRepository chiTietDonThuocRepo,
                            EntityModelMapper mapper) {
        this.thuocRepo = thuocRepo;
        this.loThuocRepo = loThuocRepo;
        this.donThuocRepo = donThuocRepo;
        this.chiTietDonThuocRepo = chiTietDonThuocRepo;
        this.mapper = mapper;
    }

    public List<Medicine> findAllMedicines() {
        List<Thuoc> list = thuocRepo.findAll();
        List<Medicine> result = new ArrayList<>();
        for (Thuoc t : list) {
            List<LoThuoc> lots = loThuocRepo.findByIdThuoc(t.getIdThuoc());
            result.add(mapper.toMedicineModel(t, lots));
        }
        return result;
    }

    public Optional<Medicine> findMedicineById(String idThuoc) {
        if (idThuoc == null) return Optional.empty();
        return thuocRepo.findById(idThuoc).map(t -> {
            List<LoThuoc> lots = loThuocRepo.findByIdThuoc(t.getIdThuoc());
            return mapper.toMedicineModel(t, lots);
        });
    }

    @Transactional
    public Medicine saveMedicine(Medicine medicine) {
        if (medicine == null || medicine.getIdThuoc() == null) return null;
        Thuoc t = mapper.toThuocEntity(medicine);
        thuocRepo.save(t);
        return findMedicineById(medicine.getIdThuoc()).orElse(medicine);
    }

    @Transactional
    public void deleteMedicine(String idThuoc) {
        if (idThuoc == null) return;
        loThuocRepo.deleteByIdThuoc(idThuoc);
        thuocRepo.deleteById(idThuoc);
    }

    public List<MedicineLot> findAllLots() {
        return loThuocRepo.findAll().stream()
                .map(mapper::toMedicineLotModel)
                .collect(Collectors.toList());
    }

    public Optional<MedicineLot> findLotById(String idLo) {
        if (idLo == null) return Optional.empty();
        return loThuocRepo.findById(idLo).map(mapper::toMedicineLotModel);
    }

    @Transactional
    public MedicineLot saveLot(MedicineLot lot) {
        if (lot == null || lot.getIdLo() == null) return null;
        Thuoc thuoc = null;
        if (lot.getIdThuoc() != null) {
            thuoc = thuocRepo.findById(lot.getIdThuoc()).orElse(null);
        }
        LoThuoc entity = mapper.toLoThuocEntity(lot, thuoc);
        loThuocRepo.save(entity);

        // Update medicine total stock
        if (thuoc != null) {
            Integer totalStock = loThuocRepo.sumStockByMedicine(thuoc.getIdThuoc());
            thuoc.setSoLuongTonKho(totalStock != null ? totalStock : 0);
            thuocRepo.save(thuoc);
        }

        return findLotById(lot.getIdLo()).orElse(lot);
    }

    @Transactional
    public void deleteLot(String idLo) {
        if (idLo == null) return;
        loThuocRepo.findById(idLo).ifPresent(lot -> {
            String idThuoc = lot.getThuoc() != null ? lot.getThuoc().getIdThuoc() : null;
            loThuocRepo.delete(lot);
            if (idThuoc != null) {
                Integer totalStock = loThuocRepo.sumStockByMedicine(idThuoc);
                thuocRepo.findById(idThuoc).ifPresent(t -> {
                    t.setSoLuongTonKho(totalStock != null ? totalStock : 0);
                    thuocRepo.save(t);
                });
            }
        });
    }

    @Transactional
    public int deleteExpiredLots(LocalDate date) {
        List<LoThuoc> expired = loThuocRepo.findByHanSuDungBefore(date);
        int count = expired.size();
        Set<String> affectedThuocIds = new HashSet<>();
        for (LoThuoc l : expired) {
            if (l.getThuoc() != null) affectedThuocIds.add(l.getThuoc().getIdThuoc());
            loThuocRepo.delete(l);
        }
        for (String idThuoc : affectedThuocIds) {
            Integer totalStock = loThuocRepo.sumStockByMedicine(idThuoc);
            thuocRepo.findById(idThuoc).ifPresent(t -> {
                t.setSoLuongTonKho(totalStock != null ? totalStock : 0);
                thuocRepo.save(t);
            });
        }
        return count;
    }
}

package com.hms.mock;

import com.hms.model.Medicine;
import com.hms.model.MedicineLot;
import com.hms.model.Prescription;
import com.hms.model.PrescriptionDetail;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MockPharmacyService {

    private final MockDataStore store;

    public MockPharmacyService(MockDataStore store) {
        this.store = store;
    }

    public List<Medicine> getAllMedicines() {
        return new ArrayList<>(store.medicines.values()).stream()
                .sorted((a, b) -> a.getIdThuoc().compareTo(b.getIdThuoc()))
                .collect(Collectors.toList());
    }

    public List<MedicineLot> getAllMedicineLots() {
        return new ArrayList<>(store.medicineLots.values()).stream()
                .sorted((a, b) -> a.getHanSuDung().compareTo(b.getHanSuDung()))
                .collect(Collectors.toList());
    }

    public List<Prescription> getAllPrescriptions() {
        store.linkAll();
        return new ArrayList<>(store.prescriptions.values()).stream()
                .sorted((a, b) -> b.getIdDt().compareTo(a.getIdDt()))
                .collect(Collectors.toList());
    }

    public Prescription getPrescriptionById(String id) {
        if (id == null) return null;
        store.linkAll();
        return store.prescriptions.get(id);
    }

    public Prescription getPrescriptionByExamId(String examId) {
        if (examId == null) return null;
        store.linkAll();
        return store.prescriptions.values().stream()
                .filter(p -> examId.equals(p.getIdPhieukham()))
                .findFirst()
                .orElse(null);
    }

    public synchronized boolean dispensePrescription(String prescriptionId) {
        Prescription rx = store.prescriptions.get(prescriptionId);
        if (rx == null || "Đã cấp phát".equalsIgnoreCase(rx.getTrangThai())) {
            return false;
        }

        List<PrescriptionDetail> details = rx.getItems();
        if (details == null || details.isEmpty()) {
            details = store.prescriptionDetails.values().stream()
                    .filter(d -> prescriptionId.equals(d.getIdDt()))
                    .collect(Collectors.toList());
        }

        // Deduct inventory
        for (PrescriptionDetail d : details) {
            String medId = d.getIdThuoc();
            int qtyNeeded = d.getSoLuong();

            Medicine med = store.medicines.get(medId);
            if (med != null) {
                int curStock = med.getSoLuongTonKho() != null ? med.getSoLuongTonKho() : 0;
                med.setSoLuongTonKho(Math.max(0, curStock - qtyNeeded));
            }

            // Deduct lots by FEFO (First Expired First Out)
            List<MedicineLot> lots = store.medicineLots.values().stream()
                    .filter(lot -> medId.equals(lot.getIdThuoc()) && lot.getSoLuongTon() > 0)
                    .sorted(Comparator.comparing(MedicineLot::getHanSuDung))
                    .collect(Collectors.toList());

            int remaining = qtyNeeded;
            for (MedicineLot lot : lots) {
                if (remaining <= 0) break;
                int lotStock = lot.getSoLuongTon();
                int deduct = Math.min(lotStock, remaining);
                lot.setSoLuongTon(lotStock - deduct);
                remaining -= deduct;
            }
        }

        rx.setTrangThai("Đã cấp phát");
        store.linkAll();
        return true;
    }

    public List<Medicine> searchMedicines(String keyword) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        return getAllMedicines().stream()
                .filter(m -> {
                    if (kw.isEmpty()) return true;
                    String target = (m.getIdThuoc() + " " + m.getTenThuoc() + " " + (m.getNhaSanXuat() != null ? m.getNhaSanXuat() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }

    public List<Prescription> searchPrescriptions(String keyword, String status) {
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        store.linkAll();
        return getAllPrescriptions().stream()
                .filter(p -> {
                    if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status) && !status.equalsIgnoreCase(p.getTrangThai())) {
                        return false;
                    }
                    if (kw.isEmpty()) return true;
                    String target = (p.getIdDt() + " " + p.getIdPhieukham() + " " + (p.getPatientName() != null ? p.getPatientName() : "") + " " + (p.getPatientId() != null ? p.getPatientId() : "")).toLowerCase();
                    return target.contains(kw);
                })
                .collect(Collectors.toList());
    }
}


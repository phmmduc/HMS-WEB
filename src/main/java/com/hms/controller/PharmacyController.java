package com.hms.controller;

import com.hms.dto.MedicineFormDto;
import com.hms.dto.MedicineLotFormDto;
import com.hms.mock.MockPharmacyService;
import com.hms.model.Medicine;
import com.hms.model.MedicineLot;
import com.hms.model.Prescription;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pharmacy")
public class PharmacyController {

    private final MockPharmacyService pharmacyService;

    public PharmacyController(MockPharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String tab,
                        Model model) {
        String activeTab = tab != null && !tab.isEmpty() ? tab : "prescriptions";

        List<Medicine> medicines = pharmacyService.searchMedicines(search, status);
        List<MedicineLot> lots = "lots".equals(activeTab)
                ? pharmacyService.searchLots(search, status)
                : pharmacyService.getAllMedicineLots();
        List<Prescription> prescriptions = pharmacyService.searchPrescriptions(search, status);

        long pendingOrders = prescriptions.stream().filter(p -> "Chờ cấp phát".equalsIgnoreCase(p.getTrangThai())).count();
        long dispensedOrders = prescriptions.stream().filter(p -> "Đã cấp phát".equalsIgnoreCase(p.getTrangThai())).count();
        long expiredLotsCount = lots.stream().filter(MedicineLot::isExpired).count();
        int totalMedicines = pharmacyService.getAllMedicines().size();

        model.addAttribute("activeNav", "pharmacy");
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("medicines", medicines);
        model.addAttribute("allMedicines", pharmacyService.getAllMedicines());
        model.addAttribute("lots", lots);
        model.addAttribute("prescriptions", prescriptions);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("dispensedOrders", dispensedOrders);
        model.addAttribute("totalMedicines", totalMedicines);
        model.addAttribute("expiredLotsCount", expiredLotsCount);
        model.addAttribute("search", search);
        model.addAttribute("status", status);

        model.addAttribute("medicineForm", new MedicineFormDto());
        model.addAttribute("lotForm", new MedicineLotFormDto());

        return "pharmacy/index";
    }

    @PostMapping("/medicine/create")
    public String createMedicine(@ModelAttribute MedicineFormDto form, RedirectAttributes ra) {
        try {
            Medicine med = pharmacyService.createMedicine(form);
            ra.addFlashAttribute("successMessage", "Thêm mới thuốc [" + med.getIdThuoc() + " - " + med.getTenThuoc() + "] vào danh mục thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể thêm thuốc: " + e.getMessage());
        }
        return "redirect:/pharmacy?tab=medicines";
    }

    @PostMapping("/medicine/delete")
    public String deleteMedicine(@RequestParam String idThuoc, RedirectAttributes ra) {
        try {
            String msg = pharmacyService.deleteMedicine(idThuoc);
            ra.addFlashAttribute("successMessage", msg);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi xóa thuốc: " + e.getMessage());
        }
        return "redirect:/pharmacy?tab=medicines";
    }

    @PostMapping("/lot/create")
    public String createLot(@ModelAttribute MedicineLotFormDto form, RedirectAttributes ra) {
        try {
            MedicineLot lot = pharmacyService.createLot(form);
            ra.addFlashAttribute("successMessage", "Nhập thành công lô thuốc [" + lot.getSoLo() + "] cho " + lot.getTenThuoc() + " (SL: " + lot.getSoLuongNhap() + "). Tổng tồn kho thuốc đã được đồng bộ.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể nhập lô thuốc: " + e.getMessage());
        }
        return "redirect:/pharmacy?tab=lots";
    }

    @PostMapping("/lot/delete")
    public String deleteLot(@RequestParam String idLo, RedirectAttributes ra) {
        try {
            String msg = pharmacyService.deleteExpiredLot(idLo);
            ra.addFlashAttribute("successMessage", msg);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể loại bỏ lô thuốc: " + e.getMessage());
        }
        return "redirect:/pharmacy?tab=lots";
    }

    @PostMapping("/dispense")
    public String dispense(@RequestParam String idDt, RedirectAttributes ra) {
        boolean success = pharmacyService.dispensePrescription(idDt);
        if (success) {
            ra.addFlashAttribute("successMessage", "Đã xác nhận cấp phát đơn thuốc " + idDt + " thành công (đã xuất kho theo lô FEFO).");
        } else {
            ra.addFlashAttribute("errorMessage", "Không thể cấp phát đơn thuốc " + idDt + " (có thể đã cấp phát hoặc không tìm thấy).");
        }
        return "redirect:/pharmacy?tab=prescriptions";
    }
}


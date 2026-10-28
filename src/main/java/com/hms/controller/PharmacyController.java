package com.hms.controller;

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
        List<Medicine> medicines = pharmacyService.searchMedicines(search);
        List<MedicineLot> lots = pharmacyService.getAllMedicineLots();
        List<Prescription> prescriptions = pharmacyService.searchPrescriptions(search, status);

        long pendingOrders = prescriptions.stream().filter(p -> "Chờ cấp phát".equalsIgnoreCase(p.getTrangThai())).count();
        long dispensedOrders = prescriptions.stream().filter(p -> "Đã cấp phát".equalsIgnoreCase(p.getTrangThai())).count();
        int totalMedicines = medicines.size();

        model.addAttribute("activeNav", "pharmacy");
        model.addAttribute("activeTab", tab != null ? tab : "prescriptions");
        model.addAttribute("medicines", medicines);
        model.addAttribute("lots", lots);
        model.addAttribute("prescriptions", prescriptions);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("dispensedOrders", dispensedOrders);
        model.addAttribute("totalMedicines", totalMedicines);
        model.addAttribute("search", search);
        model.addAttribute("status", status);

        return "pharmacy/index";
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


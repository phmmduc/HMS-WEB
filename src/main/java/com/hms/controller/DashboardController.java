package com.hms.controller;

import com.hms.mock.*;
import com.hms.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final MockPatientService patientService;
    private final MockExaminationService examinationService;
    private final MockPharmacyService pharmacyService;
    private final MockBillingService billingService;

    public DashboardController(MockPatientService patientService,
                               MockExaminationService examinationService,
                               MockPharmacyService pharmacyService,
                               MockBillingService billingService) {
        this.patientService = patientService;
        this.examinationService = examinationService;
        this.pharmacyService = pharmacyService;
        this.billingService = billingService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        // Enforce Manager role for dashboard
        session.setAttribute(GlobalControllerAdvice.SESSION_ROLE, Role.MANAGER);

        List<Patient> patients = patientService.getAllPatients();
        List<Examination> exams = examinationService.getAllExaminations();
        List<Medicine> medicines = pharmacyService.getAllMedicines();
        List<MedicineLot> lots = pharmacyService.getAllMedicineLots();

        long todayPatientsCount = exams.stream()
                .filter(x -> LocalDate.now().toString().equals(x.getNgayDky()))
                .map(Examination::getIdBn)
                .distinct()
                .count();

        long pendingExams = exams.stream()
                .filter(x -> !"Hoàn tất".equalsIgnoreCase(x.getTrangThai()) && !"Đã khám".equalsIgnoreCase(x.getTrangThai()))
                .count();

        long totalRevenue = billingService.calculateTotalRevenue();

        int totalStock = medicines.stream()
                .mapToInt(m -> m.getSoLuongTonKho() != null ? m.getSoLuongTonKho() : 0)
                .sum();

        // Expiring lots
        List<MedicineLot> expiringLots = lots.stream()
                .filter(l -> l.isExpired() || l.isExpiringSoon())
                .collect(Collectors.toList());

        // Recent exams
        List<Examination> recentExams = exams.stream().limit(5).collect(Collectors.toList());

        model.addAttribute("activeNav", "dashboard");
        model.addAttribute("totalPatients", patients.size());
        model.addAttribute("todayPatientsCount", todayPatientsCount > 0 ? todayPatientsCount : patients.size());
        model.addAttribute("totalExams", exams.size());
        model.addAttribute("pendingExams", pendingExams);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("recentExams", recentExams);
        model.addAttribute("expiringLots", expiringLots);

        return "dashboard/index";
    }
}


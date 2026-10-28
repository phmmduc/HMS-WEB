package com.hms.controller;

import com.hms.mock.MockBillingService;
import com.hms.mock.MockExaminationService;
import com.hms.mock.MockPatientService;
import com.hms.mock.MockPharmacyService;
import com.hms.model.Examination;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final MockPatientService patientService;
    private final MockExaminationService examinationService;
    private final MockPharmacyService pharmacyService;
    private final MockBillingService billingService;

    public ReportController(MockPatientService patientService,
                            MockExaminationService examinationService,
                            MockPharmacyService pharmacyService,
                            MockBillingService billingService) {
        this.patientService = patientService;
        this.examinationService = examinationService;
        this.pharmacyService = pharmacyService;
        this.billingService = billingService;
    }

    @GetMapping
    public String index(Model model) {
        long totalPatients = patientService.getAllPatients().size();
        long totalExams = examinationService.getAllExaminations().size();
        long totalPaid = billingService.calculateTotalRevenue();
        long totalDue = billingService.calculatePendingRevenue();
        long totalMedicines = pharmacyService.getAllMedicines().size();

        model.addAttribute("activeNav", "reports");
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalExams", totalExams);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalDue", totalDue);
        model.addAttribute("totalMedicines", totalMedicines);

        return "reports/index";
    }

    @GetMapping("/export-csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"hms-phieu-kham-" + LocalDate.now() + ".csv\"");

        PrintWriter writer = response.getWriter();
        // UTF-8 BOM for Excel
        writer.write('\uFEFF');
        writer.println("Mã phiếu,Mã BN,Tên bệnh nhân,Phòng khám,Bác sĩ,Ngày đăng ký,Triệu chứng,Trạng thái");

        List<Examination> list = examinationService.getAllExaminations();
        for (Examination ex : list) {
            writer.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                    ex.getIdPhieukham(),
                    ex.getIdBn(),
                    ex.getPatientName() != null ? ex.getPatientName() : "",
                    ex.getRoomName() != null ? ex.getRoomName() : "",
                    ex.getDoctorName() != null ? ex.getDoctorName() : "",
                    ex.getNgayDky(),
                    ex.getTrieuChung() != null ? ex.getTrieuChung().replace("\"", "\"\"") : "",
                    ex.getTrangThai()
            ));
        }
        writer.flush();
    }
}


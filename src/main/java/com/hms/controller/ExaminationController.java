package com.hms.controller;

import com.hms.dto.DoctorExamDto;
import com.hms.mock.MockExaminationService;
import com.hms.mock.MockPatientService;
import com.hms.mock.MockPharmacyService;
import com.hms.mock.MockStaffService;
import com.hms.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/examination")
public class ExaminationController {

    private final MockExaminationService examinationService;
    private final MockPatientService patientService;
    private final MockPharmacyService pharmacyService;
    private final MockStaffService staffService;

    public ExaminationController(MockExaminationService examinationService,
                                 MockPatientService patientService,
                                 MockPharmacyService pharmacyService,
                                 MockStaffService staffService) {
        this.examinationService = examinationService;
        this.patientService = patientService;
        this.pharmacyService = pharmacyService;
        this.staffService = staffService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String roomId,
                        Model model) {
        List<Examination> exams = examinationService.searchExaminations(search, status, roomId);
        List<ClinicRoom> rooms = staffService.getAllRooms();
        List<Diagnosis> diagnoses = staffService.getAllDiagnoses();
        List<LabType> labTypes = staffService.getAllLabTypes();
        List<Medicine> medicines = pharmacyService.getAllMedicines();
        List<Prescription> prescriptions = examinationService.getRecentPrescriptionsForDoctor(search);

        long waitingExams = exams.stream().filter(e -> "Chờ khám".equalsIgnoreCase(e.getTrangThai())).count();
        long labWaitingExams = exams.stream().filter(e -> "Chờ xét nghiệm".equalsIgnoreCase(e.getTrangThai())).count();
        long pendingTotal = exams.stream().filter(e -> !"Hoàn tất".equalsIgnoreCase(e.getTrangThai())).count();

        model.addAttribute("activeNav", "examination");
        model.addAttribute("exams", exams);
        model.addAttribute("rooms", rooms);
        model.addAttribute("diagnoses", diagnoses);
        model.addAttribute("labTypes", labTypes);
        model.addAttribute("medicines", medicines);
        model.addAttribute("prescriptions", prescriptions);

        model.addAttribute("waitingExams", waitingExams);
        model.addAttribute("labWaitingExams", labWaitingExams);
        model.addAttribute("pendingTotal", pendingTotal);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("roomId", roomId);

        model.addAttribute("examForm", new DoctorExamDto());

        return "examination/index";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable String id, Model model, RedirectAttributes ra) {
        Examination exam = examinationService.getExaminationById(id);
        if (exam == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy phiếu khám " + id);
            return "redirect:/examination";
        }

        Patient patient = patientService.getPatientById(exam.getIdBn());
        List<Diagnosis> diagnoses = staffService.getAllDiagnoses();
        List<LabType> labTypes = staffService.getAllLabTypes();
        List<Medicine> medicines = pharmacyService.getAllMedicines();
        List<MedicalRecord> medicalRecords = patientService.getMedicalRecordsByPatient(exam.getIdBn());

        model.addAttribute("activeNav", "examination");
        model.addAttribute("exam", exam);
        model.addAttribute("patient", patient);
        model.addAttribute("diagnoses", diagnoses);
        model.addAttribute("labTypes", labTypes);
        model.addAttribute("medicines", medicines);
        model.addAttribute("medicalRecords", medicalRecords);

        DoctorExamDto dto = new DoctorExamDto();
        dto.setIdPhieukham(exam.getIdPhieukham());
        dto.setThoiGianKham(exam.getThoiGianKham());
        dto.setIdCd(exam.getIdCd());
        dto.setTrieuChung(exam.getTrieuChung());
        dto.setKetLuan(exam.getKetLuan());
        model.addAttribute("examForm", dto);

        return "examination/detail";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute DoctorExamDto form, RedirectAttributes ra) {
        Examination exam = examinationService.updateDoctorExamination(form);
        if (exam != null) {
            ra.addFlashAttribute("successMessage", "Đã cập nhật kết quả khám & đơn thuốc cho phiếu: " + exam.getIdPhieukham());
        }
        return "redirect:/examination";
    }
}


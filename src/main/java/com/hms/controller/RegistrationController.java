package com.hms.controller;

import com.hms.dto.ExamFormDto;
import com.hms.dto.PatientFormDto;
import com.hms.mock.MockExaminationService;
import com.hms.mock.MockPatientService;
import com.hms.mock.MockStaffService;
import com.hms.model.ClinicRoom;
import com.hms.model.Examination;
import com.hms.model.Patient;
import com.hms.model.Staff;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/registration")
public class RegistrationController {

    private final MockPatientService patientService;
    private final MockExaminationService examinationService;
    private final MockStaffService staffService;

    public RegistrationController(MockPatientService patientService,
                                  MockExaminationService examinationService,
                                  MockStaffService staffService) {
        this.patientService = patientService;
        this.examinationService = examinationService;
        this.staffService = staffService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String tab,
                        Model model) {
        List<Patient> patients = patientService.searchPatients(search, status);
        List<Examination> exams = examinationService.getAllExaminations();
        List<Staff> doctors = staffService.getDoctors();
        List<ClinicRoom> rooms = staffService.getAllRooms();

        model.addAttribute("activeNav", "registration");
        model.addAttribute("activeTab", tab != null ? tab : "exams");
        model.addAttribute("patients", patients);
        model.addAttribute("exams", exams);
        model.addAttribute("doctors", doctors);
        model.addAttribute("rooms", rooms);
        model.addAttribute("search", search);
        model.addAttribute("status", status);

        // Blank form objects for modals
        model.addAttribute("patientForm", new PatientFormDto());
        model.addAttribute("examForm", new ExamFormDto());

        return "registration/index";
    }

    @PostMapping("/patient")
    public String savePatient(@ModelAttribute PatientFormDto form, RedirectAttributes ra) {
        Patient p = patientService.savePatient(form);
        ra.addFlashAttribute("successMessage", "Đã lưu hồ sơ bệnh nhân: " + p.getHoTen() + " (" + p.getIdBn() + ")");
        return "redirect:/registration?tab=patients";
    }

    @PostMapping("/exam")
    public String createExam(@ModelAttribute ExamFormDto form, RedirectAttributes ra) {
        Examination ex = examinationService.createExamination(form);
        ra.addFlashAttribute("successMessage", "Đã tạo phiếu khám thành công: " + ex.getIdPhieukham());
        return "redirect:/registration?tab=exams";
    }
}


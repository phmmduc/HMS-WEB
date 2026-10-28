package com.hms.controller;

import com.hms.mock.MockExaminationService;
import com.hms.mock.MockPatientService;
import com.hms.model.Examination;
import com.hms.model.MedicalRecord;
import com.hms.model.Patient;
import com.hms.model.Staff;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/medical-records")
public class MedicalRecordController {

    private final MockPatientService patientService;
    private final MockExaminationService examinationService;

    public MedicalRecordController(MockPatientService patientService,
                                  MockExaminationService examinationService) {
        this.patientService = patientService;
        this.examinationService = examinationService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String selectedId,
                        HttpSession session,
                        Model model) {
        Staff curStaff = (Staff) session.getAttribute(GlobalControllerAdvice.SESSION_STAFF);
        List<Patient> patients = patientService.getPatientsForStaff(curStaff, search, "all");

        Patient selectedPatient = null;
        List<MedicalRecord> records = Collections.emptyList();
        List<Examination> exams = Collections.emptyList();

        if (selectedId != null && !selectedId.isEmpty()) {
            selectedPatient = patients.stream()
                    .filter(p -> selectedId.equalsIgnoreCase(p.getIdBn()))
                    .findFirst()
                    .orElse(null);
        }
        if (selectedPatient == null && !patients.isEmpty()) {
            selectedPatient = patients.get(0);
        }

        if (selectedPatient != null) {
            String pId = selectedPatient.getIdBn();
            records = patientService.getMedicalRecordsByPatient(pId);
            exams = examinationService.getExaminationsForStaff(curStaff, null, null, null).stream()
                    .filter(ex -> pId.equals(ex.getIdBn()))
                    .sorted((a, b) -> b.getIdPhieukham().compareTo(a.getIdPhieukham()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("activeNav", "medical-records");
        model.addAttribute("patients", patients);
        model.addAttribute("selectedPatient", selectedPatient);
        model.addAttribute("records", records);
        model.addAttribute("exams", exams);
        model.addAttribute("search", search);

        return "medical-records/index";
    }
}


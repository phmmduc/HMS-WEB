package com.hms.controller;

import com.hms.mock.MockExaminationService;
import com.hms.mock.MockPatientService;
import com.hms.model.Examination;
import com.hms.model.MedicalRecord;
import com.hms.model.Patient;
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
                        Model model) {
        List<Patient> patients = patientService.searchPatients(search, "all");

        Patient selectedPatient = null;
        List<MedicalRecord> records = Collections.emptyList();
        List<Examination> exams = Collections.emptyList();

        if (selectedId != null && !selectedId.isEmpty()) {
            selectedPatient = patientService.getPatientById(selectedId);
        } else if (!patients.isEmpty()) {
            selectedPatient = patients.get(0);
        }

        if (selectedPatient != null) {
            String pId = selectedPatient.getIdBn();
            records = patientService.getMedicalRecordsByPatient(pId);
            exams = examinationService.getAllExaminations().stream()
                    .filter(ex -> pId.equals(ex.getIdBn()))
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


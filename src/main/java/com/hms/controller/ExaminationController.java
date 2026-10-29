package com.hms.controller;

import com.hms.dto.DoctorExamDto;
import com.hms.mock.MockExaminationService;
import com.hms.mock.MockPatientService;
import com.hms.mock.MockPharmacyService;
import com.hms.mock.MockStaffService;
import com.hms.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

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
                        HttpSession session,
                        Model model) {
        Staff cur = (Staff) session.getAttribute(GlobalControllerAdvice.SESSION_STAFF);
        List<Examination> exams = examinationService.getExaminationsForStaff(cur, search, status, roomId);

        List<ClinicRoom> rooms = staffService.getAllRooms();
        List<Diagnosis> diagnoses = staffService.getAllDiagnoses();
        List<LabType> labTypes = staffService.getAllLabTypes();
        List<Medicine> medicines = pharmacyService.getAllMedicines();
        List<Prescription> prescriptions = examinationService.getRecentPrescriptionsForDoctor(search);

        if (cur != null && "Bác sĩ".equalsIgnoreCase(cur.getVaiTro())) {
            final String docId = cur.getIdNv();
            prescriptions = prescriptions.stream()
                    .filter(p -> docId.equals(p.getIdNv()) || (p.getDoctorName() != null && p.getDoctorName().equals(cur.getHoTen())))
                    .collect(Collectors.toList());
        }

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
    public String detail(@PathVariable String id, HttpSession session, Model model, RedirectAttributes ra) {
        Examination exam = examinationService.getExaminationById(id);
        if (exam == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy phiếu khám " + id);
            return "redirect:/examination";
        }

        Staff cur = (Staff) session.getAttribute(GlobalControllerAdvice.SESSION_STAFF);
        if (cur != null && "Bác sĩ".equalsIgnoreCase(cur.getVaiTro())) {
            if (!examinationService.isDoctorAllowedToView(cur, exam)) {
                ra.addFlashAttribute("errorMessage", "Phiếu khám " + id + " thuộc phòng khám của bác sĩ khác. Bạn chỉ có quyền truy cập phiếu khám trong phòng khám được phân công.");
                return "redirect:/examination";
            }
        }

        Patient patient = patientService.getPatientById(exam.getIdBn());
        List<Diagnosis> diagnoses = staffService.getAllDiagnoses();
        List<LabType> labTypes = staffService.getActiveLabTypes();
        for (LabResult lr : exam.getLabResults()) {
            LabType lt = staffService.getLabTypeById(lr.getIdLxn());
            if (lt != null && !labTypes.contains(lt)) {
                labTypes.add(lt);
            }
        }
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
        dto.setIdNv(exam.getIdNv());
        model.addAttribute("examForm", dto);

        return "examination/detail";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute DoctorExamDto form, HttpSession session, RedirectAttributes ra) {
        if (form.getIdPhieukham() == null || form.getIdPhieukham().isBlank()) {
            ra.addFlashAttribute("errorMessage", "Mã phiếu khám không hợp lệ!");
            return "redirect:/examination";
        }
        if (form.getIdCd() == null || form.getIdCd().isBlank()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng chọn chẩn đoán mã ICD-10 trước khi lưu!");
            return "redirect:/examination/detail/" + form.getIdPhieukham();
        }
        if (form.getTrieuChung() == null || form.getTrieuChung().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập triệu chứng / khám lâm sàng của bệnh nhân!");
            return "redirect:/examination/detail/" + form.getIdPhieukham();
        }

        // Assign active logged in doctor if currentStaff is a doctor
        Staff currentStaff = (Staff) session.getAttribute(GlobalControllerAdvice.SESSION_STAFF);
        if (currentStaff != null && "Bác sĩ".equalsIgnoreCase(currentStaff.getVaiTro())) {
            form.setIdNv(currentStaff.getIdNv());
        }

        try {
            Examination exam = examinationService.updateDoctorExamination(form);
            if (exam != null) {
                ra.addFlashAttribute("successMessage", "Đã lưu kết quả khám & đơn thuốc thành công cho phiếu: " + exam.getIdPhieukham());
            } else {
                ra.addFlashAttribute("errorMessage", "Không tìm thấy phiếu khám để cập nhật!");
            }
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Lỗi lưu phiếu khám: " + ex.getMessage());
            return "redirect:/examination/detail/" + form.getIdPhieukham();
        }
        return "redirect:/examination";
    }
}

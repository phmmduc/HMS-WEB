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

import java.time.LocalDate;
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
                        jakarta.servlet.http.HttpSession session,
                        Model model) {
        String activeTab = tab != null && !tab.isBlank() ? tab.trim() : "exams";

        Staff curStaff = (Staff) session.getAttribute(GlobalControllerAdvice.SESSION_STAFF);
        List<Patient> patients;
        List<Examination> exams;

        if ("patients".equalsIgnoreCase(activeTab)) {
            patients = patientService.getPatientsForStaff(curStaff, search, status);
            exams = examinationService.getExaminationsForStaff(curStaff, null, null, null);
        } else {
            exams = examinationService.getExaminationsForStaff(curStaff, search, status, null);
            patients = patientService.getPatientsForStaff(curStaff, null, null);
        }

        // All patients list explicitly for modal dropdown
        List<Patient> allPatients = patientService.getAllPatients();
        List<Staff> doctors = staffService.getDoctors();
        List<ClinicRoom> rooms = staffService.getAllRooms();

        model.addAttribute("activeNav", "registration");
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("patients", patients);
        model.addAttribute("allPatients", allPatients);
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

    @GetMapping("/doctors-by-room")
    @ResponseBody
    public List<Staff> getDoctorsByRoom(@RequestParam(required = false) String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return List.of();
        }
        return staffService.getDoctorsByRoom(roomId.trim());
    }

    @PostMapping("/patient")
    public String savePatient(@ModelAttribute PatientFormDto form, RedirectAttributes ra) {
        if (form.getHoTen() == null || form.getHoTen().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập họ và tên bệnh nhân!");
            return "redirect:/registration?tab=patients";
        }
        if (form.getSdt() == null || form.getSdt().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập số điện thoại liên hệ của bệnh nhân!");
            return "redirect:/registration?tab=patients";
        }

        try {
            Patient p = patientService.savePatient(form);
            ra.addFlashAttribute("successMessage", "Đã lưu hồ sơ bệnh nhân thành công: " + p.getHoTen() + " (" + p.getIdBn() + ")");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Không thể lưu bệnh nhân: " + ex.getMessage());
        }
        return "redirect:/registration?tab=patients";
    }

    @PostMapping("/exam")
    public String createExam(@ModelAttribute ExamFormDto form, RedirectAttributes ra) {
        if (form.getIdBn() == null || form.getIdBn().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng chọn bệnh nhân cần đăng ký khám!");
            return "redirect:/registration?tab=exams";
        }
        if (form.getIdPk() == null || form.getIdPk().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng chọn phòng khám chuyên khoa!");
            return "redirect:/registration?tab=exams";
        }
        if (form.getIdNv() == null || form.getIdNv().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng chọn bác sĩ phụ trách tiếp nhận!");
            return "redirect:/registration?tab=exams";
        }

        ClinicRoom room = staffService.getAllRooms().stream()
                .filter(r -> r.getIdPk().equalsIgnoreCase(form.getIdPk().trim()))
                .findFirst().orElse(null);
        if (room == null) {
            ra.addFlashAttribute("errorMessage", "Phòng khám đã chọn không tồn tại trong hệ thống!");
            return "redirect:/registration?tab=exams";
        }

        Staff doctor = staffService.getStaffById(form.getIdNv().trim());
        if (doctor == null || !"Bác sĩ".equalsIgnoreCase(doctor.getVaiTro())) {
            ra.addFlashAttribute("errorMessage", "Bác sĩ phụ trách không hợp lệ!");
            return "redirect:/registration?tab=exams";
        }

        if (doctor.getIdPk() == null || !doctor.getIdPk().equalsIgnoreCase(form.getIdPk().trim())) {
            ra.addFlashAttribute("errorMessage", "Bác sĩ " + doctor.getHoTen() + " không thuộc " + room.getTenPhong() + "! Vui lòng chọn đúng bác sĩ được phân công.");
            return "redirect:/registration?tab=exams";
        }

        if (form.getNgayDky() == null || form.getNgayDky().trim().isEmpty()) {
            form.setNgayDky(LocalDate.now().toString());
        }

        try {
            Examination ex = examinationService.createExamination(form);
            ra.addFlashAttribute("successMessage", "Đăng ký lượt khám thành công! Mã phiếu khám: " + ex.getIdPhieukham() + " (Đã gán " + ex.getDoctorName() + " · " + ex.getRoomName() + ")");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Không thể tạo phiếu khám: " + ex.getMessage());
        }
        return "redirect:/registration?tab=exams";
    }
}

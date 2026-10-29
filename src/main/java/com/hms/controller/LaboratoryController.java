package com.hms.controller;

import com.hms.mock.MockStaffService;
import com.hms.model.LabResult;
import com.hms.model.LabType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/laboratory")
public class LaboratoryController {

    private final MockStaffService staffService;

    public LaboratoryController(MockStaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String status,
                        Model model) {
        List<LabResult> list = staffService.getAllLabResults();
        if (search != null && !search.trim().isEmpty()) {
            String kw = search.trim().toLowerCase();
            list = list.stream().filter(l -> (l.getIdKqxn() + " " + l.getPatientName() + " " + l.getTenLxn() + " " + l.getIdPhieukham()).toLowerCase().contains(kw)).collect(Collectors.toList());
        }
        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
            list = list.stream().filter(l -> status.equalsIgnoreCase(l.getTrangThai())).collect(Collectors.toList());
        }

        long completed = list.stream().filter(l -> "Hoàn thành".equalsIgnoreCase(l.getTrangThai())).count();
        long pending = list.stream().filter(l -> !"Hoàn thành".equalsIgnoreCase(l.getTrangThai())).count();

        model.addAttribute("activeNav", "laboratory");
        model.addAttribute("labResults", list);
        model.addAttribute("completedCount", completed);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("totalCount", list.size());
        model.addAttribute("search", search);
        model.addAttribute("status", status);

        return "laboratory/index";
    }

    @GetMapping("/types")
    public String labTypes(@RequestParam(required = false) String search,
                           @RequestParam(required = false) String status,
                           Model model) {
        List<LabType> list = staffService.getAllLabTypes();
        if (search != null && !search.trim().isEmpty()) {
            String kw = search.trim().toLowerCase();
            list = list.stream()
                    .filter(l -> (l.getIdLxn() + " " + l.getTenLxn()).toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
            list = list.stream()
                    .filter(l -> status.equalsIgnoreCase(l.getTrangThai()))
                    .collect(Collectors.toList());
        }

        long activeCount = staffService.getAllLabTypes().stream()
                .filter(l -> "Hoạt động".equalsIgnoreCase(l.getTrangThai())).count();
        long inactiveCount = staffService.getAllLabTypes().stream()
                .filter(l -> !"Hoạt động".equalsIgnoreCase(l.getTrangThai())).count();

        model.addAttribute("activeNav", "lab-types");
        model.addAttribute("pageTitle", "Quản lý loại xét nghiệm");
        model.addAttribute("labTypes", list);
        model.addAttribute("totalCount", staffService.getAllLabTypes().size());
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("newLabType", new LabType());

        return "laboratory/types";
    }

    @PostMapping("/types/create")
    public String createLabType(@ModelAttribute LabType labType, RedirectAttributes ra) {
        try {
            LabType created = staffService.createLabType(labType);
            ra.addFlashAttribute("successMessage", "Đã thêm loại xét nghiệm mới: " + created.getTenLxn() + " (" + created.getIdLxn() + ")");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Không thể thêm loại xét nghiệm: " + ex.getMessage());
        }
        return "redirect:/laboratory/types";
    }

    @PostMapping("/types/delete")
    public String deleteLabType(@RequestParam String idLxn, RedirectAttributes ra) {
        try {
            staffService.deleteLabType(idLxn);
            ra.addFlashAttribute("successMessage", "Đã xóa loại xét nghiệm " + idLxn + " thành công.");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/laboratory/types";
    }

    @PostMapping("/types/toggle")
    public String toggleLabType(@RequestParam String idLxn, RedirectAttributes ra) {
        try {
            LabType updated = staffService.toggleLabTypeStatus(idLxn);
            if (updated != null) {
                ra.addFlashAttribute("successMessage", "Đã chuyển trạng thái " + updated.getIdLxn() + " sang: " + updated.getTrangThai());
            }
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Không thể cập nhật trạng thái: " + ex.getMessage());
        }
        return "redirect:/laboratory/types";
    }

    @PostMapping("/result")
    public String saveResult(@RequestParam String idKqxn,
                             @RequestParam String ketQua,
                             @RequestParam(required = false) String ketLuan,
                             RedirectAttributes ra) {
        boolean ok = staffService.updateLabResult(idKqxn, ketQua, ketLuan);
        if (ok) {
            ra.addFlashAttribute("successMessage", "Đã cập nhật kết quả xét nghiệm " + idKqxn);
        }
        return "redirect:/laboratory";
    }
}


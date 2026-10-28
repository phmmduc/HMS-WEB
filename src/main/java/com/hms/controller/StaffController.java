package com.hms.controller;

import com.hms.mock.MockStaffService;
import com.hms.model.ClinicRoom;
import com.hms.model.Specialty;
import com.hms.model.Staff;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final MockStaffService staffService;

    public StaffController(MockStaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String role,
                        Model model) {
        List<Staff> staffList = staffService.getAllStaff();
        if (search != null && !search.trim().isEmpty()) {
            String kw = search.trim().toLowerCase();
            staffList = staffList.stream().filter(s -> (s.getHoTen() + " " + s.getIdNv() + " " + s.getEmail()).toLowerCase().contains(kw)).collect(Collectors.toList());
        }
        if (role != null && !role.trim().isEmpty() && !"all".equalsIgnoreCase(role)) {
            staffList = staffList.stream().filter(s -> role.equalsIgnoreCase(s.getVaiTro())).collect(Collectors.toList());
        }

        List<Specialty> specialties = staffService.getAllSpecialties();
        List<ClinicRoom> rooms = staffService.getAllRooms();

        model.addAttribute("activeNav", "staff");
        model.addAttribute("staffList", staffList);
        model.addAttribute("specialties", specialties);
        model.addAttribute("rooms", rooms);
        model.addAttribute("search", search);
        model.addAttribute("role", role);

        return "staff/index";
    }
}


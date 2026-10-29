package com.hms.controller;

import com.hms.mock.MockDataStore;
import com.hms.mock.MockStaffService;
import com.hms.model.Role;
import com.hms.model.Staff;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class RoleSelectionController {

    private final MockDataStore store;
    private final MockStaffService staffService;

    public RoleSelectionController(MockDataStore store, MockStaffService staffService) {
        this.store = store;
        this.staffService = staffService;
    }

    @GetMapping("/")
    public String roleSelect(Model model, HttpSession session) {
        model.addAttribute("roles", Role.values());
        Role current = (Role) session.getAttribute(GlobalControllerAdvice.SESSION_ROLE);
        model.addAttribute("selectedRole", current != null ? current.getKey() : "manager");
        return "auth/role-select";
    }

    @GetMapping("/select-role/{roleKey}")
    public String selectRole(@PathVariable String roleKey, HttpSession session) {
        Role role = Role.fromKey(roleKey);
        session.setAttribute(GlobalControllerAdvice.SESSION_ROLE, role);
        session.removeAttribute(GlobalControllerAdvice.SESSION_STAFF);
        return "redirect:/select-staff?role=" + role.getKey();
    }

    @GetMapping("/select-staff")
    public String selectStaff(@RequestParam(required = false) String role,
                             Model model,
                             HttpSession session) {
        Role selectedRole;
        if (role != null && !role.isBlank()) {
            selectedRole = Role.fromKey(role);
        } else {
            selectedRole = (Role) session.getAttribute(GlobalControllerAdvice.SESSION_ROLE);
            if (selectedRole == null) selectedRole = Role.MANAGER;
        }
        session.setAttribute(GlobalControllerAdvice.SESSION_ROLE, selectedRole);

        List<Staff> staffList = staffService.getStaffByRole(selectedRole.getShortLabel());
        Staff curStaff = (Staff) session.getAttribute(GlobalControllerAdvice.SESSION_STAFF);
        if (curStaff != null && !selectedRole.getShortLabel().equalsIgnoreCase(curStaff.getVaiTro())) {
            curStaff = null;
            session.removeAttribute(GlobalControllerAdvice.SESSION_STAFF);
        }

        model.addAttribute("selectedRole", selectedRole);
        model.addAttribute("staffList", staffList);
        model.addAttribute("currentStaff", curStaff);
        return "auth/staff-select";
    }

    @GetMapping({"/choose-staff/{id}", "/select-staff/{id}"})
    public String chooseStaff(@PathVariable String id, HttpSession session) {
        Staff staff = staffService.getStaffById(id);
        if (staff == null) {
            return "redirect:/";
        }
        Role matchedRole = null;
        for (Role r : Role.values()) {
            if (r.getShortLabel().equalsIgnoreCase(staff.getVaiTro())) {
                matchedRole = r;
                break;
            }
        }
        if (matchedRole == null) {
            matchedRole = Role.MANAGER;
        }

        session.setAttribute(GlobalControllerAdvice.SESSION_ROLE, matchedRole);
        session.setAttribute(GlobalControllerAdvice.SESSION_STAFF, staff);

        return "redirect:" + matchedRole.getStartUrl();
    }

    @GetMapping("/switch-staff/{id}")
    public String switchStaff(@PathVariable String id, HttpSession session, HttpServletRequest request) {
        Staff staff = staffService.getStaffById(id);
        if (staff != null) {
            session.setAttribute(GlobalControllerAdvice.SESSION_STAFF, staff);
            for (Role r : Role.values()) {
                if (r.getShortLabel().equalsIgnoreCase(staff.getVaiTro())) {
                    session.setAttribute(GlobalControllerAdvice.SESSION_ROLE, r);
                    break;
                }
            }
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/dashboard");
    }

    @PostMapping("/reset-demo")
    public String resetDemo(HttpSession session) {
        store.resetData();
        session.removeAttribute(GlobalControllerAdvice.SESSION_ROLE);
        session.removeAttribute(GlobalControllerAdvice.SESSION_STAFF);
        return "redirect:/";
    }
}

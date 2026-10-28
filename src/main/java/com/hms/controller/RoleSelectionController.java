package com.hms.controller;

import com.hms.mock.MockDataStore;
import com.hms.model.Role;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RoleSelectionController {

    private final MockDataStore store;

    public RoleSelectionController(MockDataStore store) {
        this.store = store;
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
        return "redirect:" + role.getStartUrl();
    }

    @PostMapping("/reset-demo")
    public String resetDemo() {
        store.resetData();
        return "redirect:/";
    }
}


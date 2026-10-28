package com.hms.controller;

import com.hms.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class GlobalControllerAdvice {

    public static final String SESSION_ROLE = "currentRole";

    @ModelAttribute("currentRole")
    public Role getCurrentRole(HttpServletRequest request, HttpSession session) {
        String roleParam = request.getParameter("role");
        if (roleParam != null && !roleParam.trim().isEmpty()) {
            Role role = Role.fromKey(roleParam);
            session.setAttribute(SESSION_ROLE, role);
            return role;
        }

        Role sessionRole = (Role) session.getAttribute(SESSION_ROLE);
        if (sessionRole != null) {
            return sessionRole;
        }

        // Default role is MANAGER
        session.setAttribute(SESSION_ROLE, Role.MANAGER);
        return Role.MANAGER;
    }

    @ModelAttribute("allRoles")
    public Role[] getAllRoles() {
        return Role.values();
    }

    @ModelAttribute("todayDate")
    public String getTodayDate() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}


package com.hms.controller;

import com.hms.mock.MockStaffService;
import com.hms.model.Role;
import com.hms.model.Staff;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    public static final String SESSION_ROLE = "currentRole";
    public static final String SESSION_STAFF = "currentStaff";

    private final MockStaffService staffService;

    public GlobalControllerAdvice(MockStaffService staffService) {
        this.staffService = staffService;
    }

    @ModelAttribute("currentRole")
    public Role getCurrentRole(HttpServletRequest request, HttpSession session) {
        String roleParam = request.getParameter("role");
        if (roleParam != null && !roleParam.trim().isEmpty()) {
            Role role = Role.fromKey(roleParam);
            session.setAttribute(SESSION_ROLE, role);
            syncStaffForRole(session, role);
            return role;
        }

        Role sessionRole = (Role) session.getAttribute(SESSION_ROLE);
        if (sessionRole != null) {
            return sessionRole;
        }

        // Default role is MANAGER
        session.setAttribute(SESSION_ROLE, Role.MANAGER);
        syncStaffForRole(session, Role.MANAGER);
        return Role.MANAGER;
    }

    public void syncStaffForRole(HttpSession session, Role role) {
        Staff cur = (Staff) session.getAttribute(SESSION_STAFF);
        if (cur == null || !role.getShortLabel().equalsIgnoreCase(cur.getVaiTro())) {
            Staff defaultStaff = staffService.getStaffById(role.getEmployeeId());
            if (defaultStaff == null) {
                List<Staff> matching = staffService.getStaffByRole(role.getShortLabel());
                if (!matching.isEmpty()) defaultStaff = matching.get(0);
            }
            if (defaultStaff != null) {
                session.setAttribute(SESSION_STAFF, defaultStaff);
            }
        }
    }

    @ModelAttribute("currentStaff")
    public Staff getCurrentStaff(HttpSession session) {
        Role curRole = (Role) session.getAttribute(SESSION_ROLE);
        if (curRole == null) curRole = Role.MANAGER;

        Staff cur = (Staff) session.getAttribute(SESSION_STAFF);
        if (cur == null || !curRole.getShortLabel().equalsIgnoreCase(cur.getVaiTro())) {
            syncStaffForRole(session, curRole);
            cur = (Staff) session.getAttribute(SESSION_STAFF);
        }
        return cur;
    }

    @ModelAttribute("sameRoleStaffList")
    public List<Staff> getSameRoleStaffList(HttpSession session) {
        Role curRole = (Role) session.getAttribute(SESSION_ROLE);
        if (curRole == null) curRole = Role.MANAGER;
        return staffService.getStaffByRole(curRole.getShortLabel());
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

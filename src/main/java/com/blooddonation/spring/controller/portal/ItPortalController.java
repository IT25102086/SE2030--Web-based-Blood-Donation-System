package com.blooddonation.spring.controller.portal;

import com.blooddonation.spring.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * IT Operations Supervisor portal — owned by <b>IT25102086</b>.
 * Branch: {@code feature/IT25102086-it-operations-supervisor}.
 * Routes: {@code /portal/it} (all methods below). Only this student modifies
 * this file.
 */
@Controller
@RequestMapping("/portal")
public class ItPortalController extends BasePortalController {

    // IT Operations Portal
    @GetMapping("/it")
    public String itPortal(Model model, HttpSession session) {
        UserAccount user = requireRole(session, UserRole.IT_ADMIN);
        model.addAttribute("user", user);
        model.addAttribute("permissions", authService.getAllPermissions());
        model.addAttribute("allScopes", authService.getAllPermissionScopes());
        var allUsers = authService.getAllUsers();
        model.addAttribute("users", allUsers);
        model.addAttribute("staffUsers", allUsers.stream()
                .filter(u -> u.getDonorId() == null || u.getDonorId().isBlank()).toList());
        model.addAttribute("donorUsers", allUsers.stream()
                .filter(u -> u.getDonorId() != null && !u.getDonorId().isBlank()).toList());
        model.addAttribute("auditLogs", authService.getRecentAuditLogs());
        model.addAttribute("roles", UserRole.values());
        return "portal/it";
    }

    @PostMapping("/it/permissions")
    public String updatePermissions(@RequestParam UserRole role, @RequestParam String permissions,
                                    HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.IT_ADMIN);
            authService.updatePermissions(role, permissions, user.getUsername());
            redirects.addFlashAttribute("success", "Permissions updated for " + role.getDisplayName());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/it";
    }

    @PostMapping("/it/users")
    public String createUser(@RequestParam String username, @RequestParam String password,
                             @RequestParam String fullName, @RequestParam UserRole role,
                             @RequestParam(required = false) String hospital,
                             HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount actor = requireRole(session, UserRole.IT_ADMIN);
            authService.createUser(username, password, fullName, role, hospital);
            authService.log(actor.getUsername(), "CREATE_USER", "Created user " + username + " (" + role + ")");
            redirects.addFlashAttribute("success", "User account created: " + username);
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/it";
    }

    @PostMapping("/it/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount actor = requireRole(session, UserRole.IT_ADMIN);
            UserAccount u = authService.toggleUserStatus(id);
            authService.log(actor.getUsername(), "TOGGLE_USER_STATUS", "User " + u.getUsername() + " status → " + (u.isActive() ? "Active" : "Inactive"));
            redirects.addFlashAttribute("success", "Account status updated for " + u.getUsername() + " (" + (u.isActive() ? "Active" : "Deactivated") + ")");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/it";
    }

    @PostMapping("/it/users/{id}/edit")
    public String editUser(@PathVariable String id, @RequestParam String fullName,
                           @RequestParam UserRole role, @RequestParam(required = false) String hospital,
                           @RequestParam(required = false) String newPassword,
                           HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount actor = requireRole(session, UserRole.IT_ADMIN);
            UserAccount u = authService.updateUser(id, fullName, role, hospital, newPassword);
            authService.log(actor.getUsername(), "UPDATE_USER", "Updated user " + u.getUsername());
            redirects.addFlashAttribute("success", "User profile updated for " + u.getUsername());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/it";
    }

    @PostMapping("/it/users/{id}/delete")
    public String deleteUser(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount actor = requireRole(session, UserRole.IT_ADMIN);
            authService.deleteUser(id, actor.getUsername());
            authService.log(actor.getUsername(), "DELETE_USER", "Deleted user ID " + id);
            redirects.addFlashAttribute("success", "User account removed permanently.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/it";
    }
}

package com.blooddonation.spring.controller.portal;

import com.blooddonation.spring.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Blood Bank Manager portal — owned by <b>IT25102082</b>.
 * Branch: {@code feature/IT25102082-blood-bank-manager}.
 * Routes: {@code /portal/manager} (all methods below). Only this student
 * modifies this file.
 */
@Controller
@RequestMapping("/portal")
public class ManagerPortalController extends BasePortalController {

    // Blood Bank Manager Portal
    @GetMapping("/manager")
    public String managerPortal(@RequestParam(required = false) String branch, Model model, HttpSession session) {
        UserAccount user = requireRole(session, UserRole.BLOOD_BANK_MANAGER);
        model.addAttribute("user", user);
        model.addAttribute("stockSummary", service.getStockDashboard(branch));
        model.addAttribute("lowStockAlerts", service.getLowStockAlerts());
        model.addAttribute("pendingRequests", service.getRequests("Pending", null, null));
        model.addAttribute("submittedRequisitions", service.getRequests("Submitted", null, null));
        model.addAttribute("branchFilter", branch);
        model.addAttribute("upcomingAppointments", service.getUpcomingAppointments());
        // Collection dropdown: only eligible + Available donors (collected ones
        // drop out automatically until the 120-day gap passes)
        var eligibleDonors = service.searchDonors(null, null, null).stream()
                .filter(d -> "Available".equalsIgnoreCase(d.getAvailability()))
                .filter(d -> "ELIGIBLE".equals(service.checkDonorEligibility(d.getId())))
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("donors", eligibleDonors);
        return "portal/manager";
    }

    @PostMapping("/manager/stock/add")
    public String addStockBatch(@RequestParam String bloodGroup, @RequestParam String component,
                                @RequestParam String branch, @RequestParam int units,
                                @RequestParam(defaultValue = "35") int expiryDays,
                                HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.BLOOD_BANK_MANAGER);
            service.registerStockBatch(bloodGroup, component, branch, units, expiryDays);
            authService.log(user.getUsername(), "ADD_STOCK_BATCH", units + " units of " + bloodGroup + " added to " + branch);
            redirects.addFlashAttribute("success", units + " units of " + bloodGroup + " (" + component + ") registered for " + branch + ".");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/manager";
    }

    @PostMapping("/manager/stock/adjust")
    public String adjustStock(@RequestParam String bloodGroup, @RequestParam String component,
                              @RequestParam String branch, @RequestParam int delta,
                              @RequestParam(required = false) String reason,
                              HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.BLOOD_BANK_MANAGER);
            long newCount = service.adjustStockQuantity(bloodGroup, component, branch, delta, reason);
            authService.log(user.getUsername(), "ADJUST_STOCK",
                    (delta > 0 ? "+" + delta : "" + delta) + " " + bloodGroup + " " + component + " @ " + branch
                    + (reason != null && !reason.isBlank() ? " (" + reason.trim() + ")" : "") + " → now " + newCount);
            redirects.addFlashAttribute("success", "Stock updated: " + bloodGroup + " " + component + " @ " + branch
                    + " (" + (delta > 0 ? "+" + delta : delta) + ") → " + newCount + " available.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/manager";
    }

    @PostMapping("/manager/collect")
    public String collectFromDonor(@RequestParam String donorPhone, @RequestParam String component,
                                   @RequestParam String branch, @RequestParam(required = false) String location,
                                   HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.BLOOD_BANK_MANAGER);
            var donorOpt = service.getDonorByPhone(donorPhone);
            if (donorOpt.isEmpty()) throw new Exception("No donor found with phone " + donorPhone + ". Register them first.");
            BloodUnit unit = service.collectDonation(donorOpt.get().getId(), component, branch, location);
            authService.log(user.getUsername(), "COLLECT_DONATION",
                    "Collected " + unit.getBloodGroup() + " " + component + " from " + donorOpt.get().getName() + " → unit " + unit.getId());
            redirects.addFlashAttribute("success", "Collection recorded: " + unit.getBloodGroup() + " " + component
                    + " from " + donorOpt.get().getName() + " → Pending Screening. Donor hidden from list until eligible again.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/manager";
    }

    @PostMapping("/manager/appointments/{id}/status")
    public String updateAppointmentStatus(@PathVariable String id, @RequestParam String status,
                                          HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.BLOOD_BANK_MANAGER);
            if ("Completed".equalsIgnoreCase(status)) service.completeAppointment(id);
            else if ("Cancelled".equalsIgnoreCase(status)) service.cancelAppointment(id, null);
            else throw new Exception("Invalid appointment status.");
            authService.log(user.getUsername(), "APPOINTMENT_" + status.toUpperCase(), "Appointment " + id + " → " + status);
            redirects.addFlashAttribute("success", "Appointment marked as " + status + ".");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/manager";
    }

    @PostMapping("/manager/approve/{id}")    public String approveRequest(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.BLOOD_BANK_MANAGER);
            Request req = service.approveRequest(id);
            authService.log(user.getUsername(), "APPROVE_REQUEST", "Request " + id + " approved");
            redirects.addFlashAttribute("success", "Request " + req.getId() + " approved.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/manager";
    }

    @PostMapping("/manager/reject/{id}")
    public String rejectRequest(@PathVariable String id, @RequestParam(required = false) String reason,
                                HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.BLOOD_BANK_MANAGER);
            Request req = service.rejectRequest(id, reason);
            authService.log(user.getUsername(), "REJECT_REQUEST", "Request " + id + " rejected. Reason: " + reason);
            redirects.addFlashAttribute("success", "Request " + req.getId() + " rejected.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/manager";
    }

    @PostMapping("/manager/request/{id}/delete")
    public String deleteManagerRequest(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.BLOOD_BANK_MANAGER);
            boolean ok = service.deleteRequest(id);
            if (ok) {
                authService.log(user.getUsername(), "DELETE_REQUEST", "Purged request " + id);
                redirects.addFlashAttribute("success", "Request purged successfully.");
            } else {
                redirects.addFlashAttribute("error", "Request not found.");
            }
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/manager";
    }
}

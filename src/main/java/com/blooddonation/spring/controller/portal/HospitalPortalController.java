package com.blooddonation.spring.controller.portal;

import com.blooddonation.spring.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Hospital Coordinator portal — owned by <b>IT25102084</b>.
 * Branch: {@code feature/IT25102084-hospital-coordinator}.
 * Routes: {@code /portal/hospital} (all methods below). Only this student
 * modifies this file.
 */
@Controller
@RequestMapping("/portal")
public class HospitalPortalController extends BasePortalController {

    // Hospital Coordinator Portal
    @GetMapping("/hospital")
    public String hospitalPortal(Model model, HttpSession session) {
        UserAccount user = requireRole(session, UserRole.HOSPITAL_COORDINATOR);
        model.addAttribute("user", user);
        model.addAttribute("requisitions", service.getRequests(null, null, null).stream()
                .filter(r -> "Hospital Requisition".equals(r.getRequestType())).toList());
        model.addAttribute("newRequisition", new Request());
        return "portal/hospital";
    }

    @PostMapping("/hospital/requisition")
    public String submitRequisition(@ModelAttribute Request req,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime neededByDate,
                                    HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.HOSPITAL_COORDINATOR);
            req.setNeededBy(neededByDate);
            req.setHospital(user.getHospital());
            long available = service.countAvailableUnits(req.getBloodGroup(), req.getComponent(), null);
            Request saved = service.createHospitalRequisition(req);
            authService.log(user.getUsername(), "SUBMIT_REQUISITION",
                    "Requisition " + saved.getId() + " — availability: " + available + " units");
            redirects.addFlashAttribute("success",
                    "Requisition ID: " + saved.getId() + " | Availability check: " + available + " units found | Status: " + saved.getStatus());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/hospital";
    }

    @PostMapping("/hospital/requisition/{id}/edit")
    public String editHospitalRequisition(@PathVariable String id,
                                          @RequestParam Integer units,
                                          @RequestParam String urgency,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime neededByDate,
                                          @RequestParam String contactPhone,
                                          @RequestParam(required = false) String notes,
                                          HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.HOSPITAL_COORDINATOR);
            Request updated = service.updateRequestDetails(id, units, neededByDate, contactPhone, notes, urgency);
            authService.log(user.getUsername(), "UPDATE_REQUISITION", "Updated requisition " + updated.getId());
            redirects.addFlashAttribute("success", "Requisition updated successfully! (ID: " + updated.getId() + ")");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/hospital";
    }

    @PostMapping("/hospital/requisition/{id}/delete")
    public String deleteHospitalRequisition(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.HOSPITAL_COORDINATOR);
            boolean ok = service.deleteRequest(id);
            if (ok) {
                authService.log(user.getUsername(), "DELETE_REQUISITION", "Cancelled/Deleted requisition " + id);
                redirects.addFlashAttribute("success", "Hospital requisition cancelled and deleted successfully.");
            } else {
                redirects.addFlashAttribute("error", "Requisition not found or could not be cancelled.");
            }
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/hospital";
    }

    @GetMapping("/hospital/check-availability")
    @ResponseBody
    public String checkAvailability(@RequestParam String bloodGroup, @RequestParam String component, HttpSession session) {
        requireRole(session, UserRole.HOSPITAL_COORDINATOR);
        long count = service.countAvailableUnits(bloodGroup, component, null);
        return count + " units available for " + bloodGroup + " (" + component + ")";
    }
}

package com.blooddonation.spring.controller.portal;

import com.blooddonation.spring.model.*;
import com.blooddonation.spring.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Donor self-service portal — <b>SHARED</b> (no student owns the donor role).
 * Routes: {@code /portal/donor}. Coordinate before modifying.
 */
@Controller
@RequestMapping("/portal")
public class DonorPortalController extends BasePortalController {

    // Donor self-service: own profile only — reuses the public donor-profile view
    @GetMapping("/donor")
    public String donorPortal(Model model, HttpSession session, RedirectAttributes redirects) {
        UserAccount user = requireDonor(session);
        var donorOpt = service.getDonorById(user.getDonorId());
        if (donorOpt.isEmpty()) {
            redirects.addFlashAttribute("error", "Your donor profile was not found. Please contact support.");
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("donor", donorOpt.get());
        model.addAttribute("eligibility", service.checkDonorEligibility(user.getDonorId()));
        model.addAttribute("myAppointments", service.getDonorAppointments(user.getDonorId()));
        return "donor-profile";
    }

    // Donor books a donation appointment
    @PostMapping("/donor/appointments")
    public String bookAppointment(@RequestParam String branch,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt,
                                  @RequestParam(required = false) String notes,
                                  HttpSession session, RedirectAttributes redirects) {
        UserAccount user = requireDonor(session);
        try {
            service.bookAppointment(user.getDonorId(), branch, scheduledAt, notes);
            authService.log(user.getUsername(), "BOOK_APPOINTMENT", "Appointment @ " + branch + " " + scheduledAt);
            redirects.addFlashAttribute("success", "Appointment booked! Please arrive 10 minutes early.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/donor";
    }

    // Donor cancels own appointment
    @PostMapping("/donor/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        UserAccount user = requireDonor(session);
        try {
            service.cancelAppointment(id, user.getDonorId());
            redirects.addFlashAttribute("success", "Appointment cancelled.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/donor";
    }
}

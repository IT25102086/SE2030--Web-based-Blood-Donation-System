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
 * Recipient / Patient portal — owned by <b>IT25102090</b>.
 * Branch: {@code feature/IT25102090-recipient-patient}.
 * Routes: {@code /portal/recipient} (all methods below). Only this student
 * modifies this file.
 */
@Controller
@RequestMapping("/portal")
public class RecipientPortalController extends BasePortalController {

    // Recipient Portal (staff recipients only; donors use /portal/donor)
    @GetMapping("/recipient")
    public String recipientPortal(@RequestParam(required = false) String bloodGroup, Model model, HttpSession session) {
        UserAccount user = requireRole(session, UserRole.RECIPIENT);
        if (isDonor(user)) return "redirect:/portal/donor";
        model.addAttribute("user", user);
        model.addAttribute("units", service.getBloodUnits(bloodGroup).stream()
                .filter(u -> "Approved - Available".equals(u.getStatus()) && u.getExpiresAt() != null).toList());
        model.addAttribute("bloodGroup", bloodGroup);
        model.addAttribute("myRequests", service.getRequests(null, null, null));
        model.addAttribute("newRequest", new Request());
        return "portal/recipient";
    }

    @PostMapping("/recipient/request")
    public String submitBloodRequest(@ModelAttribute Request request,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime neededByDate,
                                     HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount u = requireRole(session, UserRole.RECIPIENT);
            if (isDonor(u)) {
                redirects.addFlashAttribute("error", "Donor accounts can only manage their own profile.");
                return "redirect:/portal/donor";
            }
            request.setNeededBy(neededByDate);
            request.setUrgency("Urgent");
            Request saved = service.createRequest(request);
            authService.log(((UserAccount) session.getAttribute(AuthService.SESSION_USER)).getUsername(),
                    "SUBMIT_REQUEST", "Blood request " + saved.getId() + " submitted");
            redirects.addFlashAttribute("success", "Request submitted! Request ID: " + saved.getId() + ". Status: " + saved.getStatus());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/recipient";
    }

    @PostMapping("/recipient/request/{id}/edit")
    public String editRecipientRequest(@PathVariable String id,
                                       @RequestParam Integer units,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime neededByDate,
                                       @RequestParam String contactPhone,
                                       @RequestParam(required = false) String notes,
                                       HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount u2 = requireRole(session, UserRole.RECIPIENT);
            if (isDonor(u2)) {
                redirects.addFlashAttribute("error", "Donor accounts can only manage their own profile.");
                return "redirect:/portal/donor";
            }
            var existing = service.getRequestById(id);
            if (existing.isPresent() && ("Delivered".equalsIgnoreCase(existing.get().getStatus())
                    || "Dispatch Scheduled".equalsIgnoreCase(existing.get().getStatus()))) {
                redirects.addFlashAttribute("error", "This request is already " + existing.get().getStatus() + " and can no longer be edited.");
                return "redirect:/portal/recipient";
            }
            Request updated = service.updateRequestDetails(id, units, neededByDate, contactPhone, notes, null);
            authService.log(((UserAccount) session.getAttribute(AuthService.SESSION_USER)).getUsername(),
                    "UPDATE_REQUEST", "Updated blood request " + updated.getId());
            redirects.addFlashAttribute("success", "Request updated successfully! (ID: " + updated.getId() + ")");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/recipient";
    }

    @PostMapping("/recipient/request/{id}/delete")
    public String deleteRecipientRequest(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount u3 = requireRole(session, UserRole.RECIPIENT);
            if (isDonor(u3)) {
                redirects.addFlashAttribute("error", "Donor accounts can only manage their own profile.");
                return "redirect:/portal/donor";
            }
            var target = service.getRequestById(id);
            if (target.isPresent() && ("Delivered".equalsIgnoreCase(target.get().getStatus())
                    || "Dispatch Scheduled".equalsIgnoreCase(target.get().getStatus()))) {
                redirects.addFlashAttribute("error", "This request is already " + target.get().getStatus() + " and can no longer be cancelled.");
                return "redirect:/portal/recipient";
            }
            boolean ok = service.deleteRequest(id);
            if (ok) {
                authService.log(((UserAccount) session.getAttribute(AuthService.SESSION_USER)).getUsername(),
                        "DELETE_REQUEST", "Cancelled/Deleted blood request " + id);
                redirects.addFlashAttribute("success", "Blood request cancelled and removed successfully.");
            } else {
                redirects.addFlashAttribute("error", "Request not found or could not be removed.");
            }
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/recipient";
    }
}

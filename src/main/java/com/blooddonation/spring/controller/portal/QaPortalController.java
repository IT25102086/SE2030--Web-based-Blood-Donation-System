package com.blooddonation.spring.controller.portal;

import com.blooddonation.spring.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Quality Assurance Officer portal — owned by <b>IT25102085</b>.
 * Branch: {@code feature/IT25102085-quality-assurance}.
 * Routes: {@code /portal/qa} (all methods below). Only this student modifies
 * this file.
 */
@Controller
@RequestMapping("/portal")
public class QaPortalController extends BasePortalController {

    // QA Officer Portal
    @GetMapping("/qa")
    public String qaPortal(@RequestParam(required = false) String unitId, Model model, HttpSession session) {
        UserAccount user = requireRole(session, UserRole.QA_OFFICER);
        model.addAttribute("user", user);
        model.addAttribute("units", service.getBloodUnits(null));
        model.addAttribute("pendingUnits", service.getBloodUnits(null).stream()
                .filter(u -> "Pending Screening".equals(u.getStatus())).toList());
        if (unitId != null) {
            service.getBloodUnits(null).stream().filter(u -> u.getId().equals(unitId)).findFirst()
                    .ifPresent(u -> model.addAttribute("selectedUnit", u));
        }
        return "portal/qa";
    }

    @PostMapping("/qa/screening")
    public String logScreening(@RequestParam String unitId,
                               @RequestParam(required = false, defaultValue = "false") boolean hiv,
                               @RequestParam(required = false, defaultValue = "false") boolean hepB,
                               @RequestParam(required = false, defaultValue = "false") boolean hepC,
                               @RequestParam(required = false, defaultValue = "false") boolean syphilis,
                               @RequestParam(required = false, defaultValue = "false") boolean malaria,
                               @RequestParam(required = false) String notes, @RequestParam(required = false) Boolean crossMatch,
                               HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.QA_OFFICER);
            BloodUnit unit = service.recordScreening(unitId, hiv, hepB, hepC, syphilis, malaria, notes);
            authService.log(user.getUsername(), "LOG_SCREENING", "Unit " + unitId + " → " + unit.getStatus());
            redirects.addFlashAttribute("success", "Screening recorded. Unit status: " + unit.getStatus());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/qa?unitId=" + unitId;
    }

    @PostMapping("/qa/unit/{id}/discard")
    public String discardBloodUnit(@PathVariable String id, @RequestParam(required = false) String reason,
                                   HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.QA_OFFICER);
            BloodUnit unit = service.discardBloodUnit(id, reason);
            authService.log(user.getUsername(), "LOG_DISCARD", "Unit " + id + " decommissioned. Reason: " + reason);
            redirects.addFlashAttribute("success", "Unit " + id + " has been decommissioned & marked as Discarded.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/qa";
    }

    @PostMapping("/qa/unit/{id}/notes")
    public String updateQaNotes(@PathVariable String id, @RequestParam String notes,
                                HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.QA_OFFICER);
            service.updateBloodUnitNotes(id, notes);
            authService.log(user.getUsername(), "UPDATE_QA_NOTES", "Unit " + id + " notes updated");
            redirects.addFlashAttribute("success", "Quality assurance notes updated.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/qa?unitId=" + id;
    }
}

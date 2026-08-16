package com.blooddonation.spring.controller;

import com.blooddonation.spring.model.AvailabilitySlot;
import com.blooddonation.spring.model.Donor;
import com.blooddonation.spring.model.Request;
import com.blooddonation.spring.service.BloodDonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private BloodDonationService service;

    // Home / Landing Page
    @GetMapping("/")
    public String home(Model model) {
        List<Donor> allDonors = service.searchDonors(null, null, null);
        List<Request> openRequests = service.getRequests("Pending", null, null);
        List<Request> fulfilledRequests = service.getRequests("Delivered", null, null);

        model.addAttribute("totalDonors", allDonors.size());
        model.addAttribute("totalOpenRequests", openRequests.size());
        model.addAttribute("totalFulfilledRequests", fulfilledRequests.size());
        
        // Show up to 3 urgent open requests
        List<Request> urgentRequests = openRequests.stream()
            .limit(3)
            .toList();
        model.addAttribute("urgentRequests", urgentRequests);
        
        return "index";
    }

    // Register Donor GET & POST
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("donor", new Donor());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @ModelAttribute Donor donor,
            @RequestParam(value = "slotDays", required = false) List<String> days,
            @RequestParam(value = "slotStarts", required = false) List<String> starts,
            @RequestParam(value = "slotEnds", required = false) List<String> ends,
            RedirectAttributes redirectAttributes) {
        try {
            // Build slots list
            List<AvailabilitySlot> slots = new ArrayList<>();
            if (days != null) {
                for (int i = 0; i < days.size(); i++) {
                    if (i < starts.size() && i < ends.size()) {
                        String day = days.get(i);
                        String sTime = starts.get(i);
                        String eTime = ends.get(i);
                        if (day != null && !day.isEmpty() && sTime != null && !sTime.isEmpty() && eTime != null && !eTime.isEmpty()) {
                            slots.add(new AvailabilitySlot(day, sTime, eTime));
                        }
                    }
                }
            }
            donor.setAvailabilitySlots(slots);

            Donor registered = service.registerDonor(donor);
            redirectAttributes.addFlashAttribute("success", "Successfully registered as a donor! Profile ID: " + registered.getId());
            return "redirect:/donor/" + registered.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("donor", donor);
            return "redirect:/register";
        }
    }

    // Search Donors
    @GetMapping("/availability")
    public String availability(@RequestParam(value = "bloodGroup", required = false) String bloodGroup, Model model) {
        model.addAttribute("units", service.getBloodUnits(bloodGroup).stream().filter(u -> "Approved - Available".equals(u.getStatus())).toList());
        model.addAttribute("bloodGroup", bloodGroup);
        return "availability";
    }

    @GetMapping("/search")
    public String searchDonors(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "bloodGroup", required = false) String bloodGroup,
            @RequestParam(value = "city", required = false) String city,
            Model model) {
        
        List<Donor> donors = service.searchDonors(q, bloodGroup, city);
        model.addAttribute("donors", donors);
        model.addAttribute("q", q);
        model.addAttribute("bloodGroup", bloodGroup);
        model.addAttribute("city", city);
        return "search";
    }

    // Single Donor Profile / Edit
    @GetMapping("/donor/{id}")
    public String donorProfile(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Donor> opt = service.getDonorById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Donor profile not found.");
            return "redirect:/search";
        }
        model.addAttribute("donor", opt.get());
        return "donor-profile";
    }

    // Update Donor Profile Details
    @PostMapping("/donor/{id}/edit")
    public String editDonor(
            @PathVariable("id") String id,
            @ModelAttribute Donor donor,
            @RequestParam(value = "slotDays", required = false) List<String> days,
            @RequestParam(value = "slotStarts", required = false) List<String> starts,
            @RequestParam(value = "slotEnds", required = false) List<String> ends,
            RedirectAttributes redirectAttributes) {
        try {
            donor.setId(id);
            List<AvailabilitySlot> slots = new ArrayList<>();
            if (days != null) {
                for (int i = 0; i < days.size(); i++) {
                    String day = days.get(i);
                    String sTime = starts.get(i);
                    String eTime = ends.get(i);
                    if (day != null && !day.isEmpty() && sTime != null && !sTime.isEmpty() && eTime != null && !eTime.isEmpty()) {
                        slots.add(new AvailabilitySlot(day, sTime, eTime));
                    }
                }
            }
            donor.setAvailabilitySlots(slots);

            service.updateDonor(donor);
            redirectAttributes.addFlashAttribute("success", "Donor profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/donor/" + id;
    }

    // Add Donation Log entry
    @PostMapping("/donor/{id}/donations")
    public String addDonationLog(
            @PathVariable("id") String id,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam("location") String location,
            @RequestParam("notes") String notes,
            RedirectAttributes redirectAttributes) {
        try {
            service.addDonation(id, date, location, notes);
            redirectAttributes.addFlashAttribute("success", "Donation recorded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/donor/" + id;
    }

    // Delete Donor
    @PostMapping("/donor/{id}/delete")
    public String deleteDonor(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        boolean success = service.deleteDonor(id);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Donor account deleted successfully.");
            return "redirect:/search";
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete donor profile.");
            return "redirect:/donor/" + id;
        }
    }

    // Request Board view & Create Request POST
    @GetMapping("/requests")
    public String requestBoard(
            @RequestParam(value = "status", defaultValue = "Pending") String status,
            @RequestParam(value = "bloodGroup", required = false) String bloodGroup,
            @RequestParam(value = "city", required = false) String city,
            Model model) {
        
        List<Request> items = service.getRequests(status, bloodGroup, city);
        model.addAttribute("requests", items);
        model.addAttribute("newRequest", new Request());
        model.addAttribute("status", status);
        model.addAttribute("bloodGroup", bloodGroup);
        model.addAttribute("city", city);
        return "requests";
    }

    @PostMapping("/requests")
    public String createRequest(
            @ModelAttribute Request request,
            @RequestParam("neededByDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime neededBy,
            RedirectAttributes redirectAttributes) {
        try {
            request.setNeededBy(neededBy);
            service.createRequest(request);
            redirectAttributes.addFlashAttribute("success", "Blood request posted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/requests?status=Pending";
    }

    // Fulfill / Re-open Request
    @PostMapping("/requests/{id}/status")
    public String changeRequestStatus(
            @PathVariable("id") String id,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        try {
            service.updateRequestStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Request status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/requests";
    }

    // Contact Page
    @GetMapping("/contact")
    public String contactForm() {
        return "contact";
    }

    @PostMapping("/contact")
    public String contactSubmit(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            RedirectAttributes redirectAttributes) {
        try {
            service.createMessage(name, email, subject, message);
            redirectAttributes.addFlashAttribute("success", "Thank you for contacting us, " + name + "! We will get back to you shortly.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/contact";
    }
}
Commit at 2026-08-27T09:28:16
Commit at 2026-08-16T04:44:50
Commit at 2026-08-04T10:17:35
Commit at 2026-09-15T13:21:43
Commit at 2026-08-08T19:01:37
Commit at 2026-08-23T07:40:37
Commit at 2026-08-06T20:15:36
Commit at 2026-08-04T03:30:23
Commit at 2026-08-16T23:49:26

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
 * Delivery Coordination Officer portal — owned by <b>IT25102081</b>.
 * Branch: {@code feature/IT25102081-delivery-coordination}.
 * Routes: {@code /portal/delivery} (all methods below). Only this student
 * modifies this file.
 */
@Controller
@RequestMapping("/portal")
public class DeliveryPortalController extends BasePortalController {

    // Delivery Coordination Portal
    @GetMapping("/delivery")
    public String deliveryPortal(Model model, HttpSession session) {
        UserAccount user = requireRole(session, UserRole.DELIVERY_OFFICER);
        model.addAttribute("user", user);
        model.addAttribute("deliveries", service.getDeliveryOrders());
        model.addAttribute("approvedRequests", service.getRequests("Approved", null, null));
        model.addAttribute("vehicles", service.getAllVehicles());
        model.addAttribute("couriers", service.getAllCouriers());
        model.addAttribute("availableCouriers", service.getAvailableCouriers());
        model.addAttribute("availableVehicles", service.getAvailableVehicles());
        model.addAttribute("newVehicle", new DeliveryVehicle());
        model.addAttribute("newCourier", new DeliveryCourier());
        return "portal/delivery";
    }

    @PostMapping("/delivery/vehicles")
    public String registerVehicle(@ModelAttribute DeliveryVehicle vehicle, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.DELIVERY_OFFICER);
            service.registerVehicle(vehicle);
            authService.log(user.getUsername(), "REGISTER_VEHICLE", "Vehicle " + vehicle.getVehicleNumber());
            redirects.addFlashAttribute("success", "Vehicle registered: " + vehicle.getVehicleNumber());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/delivery";
    }

    @PostMapping("/delivery/vehicles/{id}/status")
    public String updateVehicleStatus(@PathVariable String id, @RequestParam String status,
                                      HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.DELIVERY_OFFICER);
            service.updateVehicleStatus(id, status);
            authService.log(user.getUsername(), "UPDATE_VEHICLE", "Vehicle " + id + " status → " + status);
            redirects.addFlashAttribute("success", "Vehicle status updated to: " + status);
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/delivery";
    }

    @PostMapping("/delivery/vehicles/{id}/delete")
    public String deleteVehicle(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.DELIVERY_OFFICER);
            boolean ok = service.deleteVehicle(id);
            if (ok) {
                authService.log(user.getUsername(), "DELETE_VEHICLE", "Deleted vehicle " + id);
                redirects.addFlashAttribute("success", "Vehicle removed from fleet.");
            } else {
                redirects.addFlashAttribute("error", "Vehicle not found.");
            }
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/delivery";
    }

    @PostMapping("/delivery/couriers")
    public String registerCourier(@ModelAttribute DeliveryCourier courier, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.DELIVERY_OFFICER);
            service.registerCourier(courier);
            authService.log(user.getUsername(), "REGISTER_COURIER", "Courier " + courier.getName());
            redirects.addFlashAttribute("success", "Courier registered: " + courier.getName());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/delivery";
    }

    @PostMapping("/delivery/couriers/{id}/status")
    public String updateCourierStatus(@PathVariable String id, @RequestParam String status,
                                      HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.DELIVERY_OFFICER);
            service.updateCourierStatus(id, status);
            authService.log(user.getUsername(), "UPDATE_COURIER", "Courier " + id + " status → " + status);
            redirects.addFlashAttribute("success", "Courier status updated to: " + status);
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/delivery";
    }

    @PostMapping("/delivery/couriers/{id}/delete")
    public String deleteCourier(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.DELIVERY_OFFICER);
            boolean ok = service.deleteCourier(id);
            if (ok) {
                authService.log(user.getUsername(), "DELETE_COURIER", "Deleted courier " + id);
                redirects.addFlashAttribute("success", "Courier removed from active directory.");
            } else {
                redirects.addFlashAttribute("error", "Courier not found.");
            }
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/delivery";
    }

    @PostMapping("/delivery/schedule")
    public String scheduleDelivery(@RequestParam String requestId, @RequestParam String courierName,
                                   @RequestParam String vehicleNumber,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt,
                                   @RequestParam(required = false) String notes,
                                   HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.DELIVERY_OFFICER);
            DeliveryOrder order = service.scheduleDelivery(requestId, courierName, vehicleNumber, scheduledAt, notes);
            authService.log(user.getUsername(), "SCHEDULE_DELIVERY", "Delivery " + order.getId() + " scheduled");
            redirects.addFlashAttribute("success", "Delivery scheduled. Requisition status: Dispatch Scheduled");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/delivery";
    }

    @PostMapping("/delivery/{id}/status")
    public String updateDeliveryStatus(@PathVariable String id, @RequestParam String status,
                                       HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.DELIVERY_OFFICER);
            service.updateDeliveryStatus(id, status);
            authService.log(user.getUsername(), "UPDATE_DELIVERY", "Delivery " + id + " → " + status);
            redirects.addFlashAttribute("success", "Delivery status updated to: " + status);
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/delivery";
    }

    @PostMapping("/delivery/{id}/cancel")
    public String cancelDelivery(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.DELIVERY_OFFICER);
            service.cancelDeliveryOrder(id);
            authService.log(user.getUsername(), "CANCEL_DELIVERY", "Cancelled delivery order " + id);
            redirects.addFlashAttribute("success", "Delivery order cancelled. Request returned to approved pool.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/delivery";
    }
}

package com.blooddonation.spring.controller;

import com.blooddonation.spring.model.*;
import com.blooddonation.spring.service.AuthService;
import com.blooddonation.spring.service.BloodDonationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/portal")
public class PortalController {

    @Autowired private BloodDonationService service;
    @Autowired private AuthService authService;

    private UserAccount requireRole(HttpSession session, UserRole... roles) {
        UserAccount user = (UserAccount) session.getAttribute(AuthService.SESSION_USER);
        if (user == null) throw new RuntimeException("Not authenticated");
        if (user.getRole() == UserRole.IT_ADMIN) return user;
        for (UserRole r : roles) if (user.getRole() == r) return user;
        throw new RuntimeException("Access denied for your role (" + user.getRole().getDisplayName() + ").");
    }

    private UserAccount requirePermission(HttpSession session, String permissionCode) {
        UserAccount user = (UserAccount) session.getAttribute(AuthService.SESSION_USER);
        if (user == null) throw new RuntimeException("Not authenticated");
        if (!authService.hasPermission(user, permissionCode)) {
            throw new RuntimeException("Access Denied: You do not have the '" + permissionCode + "' permission. Contact your IT Administrator.");
        }
        return user;
    }

    private UserAccount requireAnyPermission(HttpSession session, String... permissionCodes) {
        UserAccount user = (UserAccount) session.getAttribute(AuthService.SESSION_USER);
        if (user == null) throw new RuntimeException("Not authenticated");
        if (user.getRole() == UserRole.IT_ADMIN) return user;
        for (String perm : permissionCodes) {
            if (authService.hasPermission(user, perm)) return user;
        }
        throw new RuntimeException("Access Denied: You lack required permissions for this action. Contact your IT Administrator.");
    }

    private UserAccount requirePortalAccess(HttpSession session, UserRole defaultRole, String... fallbackPermissions) {
        UserAccount user = (UserAccount) session.getAttribute(AuthService.SESSION_USER);
        if (user == null) throw new RuntimeException("Not authenticated");
        String portal = switch (defaultRole) {
            case RECIPIENT -> "recipient";
            case HOSPITAL_COORDINATOR -> "hospital";
            case QA_OFFICER -> "qa";
            case DELIVERY_OFFICER -> "delivery";
            case BLOOD_BANK_MANAGER -> "manager";
            case IT_ADMIN -> "it";
        };
        if (authService.hasPortalAccess(user, portal)) return user;
        throw new RuntimeException("Access Denied: You do not have permissions for this portal.");
    }

    // UC-06 / PBI-05 / PBI-06 — Recipient Portal
    @GetMapping("/recipient")
    public String recipientPortal(@RequestParam(required = false) String bloodGroup, Model model, HttpSession session) {
        UserAccount user = requirePortalAccess(session, UserRole.RECIPIENT, "VIEW_AVAILABILITY", "SUBMIT_REQUEST", "TRACK_REQUEST");
        model.addAttribute("user", user);
        model.addAttribute("userPermissions", authService.getUserPermissions(user));
        model.addAttribute("accessiblePortals", authService.getAccessiblePortalKeys(user));
        model.addAttribute("units", service.getBloodUnits(bloodGroup).stream()
                .filter(u -> "Approved - Available".equals(u.getStatus())).toList());
        model.addAttribute("bloodGroup", bloodGroup);
        model.addAttribute("myRequests", service.getRequestsForRequester(user.getUsername()));
        model.addAttribute("newRequest", new Request());
        return "portal/recipient";
    }

    @PostMapping("/recipient/request")
    public String submitBloodRequest(@ModelAttribute Request request,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime neededByDate,
                                     HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "SUBMIT_REQUEST");
            request.setRequesterUsername(user.getUsername());
            request.setNeededBy(neededByDate);
            request.setUrgency("Urgent");
            Request saved = service.createRequest(request);
            authService.log(user.getUsername(), "SUBMIT_REQUEST", "Blood request " + saved.getId() + " submitted");
            redirects.addFlashAttribute("success", "Request submitted! Request ID: " + saved.getId() + ". Status: " + saved.getStatus());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/recipient";
    }

    @PostMapping("/recipient/request/{id}/cancel")
    public String cancelRecipientRequest(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "TRACK_REQUEST");
            service.cancelRequest(id, user.getUsername());
            authService.log(user.getUsername(), "CANCEL_REQUEST", "Cancelled request " + id);
            redirects.addFlashAttribute("success", "Your request was cancelled.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/recipient";
    }

    // UC-09 / PBI-09 — Hospital Coordinator Portal
    @GetMapping("/hospital")
    public String hospitalPortal(Model model, HttpSession session) {
        UserAccount user = requirePortalAccess(session, UserRole.HOSPITAL_COORDINATOR, "SUBMIT_REQUISITION", "TRACK_DISPATCH", "MANAGE_TRANSFER", "VIEW_AVAILABILITY");
        model.addAttribute("user", user);
        model.addAttribute("userPermissions", authService.getUserPermissions(user));
        model.addAttribute("accessiblePortals", authService.getAccessiblePortalKeys(user));
        model.addAttribute("requisitions", service.getRequestsForHospital(user.getHospital()).stream()
                .filter(r -> "Hospital Requisition".equals(r.getRequestType())).toList());
        model.addAttribute("newRequisition", new Request());
        return "portal/hospital";
    }

    @GetMapping("/hospital/records")
    public String hospitalRecords(Model model, HttpSession session) {
        UserAccount user = requirePortalAccess(session, UserRole.HOSPITAL_COORDINATOR, "SUBMIT_REQUISITION", "TRACK_DISPATCH");
        model.addAttribute("user", user);
        model.addAttribute("userPermissions", authService.getUserPermissions(user));
        model.addAttribute("accessiblePortals", authService.getAccessiblePortalKeys(user));
        model.addAttribute("requisitions", service.getRequestsForHospital(user.getHospital()).stream()
                .filter(r -> "Hospital Requisition".equals(r.getRequestType())).toList());
        model.addAttribute("newRequisition", new Request());
        return "portal/hospital-records";
    }

    @PostMapping("/hospital/requisition")
    public String submitRequisition(@ModelAttribute Request req,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime neededByDate,
                                    HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "SUBMIT_REQUISITION");
            req.setRequesterUsername(user.getUsername());
            req.setNeededBy(neededByDate);
            if (req.getHospital() == null || req.getHospital().isBlank()) {
                req.setHospital(user.getHospital() != null ? user.getHospital() : "General Hospital");
            }
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

    @PostMapping("/hospital/requisition/{id}/cancel")
    public String cancelHospitalRequisition(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "TRACK_DISPATCH");
            service.cancelRequest(id, user.getUsername());
            authService.log(user.getUsername(), "CANCEL_REQUISITION", "Cancelled requisition " + id);
            redirects.addFlashAttribute("success", "Your hospital requisition was cancelled.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/hospital";
    }

    @PostMapping("/hospital/records")
    public String createHospitalRecord(@ModelAttribute Request req,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime neededByDate,
                                       HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "SUBMIT_REQUISITION");
            req.setRequesterUsername(user.getUsername());
            req.setHospital(user.getHospital() != null ? user.getHospital() : "General Hospital");
            req.setNeededBy(neededByDate);
            Request saved = service.createHospitalRequisition(req);
            authService.log(user.getUsername(), "CREATE_REQUISITION", "Created requisition " + saved.getId());
            redirects.addFlashAttribute("success", "Hospital requisition created successfully.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/hospital/records";
    }

    @PostMapping("/hospital/records/{id}/update")
    public String updateHospitalRecord(@PathVariable String id,
                                       @RequestParam String patientName,
                                       @RequestParam String bloodGroup,
                                       @RequestParam String component,
                                       @RequestParam String urgency,
                                       @RequestParam String city,
                                       @RequestParam int units,
                                       @RequestParam String contactPhone,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime neededBy,
                                       @RequestParam(required = false) String notes,
                                       HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "SUBMIT_REQUISITION");
            Request changes = new Request();
            changes.setId(id);
            changes.setPatientName(patientName);
            changes.setBloodGroup(bloodGroup);
            changes.setComponent(component);
            changes.setUrgency(urgency);
            changes.setCity(city);
            changes.setUnits(units);
            changes.setContactPhone(contactPhone);
            changes.setNeededBy(neededBy);
            changes.setNotes(notes);
            service.updateHospitalRequisition(changes, user.getUsername());
            authService.log(user.getUsername(), "UPDATE_REQUISITION", "Updated requisition " + id);
            redirects.addFlashAttribute("success", "Hospital requisition updated successfully.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/hospital/records";
    }

    @PostMapping("/hospital/records/{id}/delete")
    public String deleteHospitalRecord(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "SUBMIT_REQUISITION");
            service.deleteHospitalRequisition(id, user.getUsername());
            authService.log(user.getUsername(), "DELETE_REQUISITION", "Deleted requisition " + id);
            redirects.addFlashAttribute("success", "Hospital requisition deleted successfully.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/hospital/records";
    }

    @GetMapping("/hospital/check-availability")
    @ResponseBody
    public String checkAvailability(@RequestParam String bloodGroup, @RequestParam String component, HttpSession session) {
        requireAnyPermission(session, "VIEW_AVAILABILITY", "SUBMIT_REQUISITION");
        long count = service.countAvailableUnits(bloodGroup, component, null);
        return count + " units available for " + bloodGroup + " (" + component + ")";
    }

    // UC-17 / PBI-17 — QA Officer Portal
    @GetMapping("/qa")
    public String qaPortal(@RequestParam(required = false) String unitId, Model model, HttpSession session) {
        UserAccount user = requirePortalAccess(session, UserRole.QA_OFFICER, "VIEW_INVENTORY", "LOG_SCREENING", "VERIFY_EXPIRY", "LOG_DISCARD");
        model.addAttribute("user", user);
        model.addAttribute("userPermissions", authService.getUserPermissions(user));
        model.addAttribute("accessiblePortals", authService.getAccessiblePortalKeys(user));
        model.addAttribute("units", service.getBloodUnits(null));
        if (unitId != null) {
            service.getBloodUnits(null).stream().filter(u -> u.getId().equals(unitId)).findFirst()
                    .ifPresent(u -> model.addAttribute("selectedUnit", u));
        }
        return "portal/qa";
    }

    @PostMapping("/qa/screening")
    public String logScreening(@RequestParam String unitId, @RequestParam boolean hiv, @RequestParam boolean hepB,
                               @RequestParam boolean hepC, @RequestParam boolean syphilis, @RequestParam boolean malaria,
                               @RequestParam(required = false) String notes, @RequestParam(required = false) Boolean crossMatch,
                               HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "LOG_SCREENING");
            BloodUnit unit = service.recordScreening(unitId, hiv, hepB, hepC, syphilis, malaria, notes);
            authService.log(user.getUsername(), "LOG_SCREENING", "Unit " + unitId + " → " + unit.getStatus());
            redirects.addFlashAttribute("success", "Screening recorded. Unit status: " + unit.getStatus());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/qa?unitId=" + unitId;
    }

    @GetMapping("/qa/records")
    public String qaRecords(Model model, HttpSession session) {
        UserAccount user = requirePortalAccess(session, UserRole.QA_OFFICER, "VIEW_INVENTORY", "LOG_SCREENING", "VERIFY_EXPIRY", "LOG_DISCARD");
        model.addAttribute("user", user);
        model.addAttribute("userPermissions", authService.getUserPermissions(user));
        model.addAttribute("accessiblePortals", authService.getAccessiblePortalKeys(user));
        model.addAttribute("units", service.getBloodUnits(null));
        model.addAttribute("newUnit", new BloodUnit());
        return "portal/qa-records";
    }

    @PostMapping("/qa/records")
    public String createQABloodUnit(@ModelAttribute BloodUnit unit, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "LOG_SCREENING");
            BloodUnit created = service.createBloodUnit(unit);
            authService.log(user.getUsername(), "CREATE_QA_RECORD", "Created blood unit " + created.getId() + " for " + created.getBloodGroup() + " / " + created.getComponent());
            redirects.addFlashAttribute("success", "Blood unit record created successfully.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/qa/records";
    }

    @PostMapping("/qa/records/{id}/update")
    public String updateQABloodUnit(@PathVariable String id,
                                   @RequestParam String bloodGroup,
                                   @RequestParam String component,
                                   @RequestParam String branch,
                                   @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime expiresAt,
                                   @RequestParam String status,
                                   @RequestParam(required = false) String qualityNotes,
                                   HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "LOG_SCREENING");
            BloodUnit existing = service.getBloodUnitById(id).orElseThrow(() -> new Exception("Blood unit not found"));
            existing.setBloodGroup(bloodGroup);
            existing.setComponent(component);
            existing.setBranch(branch);
            existing.setExpiresAt(expiresAt);
            existing.setStatus(status);
            existing.setQualityNotes(qualityNotes);
            BloodUnit updated = service.updateBloodUnit(existing);
            authService.log(user.getUsername(), "UPDATE_QA_RECORD", "Updated blood unit " + updated.getId() + " → " + updated.getStatus());
            redirects.addFlashAttribute("success", "Blood unit record updated successfully.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/qa/records";
    }

    @PostMapping("/qa/records/{id}/delete")
    public String deleteQABloodUnit(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "LOG_SCREENING");
            service.deleteBloodUnit(id);
            authService.log(user.getUsername(), "DELETE_QA_RECORD", "Deleted blood unit " + id);
            redirects.addFlashAttribute("success", "Blood unit record removed.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/qa/records";
    }

    // UC-02 / PBI-01 — Delivery Coordination Portal
    @GetMapping("/delivery")
    public String deliveryPortal(Model model, HttpSession session) {
        UserAccount user = requirePortalAccess(session, UserRole.DELIVERY_OFFICER, "REGISTER_VEHICLE", "REGISTER_COURIER", "SCHEDULE_DELIVERY", "CONFIRM_DELIVERY", "CANCEL_DELIVERY");
        model.addAttribute("user", user);
        model.addAttribute("userPermissions", authService.getUserPermissions(user));
        model.addAttribute("accessiblePortals", authService.getAccessiblePortalKeys(user));
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
            UserAccount user = requirePermission(session, "REGISTER_VEHICLE");
            service.registerVehicle(vehicle);
            authService.log(user.getUsername(), "REGISTER_VEHICLE", "Vehicle " + vehicle.getVehicleNumber());
            redirects.addFlashAttribute("success", "Vehicle registered: " + vehicle.getVehicleNumber());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/delivery";
    }

    @PostMapping("/delivery/couriers")
    public String registerCourier(@ModelAttribute DeliveryCourier courier, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "REGISTER_COURIER");
            service.registerCourier(courier);
            authService.log(user.getUsername(), "REGISTER_COURIER", "Courier " + courier.getName());
            redirects.addFlashAttribute("success", "Courier registered: " + courier.getName());
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
            UserAccount user = requirePermission(session, "SCHEDULE_DELIVERY");
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
            UserAccount user = requireAnyPermission(session, "CONFIRM_DELIVERY", "CANCEL_DELIVERY");
            service.updateDeliveryStatus(id, status);
            authService.log(user.getUsername(), "UPDATE_DELIVERY", "Delivery " + id + " → " + status);
            redirects.addFlashAttribute("success", "Delivery status updated to: " + status);
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/delivery";
    }

    // UC-13 / PBI-13 / PBI-16 — Blood Bank Manager Portal
    @GetMapping("/manager")
    public String managerPortal(@RequestParam(required = false) String branch, Model model, HttpSession session) {
        UserAccount user = requirePortalAccess(session, UserRole.BLOOD_BANK_MANAGER, "VIEW_STOCK", "APPROVE_REQUEST", "EMERGENCY_DISPATCH", "LOW_STOCK_ALERT", "VIEW_REPORTS");
        model.addAttribute("user", user);
        model.addAttribute("userPermissions", authService.getUserPermissions(user));
        model.addAttribute("accessiblePortals", authService.getAccessiblePortalKeys(user));
        model.addAttribute("stockSummary", service.getStockDashboard(branch));
        model.addAttribute("lowStockAlerts", service.getLowStockAlerts());
        model.addAttribute("pendingRequests", service.getRequests("Pending", null, null));
        model.addAttribute("submittedRequisitions", service.getRequests("Submitted", null, null));
        model.addAttribute("branchFilter", branch);
        return "portal/manager";
    }

    @GetMapping("/manager/records")
    public String managerRecords(Model model, HttpSession session) {
        UserAccount user = requirePortalAccess(session, UserRole.BLOOD_BANK_MANAGER, "VIEW_STOCK");
        model.addAttribute("user", user);
        model.addAttribute("userPermissions", authService.getUserPermissions(user));
        model.addAttribute("accessiblePortals", authService.getAccessiblePortalKeys(user));
        model.addAttribute("units", service.getBloodUnits(null));
        model.addAttribute("newUnit", new BloodUnit());
        return "portal/manager-records";
    }

    @PostMapping("/manager/records")
    public String createManagerBloodUnit(@ModelAttribute BloodUnit unit, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "VIEW_STOCK");
            BloodUnit created = service.createBloodUnit(unit);
            authService.log(user.getUsername(), "CREATE_MANAGER_STOCK_RECORD", "Created blood unit " + created.getId());
            redirects.addFlashAttribute("success", "Stock record created successfully.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/manager/records";
    }

    @PostMapping("/manager/records/{id}/update")
    public String updateManagerBloodUnit(@PathVariable String id,
                                         @RequestParam String bloodGroup,
                                         @RequestParam String component,
                                         @RequestParam String branch,
                                         @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime expiresAt,
                                         @RequestParam String status,
                                         @RequestParam(required = false) String qualityNotes,
                                         HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "VIEW_STOCK");
            BloodUnit existing = service.getBloodUnitById(id).orElseThrow(() -> new Exception("Blood unit not found"));
            existing.setBloodGroup(bloodGroup);
            existing.setComponent(component);
            existing.setBranch(branch);
            existing.setExpiresAt(expiresAt);
            existing.setStatus(status);
            existing.setQualityNotes(qualityNotes);
            service.updateBloodUnit(existing);
            authService.log(user.getUsername(), "UPDATE_MANAGER_STOCK_RECORD", "Updated blood unit " + id);
            redirects.addFlashAttribute("success", "Stock record updated successfully.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/manager/records";
    }

    @PostMapping("/manager/records/{id}/delete")
    public String deleteManagerBloodUnit(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requirePermission(session, "VIEW_STOCK");
            service.deleteBloodUnit(id);
            authService.log(user.getUsername(), "DELETE_MANAGER_STOCK_RECORD", "Deleted blood unit " + id);
            redirects.addFlashAttribute("success", "Stock record removed.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/manager/records";
    }

    @PostMapping("/manager/approve/{id}")
    public String approveRequest(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireAnyPermission(session, "APPROVE_REQUEST", "EMERGENCY_DISPATCH");
            Request req = service.approveRequest(id);
            authService.log(user.getUsername(), "APPROVE_REQUEST", "Request " + id + " approved");
            redirects.addFlashAttribute("success", "Request " + req.getId() + " approved.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/manager";
    }

    // UC-21 / PBI-21 — IT Operations Portal
    @GetMapping("/it")
    public String itPortal(Model model, HttpSession session) {
        UserAccount user = requireRole(session, UserRole.IT_ADMIN);
        populateItManagementModel(model, user);
        return "portal/it";
    }

    @GetMapping("/it/members")
    public String itMembersPortal(Model model, HttpSession session) {
        UserAccount user = requireRole(session, UserRole.IT_ADMIN);
        populateItManagementModel(model, user);
        return "portal/it-members";
    }

    private void populateItManagementModel(Model model, UserAccount user) {
        List<UserAccount> allUsers = authService.getAllUsers();

        Map<String, Set<String>> userPermsMap = new HashMap<>();
        Map<String, Boolean> isCustomizedMap = new HashMap<>();
        Map<String, List<String>> accessiblePortalsMap = new HashMap<>();
        for (UserAccount u : allUsers) {
            userPermsMap.put(u.getId(), authService.getUserPermissions(u));
            isCustomizedMap.put(u.getId(), u.getPermissions() != null && !u.getPermissions().trim().isEmpty());
            accessiblePortalsMap.put(u.getId(), authService.getAccessiblePortals(u));
        }

        model.addAttribute("user", user);
        model.addAttribute("users", allUsers);
        model.addAttribute("userPermsMap", userPermsMap);
        model.addAttribute("isCustomizedMap", isCustomizedMap);
        model.addAttribute("accessiblePortalsMap", accessiblePortalsMap);
        model.addAttribute("permissions", authService.getAllPermissions());
        model.addAttribute("auditLogs", authService.getRecentAuditLogs());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("userPermissions", authService.getUserPermissions(user));
        model.addAttribute("accessiblePortals", authService.getAccessiblePortalKeys(user));
        model.addAttribute("groupedPermissions", AppPermission.getGroupedByCategory());
        model.addAttribute("allAppPermissions", AppPermission.values());
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
        return "redirect:/portal/it/members";
    }

    @PostMapping("/it/role-permissions")
    public String updateRolePermissionsFromBoxes(@RequestParam UserRole role,
                                                 @RequestParam(name = "permissions", required = false) List<String> permissions,
                                                 HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount user = requireRole(session, UserRole.IT_ADMIN);
            if (permissions == null) permissions = Collections.emptyList();
            authService.updateRolePermissions(role, permissions, user.getUsername());
            redirects.addFlashAttribute("success", "Default permissions updated for role " + role.getDisplayName());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/it/members";
    }

    @PostMapping("/it/users/{id}/permissions")
    public String updateMemberPermissions(@PathVariable String id,
                                          @RequestParam(name = "permissions", required = false) List<String> permissions,
                                          HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount actor = requireRole(session, UserRole.IT_ADMIN);
            if (permissions == null) permissions = Collections.emptyList();
            UserAccount updated = authService.updateUserPermissions(id, permissions, actor.getUsername());
            redirects.addFlashAttribute("success", "Access permissions updated for " + updated.getFullName() +
                    " (@" + updated.getUsername() + "). (" + permissions.size() + " permissions granted)");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/it/members";
    }

    @PostMapping("/it/users/{id}/reset-permissions")
    public String resetMemberPermissions(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount actor = requireRole(session, UserRole.IT_ADMIN);
            UserAccount updated = authService.resetUserPermissions(id, actor.getUsername());
            redirects.addFlashAttribute("success", "Permissions reset to role defaults for @" + updated.getUsername());
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/it/members";
    }

    @PostMapping("/it/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount actor = requireRole(session, UserRole.IT_ADMIN);
            UserAccount updated = authService.toggleUserActive(id, actor.getUsername());
            redirects.addFlashAttribute("success", "Account status for @" + updated.getUsername() + " changed to " + (updated.isActive() ? "Active" : "Disabled"));
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/it/members";
    }

    @PostMapping("/it/users/{id}/delete")
    public String deleteUser(@PathVariable String id, HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount actor = requireRole(session, UserRole.IT_ADMIN);
            UserAccount target = authService.getUserById(id).orElseThrow(() -> new Exception("User not found with ID: " + id));
            authService.deleteUser(id, actor.getUsername());
            redirects.addFlashAttribute("success", "Member @" + target.getUsername() + " was permanently removed from the system.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/it/members";
    }

    @PostMapping("/it/users")
    public String createUser(@RequestParam String username, @RequestParam String password,
                             @RequestParam String fullName, @RequestParam UserRole role,
                             @RequestParam(required = false) String hospital,
                             @RequestParam(name = "permissions", required = false) List<String> permissions,
                             HttpSession session, RedirectAttributes redirects) {
        try {
            UserAccount actor = requireRole(session, UserRole.IT_ADMIN);
            UserAccount created = authService.createUser(username, password, fullName, role, hospital, permissions);
            authService.log(actor.getUsername(), "CREATE_USER", "Created user " + username + " (" + role + ")");
            redirects.addFlashAttribute("success", "User account created: " + created.getUsername() +
                    " with " + (permissions != null ? permissions.size() : "role default") + " permissions.");
        } catch (Exception e) {
            redirects.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/portal/it";
    }
}
Commit at 2026-10-02T12:06:02
Commit at 2026-10-04T10:54:33
Commit at 2026-10-02T19:50:38
Commit at 2026-09-25T04:35:13
Commit at 2026-10-02T20:26:15
Commit at 2026-09-30T20:13:06
Commit at 2026-10-01T05:10:46
Commit at 2026-09-25T09:40:09
Commit at 2026-09-29T19:47:07
Commit at 2026-10-02T01:42:21
Commit at 2026-10-04T17:18:10
Commit at 2026-10-02T12:57:37
Commit at 2026-10-04T07:27:49
Commit at 2026-10-05T02:28:31
Commit at 2026-10-04T14:49:49
Commit at 2026-09-29T21:30:56
Commit at 2026-09-25T19:55:53
Commit at 2026-09-27T17:13:43
Commit at 2026-10-04T09:38:02
Commit at 2026-10-05T07:36:11
Commit at 2026-09-28T10:37:31
Commit at 2026-09-26T03:02:07
Commit at 2026-09-25T16:11:22
Commit at 2026-10-02T06:47:43
Commit at 2026-09-28T00:23:44
Commit at 2026-09-30T18:59:04
Commit at 2026-10-03T09:46:50

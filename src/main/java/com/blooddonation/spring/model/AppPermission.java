package com.blooddonation.spring.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public enum AppPermission {
    // Recipient Portal Permissions
    VIEW_AVAILABILITY("VIEW_AVAILABILITY", "View Blood Availability", "Recipient & Patient Services", "Search and view available screened blood units"),
    SUBMIT_REQUEST("SUBMIT_REQUEST", "Submit Blood Requests", "Recipient & Patient Services", "Create and submit urgent patient blood requests"),
    TRACK_REQUEST("TRACK_REQUEST", "Track Request Status", "Recipient & Patient Services", "Monitor the progress and status of submitted requests"),

    // Hospital Coordinator Permissions
    SUBMIT_REQUISITION("SUBMIT_REQUISITION", "Submit Hospital Requisitions", "Hospital Coordination", "Submit hospital blood requisitions for patients and wards"),
    TRACK_DISPATCH("TRACK_DISPATCH", "Track Requisitions & Dispatch", "Hospital Coordination", "View dispatch statuses and tracking details for hospital orders"),
    MANAGE_TRANSFER("MANAGE_TRANSFER", "Manage Blood Transfers", "Hospital Coordination", "Coordinate inter-facility blood transfers and requisitions"),

    // Quality Assurance Permissions
    VIEW_INVENTORY("VIEW_INVENTORY", "View Blood Inventory", "Quality Assurance", "Inspect blood units inventory awaiting screening or quarantine"),
    LOG_SCREENING("LOG_SCREENING", "Log Screening Results", "Quality Assurance", "Record infectious marker tests (HIV, HepB, HepC, Syphilis, Malaria)"),
    VERIFY_EXPIRY("VERIFY_EXPIRY", "Verify Expiry Dates", "Quality Assurance", "Inspect expiry dates and compliance verification"),
    LOG_DISCARD("LOG_DISCARD", "Log Discarded Units", "Quality Assurance", "Record disposal and non-compliance notes for defective units"),

    // Delivery & Logistics Permissions
    REGISTER_VEHICLE("REGISTER_VEHICLE", "Register Delivery Vehicles", "Delivery & Logistics", "Register and manage cold-chain refrigerated delivery vehicles"),
    REGISTER_COURIER("REGISTER_COURIER", "Register Delivery Couriers", "Delivery & Logistics", "Register and maintain delivery drivers and courier dispatchers"),
    SCHEDULE_DELIVERY("SCHEDULE_DELIVERY", "Schedule Blood Deliveries", "Delivery & Logistics", "Schedule driver and vehicle dispatches for approved requisitions"),
    CONFIRM_DELIVERY("CONFIRM_DELIVERY", "Confirm Deliveries", "Delivery & Logistics", "Mark blood dispatches as safely delivered to destination"),
    CANCEL_DELIVERY("CANCEL_DELIVERY", "Cancel Delivery Orders", "Delivery & Logistics", "Cancel dispatch orders in case of logistical impediments"),

    // Blood Bank Management Permissions
    VIEW_STOCK("VIEW_STOCK", "Monitor Stock Dashboard", "Blood Bank Management", "View real-time stock levels grouped by group, component, and branch"),
    APPROVE_REQUEST("APPROVE_REQUEST", "Approve Patient Requests", "Blood Bank Management", "Review and approve urgent patient blood requests"),
    EMERGENCY_DISPATCH("EMERGENCY_DISPATCH", "Emergency Requisition Approvals", "Blood Bank Management", "Approve and authorize hospital requisitions and emergency dispatches"),
    LOW_STOCK_ALERT("LOW_STOCK_ALERT", "View Low Stock Alerts", "Blood Bank Management", "Monitor automated low inventory warnings across branches"),
    VIEW_REPORTS("VIEW_REPORTS", "View Stock Reports", "Blood Bank Management", "Generate and review inventory summary reports"),

    // IT Operations & Security Permissions
    MANAGE_USERS("MANAGE_USERS", "Manage User Accounts", "IT Administration", "Create new system accounts, activate or deactivate members"),
    MANAGE_PERMISSIONS("MANAGE_PERMISSIONS", "Manage Member Permissions", "IT Administration", "Configure granular access permissions for individual members and roles"),
    VIEW_AUDIT("VIEW_AUDIT", "View Activity Audit Logs", "IT Administration", "Inspect system security logs, action histories, and administrative events"),
    CONFIGURE_SYSTEM("CONFIGURE_SYSTEM", "Configure System Settings", "IT Administration", "Manage system parameters, integrations, and operational policies");

    private final String code;
    private final String label;
    private final String category;
    private final String description;

    AppPermission(String code, String label, String category, String description) {
        this.code = code;
        this.label = label;
        this.category = category;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }

    public static Optional<AppPermission> fromCode(String code) {
        if (code == null) return Optional.empty();
        for (AppPermission p : values()) {
            if (p.getCode().equalsIgnoreCase(code.trim())) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public static Map<String, List<AppPermission>> getGroupedByCategory() {
        Map<String, List<AppPermission>> map = new LinkedHashMap<>();
        for (AppPermission p : values()) {
            map.computeIfAbsent(p.getCategory(), k -> new ArrayList<>()).add(p);
        }
        return map;
    }
}
Commit at 2026-10-02T12:06:02
Commit at 2026-10-04T10:54:33
Commit at 2026-10-02T19:50:38

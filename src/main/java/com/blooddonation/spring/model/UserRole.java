package com.blooddonation.spring.model;

public enum UserRole {
    RECIPIENT("Recipient / Patient"),
    HOSPITAL_COORDINATOR("Hospital Coordinator"),
    QA_OFFICER("Quality Assurance Officer"),
    DELIVERY_OFFICER("Delivery Coordination Officer"),
    BLOOD_BANK_MANAGER("Blood Bank Manager"),
    IT_ADMIN("IT Operations Supervisor");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
Commit at 2026-08-27T09:28:16
Commit at 2026-08-16T04:44:50
Commit at 2026-08-04T10:17:35
Commit at 2026-09-15T13:21:43
Commit at 2026-08-08T19:01:37

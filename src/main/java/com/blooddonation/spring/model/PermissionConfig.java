package com.blooddonation.spring.model;

import jakarta.persistence.*;

@Entity
@Table(name = "permission_configs")
public class PermissionConfig {

    @Id
    @Column(length = 30)
    private String role;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String permissions;

    public PermissionConfig() {}

    public PermissionConfig(String role, String permissions) {
        this.role = role;
        this.permissions = permissions;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
}
Commit at 2026-10-02T12:06:02
Commit at 2026-10-04T10:54:33
Commit at 2026-10-02T19:50:38
Commit at 2026-09-25T04:35:13

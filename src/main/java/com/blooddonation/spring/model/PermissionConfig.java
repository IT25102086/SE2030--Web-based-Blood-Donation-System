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
Commit at 2026-08-27T09:28:16
Commit at 2026-08-16T04:44:50
Commit at 2026-08-04T10:17:35
Commit at 2026-09-15T13:21:43
Commit at 2026-08-08T19:01:37
Commit at 2026-08-23T07:40:37
Commit at 2026-08-06T20:15:36
Commit at 2026-08-04T03:30:23
Commit at 2026-08-16T23:49:26
Commit at 2026-08-31T01:18:15
Commit at 2026-08-10T18:31:13
Commit at 2026-08-29T03:35:22

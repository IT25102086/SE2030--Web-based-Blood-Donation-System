# IT25102086 — IT Operations Supervisor
## BloodConnect Group Project — Role Contribution

**Student ID:** IT25102086  
**Assigned Role:** IT Operations Supervisor  
**Portal URL:** `http://localhost:8089/portal/it`  
**Demo Login:** username `itadmin` / password `pass123`

---

## Role Overview

The IT Operations Supervisor is the system administrator with **full access to all portals and all permissions**. This role:

- Creates and manages all user accounts (all 6 roles)
- Configures role-based default permissions
- Customizes individual member permissions beyond their role defaults
- Activates and deactivates user accounts
- Views comprehensive system audit logs
- Manages the entire technical infrastructure of the platform

The IT Admin bypass rule: `if (user.getRole() == UserRole.IT_ADMIN) return user;` — IT admins skip all permission checks and can access every portal and action in the system.

---

## My Permissions

| Permission Code | Label | Description |
|---|---|---|
| `MANAGE_USERS` | Manage User Accounts | Create new system accounts, activate or deactivate members |
| `MANAGE_PERMISSIONS` | Manage Member Permissions | Configure granular access permissions for individuals and roles |
| `VIEW_AUDIT` | View Activity Audit Logs | Inspect security logs, action histories, and admin events |
| `CONFIGURE_SYSTEM` | Configure System Settings | Manage system parameters, integrations, and operational policies |

---

## Backend Files & Methods

### PortalController.java
**Path:** `src/main/java/com/blooddonation/spring/controller/PortalController.java`

| Method | HTTP | URL | Lines | Description |
|---|---|---|---|---|
| `itPortal()` | GET | `/portal/it` | 509 – 514 | Load IT main console — user list + audit logs |
| `itMembersPortal()` | GET | `/portal/it/members` | 516 – 521 | Load member permission management view |
| `populateItManagementModel()` | — | — | 523 – 547 | Private: builds the full model for IT views |
| `updatePermissions()` | POST | `/portal/it/permissions` | 549 – 560 | Update default permissions for an entire role (text field) |
| `updateRolePermissionsFromBoxes()` | POST | `/portal/it/role-permissions` | 562 – 575 | Update role default permissions via checkbox UI |
| `updateMemberPermissions()` | POST | `/portal/it/users/{id}/permissions` | 577 – 591 | Set custom permissions for a specific member |
| `resetMemberPermissions()` | POST | `/portal/it/users/{id}/reset-permissions` | 593 – 603 | Reset member to their role's default permissions |
| `toggleUserStatus()` | POST | `/portal/it/users/{id}/toggle-status` | 605 – 615 | Activate or deactivate a user account |
| `deleteUser()` | POST | `/portal/it/users/{id}/delete` | 617 – 628 | Permanently remove a user from the system |
| `createUser()` | POST | `/portal/it/users` | 630 – 646 | Create new user account with role and optional permissions |

### AuthController.java — Full Ownership
**Path:** `src/main/java/com/blooddonation/spring/controller/AuthController.java`

| Method | HTTP | URL | Description |
|---|---|---|---|
| `loginPage()` | GET | `/login` | Render staff login page |
| `login()` | POST | `/login` | Authenticate user + redirect to their portal |
| `logout()` | GET | `/logout` | Invalidate session, redirect to login |

### AdminController.java — Full Ownership
**Path:** `src/main/java/com/blooddonation/spring/controller/AdminController.java`

Full admin panel: dashboard, donor management, inventory, QA screening, requests, deliveries, contact messages.

### WebController.java — Full Ownership
**Path:** `src/main/java/com/blooddonation/spring/controller/WebController.java`

All public-facing routes: home (`/`), `/availability`, `/search`, `/register`, `/donor/{id}`, `/requests`, `/contact`.

### AuthService.java — Full Ownership
**Path:** `src/main/java/com/blooddonation/spring/service/AuthService.java`

The complete authentication and authorization engine. Key responsibilities:

| Section | Description |
|---|---|
| `DEFAULT_PERMISSIONS` map | Defines default permission strings for all 6 roles |
| `PORTAL_PERMISSIONS` map | Defines which permissions grant access to each portal |
| `PORTAL_LABELS` map | Human-readable portal names |
| `seedUsersAndPermissions()` | Creates initial PermissionConfig rows + 6 demo user accounts |
| `authenticate()` | BCrypt password verification |
| `getRolePermissions()` | Loads role permissions from DB or defaults |
| `getUserPermissions()` | Gets effective permissions (custom or role default) |
| `hasPermission()` | Single permission check with IT_ADMIN bypass |
| `hasPortalAccess()` | Checks if user can access a portal |
| `getAccessiblePortals()` | Lists all accessible portals for nav rendering |
| `updatePermissions()` | Update role-level default permissions |
| `updateRolePermissions()` | Update role permissions from checkbox list |
| `updateUserPermissions()` | Set custom permissions for a specific member |
| `resetUserPermissions()` | Clear custom permissions → fall back to role defaults |
| `toggleUserActive()` | Enable/disable a user account |
| `createUser()` | Create new user with optional custom permissions |
| `deleteUser()` | Permanently delete a user account |
| `log()` | Write an audit log entry |
| `getRecentAuditLogs()` | Retrieve last 50 audit events |
| `getPortalPath()` | Determine redirect path after login |

### Config Files — Full Ownership

| File | Path | Purpose |
|---|---|---|
| `AdminInterceptor.java` | `src/main/java/com/blooddonation/spring/config/AdminInterceptor.java` | Protects `/admin/**` routes |
| `PortalAuthInterceptor.java` | `src/main/java/com/blooddonation/spring/config/PortalAuthInterceptor.java` | Protects `/portal/**` routes |
| `PortalExceptionHandler.java` | `src/main/java/com/blooddonation/spring/config/PortalExceptionHandler.java` | Handles RuntimeException in portal (access denied, etc.) |
| `WebConfig.java` | `src/main/java/com/blooddonation/spring/config/WebConfig.java` | Registers interceptors, configures MVC |

### Models — Full Ownership

| File | Path | Description |
|---|---|---|
| `AppPermission.java` | `src/main/java/com/blooddonation/spring/model/AppPermission.java` | All 22 permission definitions grouped by category |
| `UserRole.java` | `src/main/java/com/blooddonation/spring/model/UserRole.java` | All 6 user roles with display names |
| `UserAccount.java` | `src/main/java/com/blooddonation/spring/model/UserAccount.java` | User entity (id, username, passwordHash, role, hospital, permissions, active) |
| `PermissionConfig.java` | `src/main/java/com/blooddonation/spring/model/PermissionConfig.java` | Role-level permission config stored in DB |
| `AuditLog.java` | `src/main/java/com/blooddonation/spring/model/AuditLog.java` | Audit log entity (username, action, details, timestamp) |

### Repositories — Full Ownership

| File | Path |
|---|---|
| `UserAccountRepository.java` | `src/main/java/com/blooddonation/spring/repository/UserAccountRepository.java` |
| `PermissionConfigRepository.java` | `src/main/java/com/blooddonation/spring/repository/PermissionConfigRepository.java` |
| `AuditLogRepository.java` | `src/main/java/com/blooddonation/spring/repository/AuditLogRepository.java` |

### Infrastructure Files — Full Ownership

| File | Purpose |
|---|---|
| `BloodDonationApplication.java` | Spring Boot main entry point — loads native SQL Server DLL |
| `pom.xml` | Maven build config — Java 17, Spring Boot 3.2.5, SQL Server JDBC, BCrypt |
| `src/main/resources/application.properties` | Server port, datasource URL, JPA settings, Thymeleaf config |
| `run.bat` | Windows startup helper — finds JDK/Maven and launches the app |

---

## Frontend Files

| File | Path | Purpose |
|---|---|---|
| `it.html` | `src/main/resources/templates/portal/it.html` | IT main console — create users, view audit logs |
| `it-members.html` | `src/main/resources/templates/portal/it-members.html` | Member permission management — role defaults + per-member customization |
| `_nav.html` | `src/main/resources/templates/portal/_nav.html` | Shared portal navigation sidebar fragment |
| `login.html` | `src/main/resources/templates/login.html` | Staff login page |
| `layout.html` | `src/main/resources/templates/layout.html` | Shared base Thymeleaf layout |
| `error.html` | `src/main/resources/templates/error.html` | Global error page |
| `index.html` | `src/main/resources/templates/index.html` | Public home dashboard |
| `contact.html` | `src/main/resources/templates/contact.html` | Public contact form |
| `search.html` | `src/main/resources/templates/search.html` | Public donor search |
| `donor-profile.html` | `src/main/resources/templates/donor-profile.html` | Public donor profile |
| `register.html` | `src/main/resources/templates/register.html` | Public donor registration |
| `admin/login.html` | `src/main/resources/templates/admin/login.html` | Admin login page |
| `admin/dashboard.html` | `src/main/resources/templates/admin/dashboard.html` | Admin system dashboard |
| `admin/donors.html` | `src/main/resources/templates/admin/donors.html` | Admin donor management |
| `admin/donor-edit.html` | `src/main/resources/templates/admin/donor-edit.html` | Admin donor edit form |
| `admin/messages.html` | `src/main/resources/templates/admin/messages.html` | Admin contact message inbox |
| `admin/requests.html` | `src/main/resources/templates/admin/requests.html` | Admin request management |
| `static/css/style.css` | `src/main/resources/static/css/style.css` | Global stylesheet |
| `static/js/app.js` | `src/main/resources/static/js/app.js` | Global JavaScript |

---

## Key Use Cases

| Use Case | Action | Portal |
|---|---|---|
| UC-21 | Create user accounts for team members | POST `/portal/it/users` |
| PBI-21 | Manage role-level permissions | POST `/portal/it/role-permissions` |
| — | Customize individual member permissions | POST `/portal/it/users/{id}/permissions` |
| — | Reset member to role defaults | POST `/portal/it/users/{id}/reset-permissions` |
| — | Enable/disable user accounts | POST `/portal/it/users/{id}/toggle-status` |
| — | View system audit log | GET `/portal/it` |

---

## Permission System Architecture

```text
Role Default Permissions (PermissionConfig table)
  └── e.g., RECIPIENT → "VIEW_AVAILABILITY,SUBMIT_REQUEST,TRACK_REQUEST"

Member Override Permissions (UserAccount.permissions field)
  └── If set: overrides role defaults entirely
  └── If blank/null: falls back to role defaults

IT Admin Portal (/portal/it/members)
  ├── Update role defaults (applies to all members of that role)
  ├── Grant custom permissions to specific members
  └── Reset member to role defaults
```

---

## Seeded Demo Accounts (all use password: `pass123`)

| Username | Role | Full Name |
|---|---|---|
| `recipient` | Recipient / Patient | Weerasuriya M.W.K.C.P. |
| `hospital` | Hospital Coordinator | Piumal K.P.N. |
| `qa` | QA Officer | Rubasinghe S.S. |
| `delivery` | Delivery Officer | Jayasekara G.H.D.T.K. |
| `manager` | Blood Bank Manager | Widanapathirana S.K.N. |
| `itadmin` | IT Operations Supervisor | Wilabada W.K.S.M. |

---

## Notes
- IT_ADMIN has a hard-coded bypass in `requireRole()`, `requirePermission()`, `requirePortalAccess()`, and `hasPermission()` — they always pass
- Audit log writes happen for every significant action across all portals via `authService.log()`
- BCrypt (strength 10) is used for all password hashing — never stored in plaintext
- `spring.jpa.hibernate.ddl-auto=update` means Hibernate auto-creates/updates all tables on startup





































































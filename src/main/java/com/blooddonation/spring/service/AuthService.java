package com.blooddonation.spring.service;

import com.blooddonation.spring.model.*;
import com.blooddonation.spring.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    public static final String SESSION_USER = "currentUser";
    public static final String SESSION_ROLE = "currentRole";

    private static final Map<UserRole, String> DEFAULT_PERMISSIONS = Map.of(
            UserRole.RECIPIENT, "VIEW_AVAILABILITY,SUBMIT_REQUEST,TRACK_REQUEST",
            UserRole.HOSPITAL_COORDINATOR, "VIEW_AVAILABILITY,SUBMIT_REQUISITION,TRACK_DISPATCH,MANAGE_TRANSFER",
            UserRole.QA_OFFICER, "VIEW_INVENTORY,LOG_SCREENING,VERIFY_EXPIRY,LOG_DISCARD",
            UserRole.DELIVERY_OFFICER, "REGISTER_VEHICLE,REGISTER_COURIER,SCHEDULE_DELIVERY,CANCEL_DELIVERY,CONFIRM_DELIVERY",
            UserRole.BLOOD_BANK_MANAGER, "VIEW_STOCK,APPROVE_REQUEST,EMERGENCY_DISPATCH,VIEW_REPORTS,LOW_STOCK_ALERT",
            UserRole.IT_ADMIN, "MANAGE_USERS,MANAGE_PERMISSIONS,VIEW_AUDIT,CONFIGURE_SYSTEM"
    );

            private static final Map<String, Set<String>> PORTAL_PERMISSIONS = Map.of(
                "recipient", Set.of("VIEW_AVAILABILITY", "SUBMIT_REQUEST", "TRACK_REQUEST"),
                "hospital", Set.of("VIEW_AVAILABILITY", "SUBMIT_REQUISITION", "TRACK_DISPATCH", "MANAGE_TRANSFER"),
                "qa", Set.of("VIEW_INVENTORY", "LOG_SCREENING", "VERIFY_EXPIRY", "LOG_DISCARD"),
                "delivery", Set.of("REGISTER_VEHICLE", "REGISTER_COURIER", "SCHEDULE_DELIVERY", "CONFIRM_DELIVERY", "CANCEL_DELIVERY"),
                "manager", Set.of("VIEW_STOCK", "APPROVE_REQUEST", "EMERGENCY_DISPATCH", "LOW_STOCK_ALERT", "VIEW_REPORTS"),
                "it", Set.of("MANAGE_USERS", "MANAGE_PERMISSIONS", "VIEW_AUDIT", "CONFIGURE_SYSTEM")
            );

            private static final Map<String, String> PORTAL_LABELS = Map.of(
                "recipient", "Recipient Services",
                "hospital", "Hospital Coordination",
                "qa", "QA Screening",
                "delivery", "Deliveries",
                "manager", "Stock Dashboard",
                "it", "Admin Console"
            );

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired private UserAccountRepository userRepo;
    @Autowired private PermissionConfigRepository permRepo;
    @Autowired private AuditLogRepository auditRepo;

    @PostConstruct
    public void seedUsersAndPermissions() {
        for (Map.Entry<UserRole, String> entry : DEFAULT_PERMISSIONS.entrySet()) {
            if (permRepo.findById(entry.getKey().name()).isEmpty()) {
                permRepo.save(new PermissionConfig(entry.getKey().name(), entry.getValue()));
            }
        }
        if (userRepo.count() == 0) {
            seedUser("recipient", "Weerasuriya M.W.K.C.P.", UserRole.RECIPIENT, null);
            seedUser("hospital", "Piumal K.P.N.", UserRole.HOSPITAL_COORDINATOR, "National Hospital Colombo");
            seedUser("qa", "Rubasinghe S.S.", UserRole.QA_OFFICER, null);
            seedUser("delivery", "Jayasekara G.H.D.T.K.", UserRole.DELIVERY_OFFICER, null);
            seedUser("manager", "Widanapathirana S.K.N.", UserRole.BLOOD_BANK_MANAGER, null);
            seedUser("itadmin", "Wilabada W.K.S.M.", UserRole.IT_ADMIN, null);
        }
    }

    private void seedUser(String username, String fullName, UserRole role, String hospital) {
        UserAccount u = new UserAccount();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode("pass123"));
        u.setFullName(fullName);
        u.setRole(role);
        u.setHospital(hospital);
        userRepo.save(u);
    }

    public Optional<UserAccount> authenticate(String username, String password) {
        return userRepo.findByUsername(username)
                .filter(UserAccount::isActive)
                .filter(u -> encoder.matches(password, u.getPasswordHash()));
    }

    public Set<String> getRolePermissions(UserRole role) {
        if (role == null) return Collections.emptySet();
        String perms = permRepo.findById(role.name())
                .map(PermissionConfig::getPermissions)
                .orElse(DEFAULT_PERMISSIONS.get(role));
        if (perms == null || perms.isBlank()) return Collections.emptySet();
        return Arrays.stream(perms.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> getUserPermissions(UserAccount user) {
        if (user == null) return Collections.emptySet();
        if (user.getPermissions() != null && !user.getPermissions().isBlank()) {
            return Arrays.stream(user.getPermissions().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return getRolePermissions(user.getRole());
    }

    public boolean hasPermission(UserAccount user, String permission) {
        if (user == null || !user.isActive()) return false;
        if (user.getRole() == UserRole.IT_ADMIN) return true;
        return getUserPermissions(user).contains(permission);
    }

    public boolean hasPermission(UserRole role, String permission) {
        return getRolePermissions(role).contains(permission);
    }

    public boolean hasPortalAccess(UserAccount user, String portal) {
        if (user == null || !user.isActive()) return false;
        if (user.getRole() == UserRole.IT_ADMIN) return true;
        Set<String> portalPermissions = PORTAL_PERMISSIONS.get(portal);
        return portalPermissions != null && getUserPermissions(user).stream().anyMatch(portalPermissions::contains);
    }

    public List<String> getAccessiblePortals(UserAccount user) {
        return PORTAL_LABELS.keySet().stream()
                .filter(portal -> hasPortalAccess(user, portal))
                .map(PORTAL_LABELS::get)
                .collect(Collectors.toList());
    }

    public List<String> getAccessiblePortalKeys(UserAccount user) {
        return PORTAL_LABELS.keySet().stream()
                .filter(portal -> hasPortalAccess(user, portal))
                .collect(Collectors.toList());
    }

    public boolean hasAnyPortalAccess(UserAccount user) {
        return PORTAL_PERMISSIONS.keySet().stream().anyMatch(portal -> hasPortalAccess(user, portal));
    }

    public Map<UserRole, String> getAllPermissions() {
        Map<UserRole, String> map = new LinkedHashMap<>();
        for (UserRole role : UserRole.values()) {
            map.put(role, permRepo.findById(role.name())
                    .map(PermissionConfig::getPermissions)
                    .orElse(DEFAULT_PERMISSIONS.get(role)));
        }
        return map;
    }

    public void updatePermissions(UserRole role, String permissions, String actor) {
        PermissionConfig config = permRepo.findById(role.name())
                .orElse(new PermissionConfig(role.name(), permissions));
        config.setPermissions(permissions != null ? permissions.trim() : "");
        permRepo.save(config);
        log(actor, "UPDATE_ROLE_PERMISSIONS", "Updated default permissions for role " + role.name() + ": " + config.getPermissions());
    }

    public void updateRolePermissions(UserRole role, List<String> permissions, String actor) {
        String joined = (permissions == null || permissions.isEmpty()) ? "" : String.join(",", permissions);
        updatePermissions(role, joined, actor);
    }

    public UserAccount updateUserPermissions(String userId, List<String> permissions, String actor) throws Exception {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new Exception("User not found with ID: " + userId));
        String joined = (permissions == null || permissions.isEmpty()) ? "" : String.join(",", permissions);
        user.setPermissions(joined);
        UserAccount saved = userRepo.save(user);
        log(actor, "UPDATE_MEMBER_PERMISSIONS", "Updated permissions for member @" + user.getUsername() + " (" + user.getFullName() + ") → [" + joined + "]");
        return saved;
    }

    public UserAccount resetUserPermissions(String userId, String actor) throws Exception {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new Exception("User not found with ID: " + userId));
        user.setPermissions(null);
        UserAccount saved = userRepo.save(user);
        log(actor, "RESET_MEMBER_PERMISSIONS", "Reset permissions to role defaults for member @" + user.getUsername());
        return saved;
    }

    public UserAccount toggleUserActive(String userId, String actor) throws Exception {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new Exception("User not found with ID: " + userId));
        user.setActive(!user.isActive());
        UserAccount saved = userRepo.save(user);
        log(actor, "TOGGLE_USER_STATUS", "User @" + user.getUsername() + " status set to: " + (user.isActive() ? "ACTIVE" : "DISABLED"));
        return saved;
    }

    public Optional<UserAccount> getUserById(String id) {
        return userRepo.findById(id);
    }

    public Optional<UserAccount> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public boolean deleteUser(String userId, String actor) throws Exception {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new Exception("User not found with ID: " + userId));
        userRepo.deleteById(userId);
        log(actor, "DELETE_MEMBER", "Deleted member @" + user.getUsername() + " (" + user.getFullName() + ") from the system");
        return true;
    }

    public List<UserAccount> getAllUsers() {
        return userRepo.findAll();
    }

    public UserAccount createUser(String username, String password, String fullName, UserRole role, String hospital) throws Exception {
        return createUser(username, password, fullName, role, hospital, null);
    }

    public UserAccount createUser(String username, String password, String fullName, UserRole role, String hospital, List<String> permissions) throws Exception {
        if (userRepo.existsByUsername(username)) throw new Exception("Username already exists");
        UserAccount u = new UserAccount();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setFullName(fullName);
        u.setRole(role);
        u.setHospital(hospital);
        if (permissions != null && !permissions.isEmpty()) {
            u.setPermissions(String.join(",", permissions));
        }
        return userRepo.save(u);
    }

    public void log(String username, String action, String details) {
        auditRepo.save(new AuditLog(username, action, details));
    }

    public List<AuditLog> getRecentAuditLogs() {
        return auditRepo.findTop50ByOrderByCreatedAtDesc();
    }

    public String getPortalPath(UserRole role) {
        return switch (role) {
            case RECIPIENT -> "/portal/recipient";
            case HOSPITAL_COORDINATOR -> "/portal/hospital";
            case QA_OFFICER -> "/portal/qa";
            case DELIVERY_OFFICER -> "/portal/delivery";
            case BLOOD_BANK_MANAGER -> "/portal/manager";
            case IT_ADMIN -> "/portal/it";
        };
    }

    public String getPortalPath(UserAccount user) {
        if (user == null) return "/login";
        if (user.getRole() == UserRole.IT_ADMIN) return "/portal/it";
        String preferredPortal = switch (user.getRole()) {
            case RECIPIENT -> "recipient";
            case HOSPITAL_COORDINATOR -> "hospital";
            case QA_OFFICER -> "qa";
            case DELIVERY_OFFICER -> "delivery";
            case BLOOD_BANK_MANAGER -> "manager";
            case IT_ADMIN -> "it";
        };
        if (hasPortalAccess(user, preferredPortal)) return "/portal/" + preferredPortal;
        return PORTAL_PERMISSIONS.keySet().stream()
                .filter(portal -> hasPortalAccess(user, portal))
                .findFirst()
                .map(portal -> "/portal/" + portal)
                .orElse("/login?noAccess=true");
    }

    public List<String> getModulesForRole(UserRole role) {
        String perms = permRepo.findById(role.name())
                .map(PermissionConfig::getPermissions)
                .orElse(DEFAULT_PERMISSIONS.get(role));
        return Arrays.stream(perms.split(",")).collect(Collectors.toList());
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

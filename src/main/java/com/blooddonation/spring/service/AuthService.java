package com.blooddonation.spring.service;

import com.blooddonation.spring.model.*;
import com.blooddonation.spring.repository.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public static final String SESSION_USER = "currentUser";
    public static final String SESSION_ROLE = "currentRole";

    private static final Map<UserRole, String> DEFAULT_PERMISSIONS = Map.of(
            UserRole.RECIPIENT, "VIEW_AVAILABILITY,SUBMIT_REQUEST,TRACK_REQUEST",
            UserRole.DONOR, "VIEW_AVAILABILITY,BOOK_APPOINTMENT,UPDATE_PROFILE,TRACK_REQUEST",
            UserRole.HOSPITAL_COORDINATOR, "VIEW_AVAILABILITY,SUBMIT_REQUISITION,TRACK_DISPATCH,MANAGE_TRANSFER",
            UserRole.QA_OFFICER, "VIEW_INVENTORY,LOG_SCREENING,VERIFY_EXPIRY,LOG_DISCARD",
            UserRole.DELIVERY_OFFICER, "REGISTER_VEHICLE,REGISTER_COURIER,SCHEDULE_DELIVERY,CANCEL_DELIVERY,CONFIRM_DELIVERY",
            UserRole.BLOOD_BANK_MANAGER, "VIEW_STOCK,APPROVE_REQUEST,EMERGENCY_DISPATCH,VIEW_REPORTS,LOW_STOCK_ALERT",
            UserRole.IT_ADMIN, "MANAGE_USERS,MANAGE_PERMISSIONS,VIEW_AUDIT,CONFIGURE_SYSTEM"
    );

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired private UserAccountRepository userRepo;
    @Autowired private DonorRepository donorRepo;
    @Autowired private PermissionConfigRepository permRepo;
    @Autowired private AuditLogRepository auditRepo;

    @Autowired
    private javax.sql.DataSource dataSource;

    /** Drop stale CHECK constraints on user_accounts.role (Hibernate never alters them).
     *  Plain JDBC with auto-commit: JPA has no transaction in @PostConstruct. */
    private void dropRoleCheckConstraints() {
        try (java.sql.Connection c = dataSource.getConnection();
             java.sql.Statement s = c.createStatement();
             java.sql.ResultSet rs = s.executeQuery(
                     "SELECT cc.name FROM sys.check_constraints cc " +
                     "JOIN sys.columns c ON c.object_id = cc.parent_object_id AND c.column_id = cc.parent_column_id " +
                     "JOIN sys.tables t ON t.object_id = cc.parent_object_id " +
                     "WHERE t.name = 'user_accounts' AND c.name = 'role'")) {
            java.util.List<String> names = new java.util.ArrayList<>();
            while (rs.next()) names.add(rs.getString(1));
            for (String name : names) {
                if (name != null && name.matches("[A-Za-z0-9_]+")) {
                    try (java.sql.Statement d = c.createStatement()) {
                        d.execute("ALTER TABLE user_accounts DROP CONSTRAINT [" + name + "]");
                        log.info("Dropped stale role CHECK constraint: {}", name);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not drop role CHECK constraints (non-SQLServer DB?): {}", e.getMessage());
        }
    }

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @PostConstruct
    public void seedUsersAndPermissions() {
        for (Map.Entry<UserRole, String> entry : DEFAULT_PERMISSIONS.entrySet()) {
            if (permRepo.findById(entry.getKey().name()).isEmpty()) {
                permRepo.save(new PermissionConfig(entry.getKey().name(), entry.getValue()));
            }
        }
        // One-time migration: donor-linked accounts created as RECIPIENT → real DONOR role.
        // The DB CHECK constraint on user_accounts.role only allows old enum values,
        // and ddl-auto=update never alters it — so drop stale constraints first.
        dropRoleCheckConstraints();
        for (UserAccount u : userRepo.findAll()) {
            if (u.getDonorId() != null && !u.getDonorId().isBlank() && u.getRole() == UserRole.RECIPIENT) {
                try {
                    u.setRole(UserRole.DONOR);
                    userRepo.save(u);
                    log.info("Migrated donor login to DONOR role: {}", u.getUsername());
                } catch (Exception e) {
                    log.error("Donor role migration failed for {}: {}", u.getUsername(), e.getMessage());
                }
            }
        }
        // Real-world: never seed weak demo passwords in production.
        // Set APP_SEED=false env var on real deploys and create users via IT portal.
        if (!seedEnabled) {
            log.info("Demo user seeding disabled (app.seed.enabled=false)");
        } else {
            if (userRepo.count() == 0) {
                seedUser("recipient", "Weerasuriya M.W.K.C.P.", UserRole.RECIPIENT, null);
                seedUser("hospital", "Piumal K.P.N.", UserRole.HOSPITAL_COORDINATOR, "National Hospital Colombo");
                seedUser("qa", "Rubasinghe S.S.", UserRole.QA_OFFICER, null);
                seedUser("delivery", "Jayasekara G.H.D.T.K.", UserRole.DELIVERY_OFFICER, null);
                seedUser("manager", "Widanapathirana S.K.N.", UserRole.BLOOD_BANK_MANAGER, null);
                seedUser("itadmin", "Wilabada W.K.S.M.", UserRole.IT_ADMIN, null);
            }
        }
        // Every donor must have a login (username = name, password = phone).
        // Backfills seed/IT-created profiles that never self-registered. Idempotent.
        backfillDonorLogins();
    }

    private void backfillDonorLogins() {
        for (Donor donor : donorRepo.findAll()) {
            if (donor.getId() == null || userRepo.findByDonorId(donor.getId()).isPresent()) continue;
            try {
                String username = createDonorAccount(donor);
                log.info("Backfilled donor login: {} (donor {})", username, donor.getId());
            } catch (Exception e) {
                log.warn("Could not backfill login for donor {}: {}", donor.getId(), e.getMessage());
            }
        }
    }

    private void seedUser(String username, String fullName, UserRole role, String hospital) {
        seedUserWithPassword(username, fullName, role, hospital, "pass123");
    }

    private void seedUserWithPassword(String username, String fullName, UserRole role, String hospital, String password) {
        UserAccount u = new UserAccount();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setFullName(fullName);
        u.setRole(role);
        u.setHospital(hospital);
        userRepo.save(u);
    }

    public Optional<UserAccount> authenticate(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        String clean = username.trim().toLowerCase();
        Optional<UserAccount> result = userRepo.findByUsername(clean)
                .filter(UserAccount::isActive)
                .filter(u -> encoder.matches(password, u.getPasswordHash()));
        // Backwards-compat: older seeds stored mixed-case usernames
        if (result.isEmpty()) {
            result = userRepo.findByUsername(username.trim())
                    .filter(UserAccount::isActive)
                    .filter(u -> encoder.matches(password, u.getPasswordHash()));
        }
        if (result.isEmpty()) {
            log.warn("Failed login attempt for username '{}'", username);
        }
        return result;
    }

    public boolean hasPermission(UserRole role, String permission) {
        return permRepo.findById(role.name())
                .map(p -> Arrays.asList(p.getPermissions().split(",")).contains(permission))
                .orElse(false);
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

    /** Distinct union of every known permission scope (defaults + DB), sorted — drives tick-box UI. */
    public List<String> getAllPermissionScopes() {
        java.util.Set<String> scopes = new java.util.TreeSet<>();
        DEFAULT_PERMISSIONS.values().forEach(csv -> scopes.addAll(Arrays.asList(csv.split(","))));
        permRepo.findAll().forEach(p -> {
            if (p.getPermissions() != null) scopes.addAll(Arrays.asList(p.getPermissions().split(",")));
        });
        scopes.removeIf(s -> s == null || s.isBlank());
        return new java.util.ArrayList<>(scopes);
    }

    public void updatePermissions(UserRole role, String permissions, String actor) {
        PermissionConfig config = permRepo.findById(role.name())
                .orElse(new PermissionConfig(role.name(), permissions));
        config.setPermissions(permissions);
        permRepo.save(config);
        log(actor, "UPDATE_PERMISSIONS", "Updated permissions for " + role.name() + ": " + permissions);
    }

    public List<UserAccount> getAllUsers() {
        return userRepo.findAll();
    }

    public java.util.Optional<UserAccount> findByDonorId(String donorId) {
        return userRepo.findByDonorId(donorId);
    }

    public UserAccount createUser(String username, String password, String fullName, UserRole role, String hospital) throws Exception {
        if (username == null || username.trim().length() < 3) throw new Exception("Username must be at least 3 characters");
        String clean = username.trim().toLowerCase();
        if (userRepo.existsByUsername(clean) || userRepo.existsByUsername(username.trim())) throw new Exception("Username already exists");
        validatePassword(password);
        if (fullName == null || fullName.trim().length() < 2) throw new Exception("Full name is required");
        if (role == null) throw new Exception("Role is required");
        UserAccount u = new UserAccount();
        u.setUsername(clean);
        u.setPasswordHash(encoder.encode(password));
        u.setFullName(fullName.trim());
        u.setRole(role);
        u.setHospital(hospital);
        UserAccount saved = userRepo.save(u);
        log.info("User created: {} with role {}", clean, role);
        return saved;
    }

    public void validatePassword(String password) throws Exception {
        if (password == null || password.length() < 8) {
            throw new Exception("Password must be at least 8 characters (real-world policy)");
        }
    }

    /**
     * Keep donor login in sync when they edit their own name/phone:
     * username follows name, password follows phone number.
     */
    public void updateDonorCredentials(String donorId, String newName, String newPhone) throws Exception {
        UserAccount acc = userRepo.findByDonorId(donorId).orElse(null);
        if (acc == null) return;
        String cleanName = newName == null ? "" : newName.trim().toLowerCase();
        if (!cleanName.isEmpty() && !cleanName.equals(acc.getUsername())) {
            if (cleanName.length() < 3) throw new Exception("Name is too short to use as a login username.");
            if (userRepo.existsByUsername(cleanName)) {
                throw new Exception("This name is already taken as a login username. Please use a different name.");
            }
            acc.setUsername(cleanName);
        }
        String digits = newPhone == null ? "" : newPhone.replaceAll("\\D", "");
        if (!digits.isEmpty() && !encoder.matches(digits, acc.getPasswordHash())) {
            if (digits.length() < 9) throw new Exception("Phone number is too short.");
            acc.setPasswordHash(encoder.encode(digits));
        }
        if (newName != null && !newName.isBlank()) acc.setFullName(newName.trim());
        userRepo.save(acc);
    }

    /**
     * Self-registered donors get an automatic login account:
     * username = registered name (lowercase), password = registered phone number.
     * Returns the final username (suffixed on collision) so the UI can show it.
     */
    public String createDonorAccount(Donor donor) throws Exception {
        if (donor.getId() != null && userRepo.findByDonorId(donor.getId()).isPresent()) {
            return userRepo.findByDonorId(donor.getId()).get().getUsername();
        }
        String base = donor.getName() == null ? "" : donor.getName().trim().toLowerCase();
        String phone = donor.getPhone() == null ? "" : donor.getPhone().replaceAll("\\D", "");
        if (phone.length() < 9) throw new Exception("Valid phone number is required for login account creation");
        if (base.length() < 3) base = (base + "-" + phone.substring(Math.max(0, phone.length() - 4))).trim();
        if (base.length() > 40) base = base.substring(0, 40);
        String username = base;
        if (userRepo.existsByUsername(username)) {
            username = base + "-" + phone.substring(Math.max(0, phone.length() - 4));
        }
        if (userRepo.existsByUsername(username)) {
            username = base + "-" + phone.substring(Math.max(0, phone.length() - 4))
                    + (100 + new java.util.Random().nextInt(900));
        }
        if (username.length() > 50) username = username.substring(0, 50);
        if (userRepo.existsByUsername(username)) throw new Exception("Could not generate a unique login username");
        UserAccount u = new UserAccount();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(phone));
        u.setFullName(donor.getName().trim());
        u.setRole(UserRole.DONOR);
        u.setDonorId(donor.getId());
        userRepo.save(u);
        log.info("Donor login account created: {} (donor {})", username, donor.getId());
        return username;
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
            case DONOR -> "/portal/donor";
            case HOSPITAL_COORDINATOR -> "/portal/hospital";
            case QA_OFFICER -> "/portal/qa";
            case DELIVERY_OFFICER -> "/portal/delivery";
            case BLOOD_BANK_MANAGER -> "/portal/manager";
            case IT_ADMIN -> "/portal/it";
        };
    }

    /** Self-registered donors land on their own profile only — nothing else. */
    public String getHomePath(UserAccount user) {
        if (user != null && user.getDonorId() != null && !user.getDonorId().isBlank()) {
            return "/portal/donor";
        }
        return getPortalPath(user.getRole());
    }

    public List<String> getModulesForRole(UserRole role) {
        String perms = permRepo.findById(role.name())
                .map(PermissionConfig::getPermissions)
                .orElse(DEFAULT_PERMISSIONS.get(role));
        return Arrays.stream(perms.split(",")).collect(Collectors.toList());
    }

    public UserAccount toggleUserStatus(String id) throws Exception {
        UserAccount user = userRepo.findById(id).orElseThrow(() -> new Exception("User not found"));
        user.setActive(!user.isActive());
        return userRepo.save(user);
    }

    public UserAccount updateUser(String id, String fullName, UserRole role, String hospital, String newPassword) throws Exception {
        UserAccount user = userRepo.findById(id).orElseThrow(() -> new Exception("User not found"));
        if (fullName != null && !fullName.isBlank()) user.setFullName(fullName.trim());
        if (role != null) user.setRole(role);
        user.setHospital(hospital);
        if (newPassword != null && !newPassword.isBlank()) {
            validatePassword(newPassword);
            user.setPasswordHash(encoder.encode(newPassword));
        }
        return userRepo.save(user);
    }

    public void deleteUser(String id, String currentUsername) throws Exception {
        UserAccount user = userRepo.findById(id).orElseThrow(() -> new Exception("User not found"));
        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new Exception("You cannot delete your own currently logged-in account!");
        }
        // Donor-linked logins: remove the donor profile too, otherwise the
        // deleted donor keeps appearing in Find Donors (/search).
        if (user.getDonorId() != null && !user.getDonorId().isBlank()) {
            donorRepo.findById(user.getDonorId()).ifPresent(donorRepo::delete);
        }
        userRepo.delete(user);
    }
}

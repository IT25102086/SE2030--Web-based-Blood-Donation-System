package com.blooddonation.spring.service;

import com.blooddonation.spring.model.PermissionConfig;
import com.blooddonation.spring.model.UserAccount;
import com.blooddonation.spring.model.UserRole;
import com.blooddonation.spring.repository.PermissionConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final AuthService authService = new AuthService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "permRepo", repositoryReturning(Optional.empty()));
    }

    @Test
    void rolePermissionsUseStoredConfigurationWhenPresent() {
        PermissionConfigRepository permissionRepository = repositoryReturning(
            Optional.of(new PermissionConfig(UserRole.QA_OFFICER.name(), "VIEW_INVENTORY, LOG_SCREENING")));
        ReflectionTestUtils.setField(authService, "permRepo", permissionRepository);

        assertEquals(Set.of("VIEW_INVENTORY", "LOG_SCREENING"),
                authService.getRolePermissions(UserRole.QA_OFFICER));
    }

    @Test
    void inactiveUserHasNoPermission() {
        UserAccount user = user(UserRole.RECIPIENT, "VIEW_AVAILABILITY");
        user.setActive(false);

        assertFalse(authService.hasPermission(user, "VIEW_AVAILABILITY"));
    }

    @Test
    void customizedMemberCanAccessOnlyAssignedPortal() {
        UserAccount user = user(UserRole.RECIPIENT, "VIEW_INVENTORY");

        assertTrue(authService.hasPortalAccess(user, "qa"));
        assertFalse(authService.hasPortalAccess(user, "recipient"));
        assertEquals("/portal/qa", authService.getPortalPath(user));
    }

    @Test
    void memberWithNoPortalPermissionIsSentToNoAccessLogin() {
        UserAccount user = user(UserRole.RECIPIENT, "UNKNOWN_PERMISSION");

        assertFalse(authService.hasAnyPortalAccess(user));
        assertEquals("/login?noAccess=true", authService.getPortalPath(user));
    }

    @Test
    void itAdminCanAccessAllPortals() {
        UserAccount user = user(UserRole.IT_ADMIN, null);

        assertTrue(authService.hasPortalAccess(user, "recipient"));
        assertTrue(authService.hasPortalAccess(user, "it"));
        assertEquals("/portal/it", authService.getPortalPath(user));
    }

    @Test
    void inactiveUserCannotLoginEvenWithCorrectPassword() {
        UserAccount storedUser = user(UserRole.RECIPIENT, "VIEW_AVAILABILITY");
        storedUser.setUsername("recipient");
        storedUser.setPasswordHash("$2a$10$z7L5E7T3n2P2n1W6Y0YLOO7/gH9s0AJrJvR7Z6m5Lr2mTQxR2KX8e");
        storedUser.setActive(false);

        Map<String, UserAccount> users = new HashMap<>();
        users.put("recipient", storedUser);

        ReflectionTestUtils.setField(authService, "userRepo", repositoryWithUsers(users));
        ReflectionTestUtils.setField(authService, "auditRepo", repositoryForAudit());

        assertTrue(authService.authenticate("recipient", "pass123").isEmpty());
    }

    @Test
    void deletingMemberRemovesThemFromRepository() throws Exception {
        UserAccount storedUser = user(UserRole.HOSPITAL_COORDINATOR, "VIEW_AVAILABILITY");
        storedUser.setId("member-1");
        storedUser.setUsername("hospital");
        storedUser.setPasswordHash("hash");

        Map<String, UserAccount> users = new HashMap<>();
        users.put("member-1", storedUser);

        ReflectionTestUtils.setField(authService, "userRepo", repositoryWithUsers(users));
        ReflectionTestUtils.setField(authService, "auditRepo", repositoryForAudit());

        authService.deleteUser("member-1", "itadmin");

        assertFalse(users.containsKey("member-1"));
    }

    @Test
    void customMemberSeesOnlyAssignedPortalsInNavigation() {
        UserAccount user = user(UserRole.RECIPIENT, "VIEW_INVENTORY,LOG_SCREENING");

        assertFalse(authService.hasPortalAccess(user, "recipient"));
        assertTrue(authService.hasPortalAccess(user, "qa"));
        assertEquals(List.of("qa"), authService.getAccessiblePortalKeys(user));
    }

    private UserAccount user(UserRole role, String permissions) {
        UserAccount user = new UserAccount();
        user.setRole(role);
        user.setUsername("test-user");
        user.setFullName("Test User");
        user.setPermissions(permissions);
        return user;
    }

    private PermissionConfigRepository repositoryReturning(Optional<PermissionConfig> config) {
        return (PermissionConfigRepository) Proxy.newProxyInstance(
                PermissionConfigRepository.class.getClassLoader(),
                new Class<?>[]{PermissionConfigRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) return config;
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class || method.getReturnType() == long.class) return 0;
                    return null;
                });
    }

    private com.blooddonation.spring.repository.UserAccountRepository repositoryWithUsers(Map<String, UserAccount> users) {
        return (com.blooddonation.spring.repository.UserAccountRepository) Proxy.newProxyInstance(
                com.blooddonation.spring.repository.UserAccountRepository.class.getClassLoader(),
                new Class<?>[]{com.blooddonation.spring.repository.UserAccountRepository.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findByUsername":
                            String username = (String) args[0];
                            return Optional.ofNullable(users.values().stream()
                                    .filter(u -> username.equals(u.getUsername()))
                                    .findFirst()
                                    .orElse(null));
                        case "findById":
                            String id = (String) args[0];
                            return Optional.ofNullable(users.get(id));
                        case "existsByUsername":
                            String usernameToCheck = (String) args[0];
                            return users.values().stream().anyMatch(u -> usernameToCheck.equals(u.getUsername()));
                        case "findAll":
                            return List.copyOf(users.values());
                        case "save":
                            UserAccount value = (UserAccount) args[0];
                            if (value.getId() == null) {
                                value.setId("generated-" + users.size());
                            }
                            users.put(value.getId(), value);
                            return value;
                        case "deleteById":
                            users.remove(args[0]);
                            return null;
                        case "count":
                            return (long) users.size();
                        default:
                            if (method.getReturnType() == boolean.class) return false;
                            if (method.getReturnType() == int.class || method.getReturnType() == long.class) return 0;
                            return null;
                    }
                });
    }

    private com.blooddonation.spring.repository.AuditLogRepository repositoryForAudit() {
        return (com.blooddonation.spring.repository.AuditLogRepository) Proxy.newProxyInstance(
                com.blooddonation.spring.repository.AuditLogRepository.class.getClassLoader(),
                new Class<?>[]{com.blooddonation.spring.repository.AuditLogRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) return args[0];
                    if ("findTop50ByOrderByCreatedAtDesc".equals(method.getName())) return List.of();
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class || method.getReturnType() == long.class) return 0;
                    return null;
                });
    }
}

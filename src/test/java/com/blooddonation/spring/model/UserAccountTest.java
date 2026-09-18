package com.blooddonation.spring.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserAccountTest {

    @Test
    void accountStoresIdentityAndAccessAttributes() {
        UserAccount user = new UserAccount();
        user.setId("member-1");
        user.setUsername("member");
        user.setFullName("Member One");
        user.setRole(UserRole.QA_OFFICER);
        user.setHospital("Central Hospital");
        user.setPermissions("VIEW_INVENTORY,LOG_SCREENING");
        user.setActive(false);

        assertEquals("member-1", user.getId());
        assertEquals("member", user.getUsername());
        assertEquals("Member One", user.getFullName());
        assertEquals(UserRole.QA_OFFICER, user.getRole());
        assertEquals("Central Hospital", user.getHospital());
        assertEquals("VIEW_INVENTORY,LOG_SCREENING", user.getPermissions());
        assertFalse(user.isActive());
    }

    @Test
    void createdAtIsAssignedWhenEntityIsPersisted() throws Exception {
        UserAccount user = new UserAccount();
        Method onCreate = UserAccount.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);

        onCreate.invoke(user);

        assertNotNull(user.getCreatedAt());
        assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}

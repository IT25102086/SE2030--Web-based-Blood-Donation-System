package com.blooddonation.spring.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AppPermissionTest {

    @Test
    void permissionLookupIsCaseInsensitiveAndTrimsInput() {
        assertEquals(AppPermission.VIEW_STOCK,
                AppPermission.fromCode("  view_stock ").orElseThrow());
        assertTrue(AppPermission.fromCode("unknown").isEmpty());
        assertTrue(AppPermission.fromCode(null).isEmpty());
    }

    @Test
    void permissionsAreGroupedByFunctionalCategory() {
        Map<String, List<AppPermission>> grouped = AppPermission.getGroupedByCategory();

        assertTrue(grouped.containsKey("IT Administration"));
        assertTrue(grouped.get("IT Administration").contains(AppPermission.MANAGE_USERS));
        assertTrue(grouped.get("Recipient & Patient Services").contains(AppPermission.SUBMIT_REQUEST));
    }
}

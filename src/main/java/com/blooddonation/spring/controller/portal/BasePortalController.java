package com.blooddonation.spring.controller.portal;

import com.blooddonation.spring.model.UserAccount;
import com.blooddonation.spring.model.UserRole;
import com.blooddonation.spring.service.AuthService;
import com.blooddonation.spring.service.BloodDonationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Shared guards for all portal controllers — SECURITY-CRITICAL shared code.
 *
 * <p>Every role controller in this package extends this base class. Coordinate
 * before modifying: a change here affects all six portals.</p>
 */
public abstract class BasePortalController {

    @Autowired protected BloodDonationService service;
    @Autowired protected AuthService authService;

    protected UserAccount requireRole(HttpSession session, UserRole... roles) {
        UserAccount user = (UserAccount) session.getAttribute(AuthService.SESSION_USER);
        if (user == null) throw new RuntimeException("Not authenticated");
        if (user.getRole() == UserRole.IT_ADMIN) return user;
        for (UserRole r : roles) if (user.getRole() == r) return user;
        throw new RuntimeException("Access denied");
    }

    /** Self-registered donors may ONLY use their own profile page — nothing else. */
    protected UserAccount requireDonor(HttpSession session) {
        UserAccount user = (UserAccount) session.getAttribute(AuthService.SESSION_USER);
        if (user == null) throw new RuntimeException("Not authenticated");
        if (user.getDonorId() == null || user.getDonorId().isBlank()) throw new RuntimeException("Access denied");
        return user;
    }

    protected boolean isDonor(UserAccount user) {
        return user != null && user.getDonorId() != null && !user.getDonorId().isBlank();
    }
}

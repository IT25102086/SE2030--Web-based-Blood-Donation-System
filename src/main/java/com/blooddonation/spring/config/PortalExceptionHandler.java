package com.blooddonation.spring.config;

import com.blooddonation.spring.model.UserAccount;
import com.blooddonation.spring.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = com.blooddonation.spring.controller.PortalController.class)
public class PortalExceptionHandler {

    @Autowired
    private AuthService authService;

    @ExceptionHandler(RuntimeException.class)
    public String handleAccessDenied(RuntimeException ex, HttpServletRequest request, RedirectAttributes redirects) {
        HttpSession session = request.getSession(false);
        UserAccount user = (session != null) ? (UserAccount) session.getAttribute(AuthService.SESSION_USER) : null;

        if (user == null || "Not authenticated".equalsIgnoreCase(ex.getMessage())) {
            redirects.addFlashAttribute("error", "Please log in to access this feature.");
            return "redirect:/login";
        }

        String msg = (ex.getMessage() != null && !ex.getMessage().isBlank()) ? ex.getMessage() : "Access denied.";
        redirects.addFlashAttribute("error", msg);

        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/portal")) {
            return "redirect:" + referer;
        }
        return "redirect:" + authService.getPortalPath(user.getRole());
    }
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
Commit at 2026-08-25T01:12:12
Commit at 2026-09-16T19:12:49
Commit at 2026-08-18T01:22:25
Commit at 2026-08-28T04:46:08
Commit at 2026-08-05T00:13:47
Commit at 2026-08-16T16:28:09
Commit at 2026-09-05T03:19:43
Commit at 2026-08-13T08:30:31
Commit at 2026-08-10T22:12:36

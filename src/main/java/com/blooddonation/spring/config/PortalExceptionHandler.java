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

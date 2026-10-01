package com.blooddonation.spring.controller;

import com.blooddonation.spring.model.UserAccount;
import com.blooddonation.spring.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginForm(HttpSession session, Model model,
                            @RequestParam(required = false) boolean noAccess) {
        if (session.getAttribute(AuthService.SESSION_USER) != null) {
            UserAccount user = (UserAccount) session.getAttribute(AuthService.SESSION_USER);
            if (authService.hasAnyPortalAccess(user)) return "redirect:" + authService.getPortalPath(user);
            model.addAttribute("error", "No site access is assigned to this account. Contact your IT Administrator.");
            return "login";
        }
        if (noAccess) model.addAttribute("error", "No site access is assigned to this account. Contact your IT Administrator.");
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String username, @RequestParam String password,
                              HttpSession session, RedirectAttributes redirects) {
        var existingUser = authService.getUserByUsername(username);
        if (existingUser.isPresent() && !existingUser.get().isActive()) {
            redirects.addFlashAttribute("error", "This account has been disabled. Please contact the IT administrator.");
            return "redirect:/login";
        }

        var userOpt = authService.authenticate(username, password);
        if (userOpt.isEmpty()) {
            redirects.addFlashAttribute("error", "Invalid username or password.");
            return "redirect:/login";
        }
        UserAccount user = userOpt.get();
        session.setAttribute(AuthService.SESSION_USER, user);
        session.setAttribute(AuthService.SESSION_ROLE, user.getRole());
        authService.log(user.getUsername(), "LOGIN", "User logged in as " + user.getRole().getDisplayName());
        redirects.addFlashAttribute("success", "Welcome, " + user.getFullName() + "!");
        return "redirect:" + authService.getPortalPath(user);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirects) {
        UserAccount user = (UserAccount) session.getAttribute(AuthService.SESSION_USER);
        if (user != null) authService.log(user.getUsername(), "LOGOUT", "User logged out");
        session.invalidate();
        redirects.addFlashAttribute("success", "Logged out successfully.");
        return "redirect:/login";
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
Commit at 2026-10-03T10:07:02
Commit at 2026-10-02T06:07:37
Commit at 2026-10-01T23:55:20

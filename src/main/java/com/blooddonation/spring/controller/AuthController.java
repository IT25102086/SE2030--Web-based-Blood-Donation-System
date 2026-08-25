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
Commit at 2026-09-07T14:07:50
Commit at 2026-08-24T13:39:55
Commit at 2026-08-17T16:23:04
Commit at 2026-08-20T03:23:53
Commit at 2026-08-27T06:06:59
Commit at 2026-08-22T07:51:52
Commit at 2026-08-19T10:19:15
Commit at 2026-08-10T20:17:15
Commit at 2026-09-07T09:12:22
Commit at 2026-08-13T20:40:16
Commit at 2026-08-11T16:52:28
Commit at 2026-09-07T11:08:19
Commit at 2026-09-05T05:56:42
Commit at 2026-08-13T17:04:19
Commit at 2026-08-20T16:25:37
Commit at 2026-08-26T20:00:39
Commit at 2026-08-26T01:55:16

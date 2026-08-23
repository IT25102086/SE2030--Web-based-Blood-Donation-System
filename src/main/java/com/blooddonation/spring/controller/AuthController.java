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

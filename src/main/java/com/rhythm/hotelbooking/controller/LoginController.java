package com.rhythm.hotelbooking.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/")
    public String home() {
        return "redirect:/browse";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {

        if (username.equals("admin") && password.equals("admin123")) {
            session.setAttribute("role", "ADMIN");
            return "redirect:/admin/dashboard";
        }

        return "redirect:/login?error";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        return "admin-dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/browse";
    }
}
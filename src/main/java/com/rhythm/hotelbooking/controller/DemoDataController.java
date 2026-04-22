package com.rhythm.hotelbooking.controller;

import com.rhythm.hotelbooking.service.DemoDataService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DemoDataController {

    private final DemoDataService demoDataService;

    public DemoDataController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    @PostMapping("/admin/reset-demo")
    public String resetDemoData(HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        demoDataService.resetAndSeedDemoData();

        return "redirect:/admin/dashboard";
    }
}
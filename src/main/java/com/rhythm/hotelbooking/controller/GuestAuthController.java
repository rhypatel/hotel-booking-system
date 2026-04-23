package com.rhythm.hotelbooking.controller;

import com.rhythm.hotelbooking.model.GuestUser;
import com.rhythm.hotelbooking.repository.BookingRepository;
import com.rhythm.hotelbooking.repository.GuestUserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GuestAuthController {

    private final GuestUserRepository guestUserRepository;
    private final BookingRepository bookingRepository;

    public GuestAuthController(GuestUserRepository guestUserRepository,
                               BookingRepository bookingRepository) {
        this.guestUserRepository = guestUserRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/guest-signup")
    public String showGuestSignupPage(Model model) {
        model.addAttribute("guestUser", new GuestUser());
        return "guest-signup";
    }

    @PostMapping("/guest-signup")
    public String processGuestSignup(@ModelAttribute GuestUser guestUser, Model model) {
        if (guestUserRepository.findByEmail(guestUser.getEmail()) != null) {
            model.addAttribute("guestUser", guestUser);
            model.addAttribute("error", "An account with this email already exists.");
            return "guest-signup";
        }

        guestUserRepository.save(guestUser);
        return "redirect:/guest-login?signupSuccess";
    }

    @GetMapping("/guest-login")
    public String showGuestLoginPage() {
        return "guest-login";
    }

    @PostMapping("/guest-login")
    public String processGuestLogin(@RequestParam String email,
                                    @RequestParam String password,
                                    HttpSession session) {

        GuestUser guestUser = guestUserRepository.findByEmail(email);

        if (guestUser != null && guestUser.getPassword().equals(password)) {
            session.setAttribute("guestUserEmail", guestUser.getEmail());
            session.setAttribute("guestUserName", guestUser.getFullName());
            return "redirect:/guest-dashboard";
        }

        return "redirect:/guest-login?error";
    }

    @GetMapping("/guest-dashboard")
    public String showGuestDashboard(HttpSession session, Model model) {
        Object guestEmail = session.getAttribute("guestUserEmail");

        if (guestEmail == null) {
            return "redirect:/guest-login";
        }

        GuestUser guestUser = guestUserRepository.findByEmail(guestEmail.toString());
        var bookings = bookingRepository.findByEmailOrderByIdDesc(guestEmail.toString());

        String loyaltyTier;
        int points = guestUser.getLoyaltyPoints();

        if (points >= 1000) {
            loyaltyTier = "Platinum";
        } else if (points >= 500) {
            loyaltyTier = "Gold";
        } else if (points >= 100) {
            loyaltyTier = "Silver";
        } else {
            loyaltyTier = "Bronze";
        }

        model.addAttribute("guestUser", guestUser);
        model.addAttribute("bookings", bookings);
        model.addAttribute("loyaltyTier", loyaltyTier);

        return "guest-dashboard";
    }

    @GetMapping("/guest-logout")
    public String guestLogout(HttpSession session) {
        session.removeAttribute("guestUserEmail");
        session.removeAttribute("guestUserName");
        return "redirect:/browse";
    }
}
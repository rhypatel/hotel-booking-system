package com.rhythm.hotelbooking.controller;

import com.rhythm.hotelbooking.repository.BookingRepository;
import com.rhythm.hotelbooking.repository.RoomRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public AnalyticsController(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/analytics")
    public String showAnalytics(Model model, HttpSession session) {

        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        long totalRooms = roomRepository.count();
        long availableRooms = roomRepository.findAll().stream()
                .filter(room -> "AVAILABLE".equals(room.getStatus()))
                .count();
        long occupiedRooms = roomRepository.findAll().stream()
                .filter(room -> "OCCUPIED".equals(room.getStatus()))
                .count();
        long cleaningRooms = roomRepository.findAll().stream()
                .filter(room -> "CLEANING".equals(room.getStatus()))
                .count();

        long pendingBookings = bookingRepository.findAll().stream()
                .filter(booking -> "PENDING".equals(booking.getStatus()))
                .count();
        long confirmedBookings = bookingRepository.findAll().stream()
                .filter(booking -> "CONFIRMED".equals(booking.getStatus()))
                .count();
        long completedBookings = bookingRepository.findAll().stream()
                .filter(booking -> "COMPLETED".equals(booking.getStatus()))
                .count();
        long rejectedBookings = bookingRepository.findAll().stream()
                .filter(booking -> "REJECTED".equals(booking.getStatus()))
                .count();

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("occupiedRooms", occupiedRooms);
        model.addAttribute("cleaningRooms", cleaningRooms);

        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("rejectedBookings", rejectedBookings);

        return "analytics";
    }
}
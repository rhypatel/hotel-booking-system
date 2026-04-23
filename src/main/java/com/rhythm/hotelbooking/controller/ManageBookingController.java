package com.rhythm.hotelbooking.controller;

import com.rhythm.hotelbooking.model.Booking;
import com.rhythm.hotelbooking.model.GuestUser;
import com.rhythm.hotelbooking.model.Room;
import com.rhythm.hotelbooking.repository.BookingRepository;
import com.rhythm.hotelbooking.repository.GuestUserRepository;
import com.rhythm.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ManageBookingController {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestUserRepository guestUserRepository;

    public ManageBookingController(BookingRepository bookingRepository,
                                   RoomRepository roomRepository,
                                   GuestUserRepository guestUserRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.guestUserRepository = guestUserRepository;
    }

    @GetMapping("/manage-booking")
    public String showManageBookingPage() {
        return "manage-booking";
    }

    @PostMapping("/manage-booking")
    public String findBooking(@RequestParam Long bookingId, Model model) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);

        if (booking == null) {
            model.addAttribute("error", "No reservation found for that booking ID.");
            return "manage-booking";
        }

        model.addAttribute("booking", booking);
        return "manage-booking-result";
    }

    @PostMapping("/manage-booking/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            model.addAttribute("error", "No reservation found.");
            return "manage-booking";
        }

        if ("PENDING".equalsIgnoreCase(booking.getStatus()) || "CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
            model.addAttribute("booking", booking);
            model.addAttribute("success", "Your reservation has been cancelled successfully.");
            return "manage-booking-result";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("error", "This reservation can no longer be cancelled.");
        return "manage-booking-result";
    }

    @PostMapping("/manage-booking/check-in/{id}")
    public String checkInBooking(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            model.addAttribute("error", "No reservation found.");
            return "manage-booking";
        }

        if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            model.addAttribute("booking", booking);
            model.addAttribute("error", "This reservation is not ready for check-in.");
            return "manage-booking-result";
        }

        Room room = roomRepository.findAll()
                .stream()
                .filter(r -> r.getRoomNumber().equals(booking.getRoomNumber()))
                .findFirst()
                .orElse(null);

        if (room != null) {
            room.setStatus("OCCUPIED");
            roomRepository.save(room);
        }

        booking.setStatus("CHECKED_IN");
        bookingRepository.save(booking);

        model.addAttribute("booking", booking);
        model.addAttribute("success", "You have checked in successfully.");
        return "manage-booking-result";
    }

    @PostMapping("/manage-booking/check-out/{id}")
    public String checkOutBooking(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            model.addAttribute("error", "No reservation found.");
            return "manage-booking";
        }

        if (!"CHECKED_IN".equalsIgnoreCase(booking.getStatus())) {
            model.addAttribute("booking", booking);
            model.addAttribute("error", "This reservation is not currently checked in.");
            return "manage-booking-result";
        }

        Room room = roomRepository.findAll()
                .stream()
                .filter(r -> r.getRoomNumber().equals(booking.getRoomNumber()))
                .findFirst()
                .orElse(null);

        if (room != null) {
            room.setStatus("CLEANING");
            roomRepository.save(room);
        }

        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);

        if (booking.getEmail() != null && !booking.getEmail().isBlank()) {
            GuestUser guestUser = guestUserRepository.findByEmail(booking.getEmail());

            if (guestUser != null) {
                guestUser.setLoyaltyPoints(guestUser.getLoyaltyPoints() + 100);
                guestUserRepository.save(guestUser);
            }
        }

        model.addAttribute("booking", booking);
        model.addAttribute("success", "You have checked out successfully. Loyalty points have been added to your account.");
        return "manage-booking-result";
    }
}
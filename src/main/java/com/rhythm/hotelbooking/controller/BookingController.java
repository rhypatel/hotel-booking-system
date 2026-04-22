package com.rhythm.hotelbooking.controller;

import com.rhythm.hotelbooking.model.Booking;
import com.rhythm.hotelbooking.repository.BookingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.rhythm.hotelbooking.model.Room;
import com.rhythm.hotelbooking.repository.RoomRepository;

import java.util.List;

@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public BookingController(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/book-room")
    public String showBookingForm(@RequestParam(required = false) String selectedRoom,
                                  Model model) {

        Booking booking = new Booking();

        if (selectedRoom != null) {
            booking.setRoomNumber(selectedRoom);
        }

        var availableRooms = roomRepository.findAllByOrderByFloorAscRoomNumberAsc()
                .stream()
                .filter(room -> "AVAILABLE".equals(room.getStatus()))
                .toList();

        model.addAttribute("booking", booking);
        model.addAttribute("rooms", availableRooms);

        return "book-room";
    }

    @PostMapping("/book-room")
    public String submitBooking(@ModelAttribute Booking booking,
                                Model model) {

        var availableRooms = roomRepository.findAllByOrderByFloorAscRoomNumberAsc()
                .stream()
                .filter(room -> "AVAILABLE".equals(room.getStatus()))
                .toList();

        Room room = roomRepository.findAll()
                .stream()
                .filter(r -> r.getRoomNumber().equals(booking.getRoomNumber()))
                .findFirst()
                .orElse(null);

        if (room == null) {
            model.addAttribute("booking", booking);
            model.addAttribute("rooms", availableRooms);
            model.addAttribute("error", "Selected room does not exist.");
            return "book-room";
        }

        if (!"AVAILABLE".equals(room.getStatus())) {
            model.addAttribute("booking", booking);
            model.addAttribute("rooms", availableRooms);
            model.addAttribute("error", "Selected room is not currently available.");
            return "book-room";
        }

        if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
            model.addAttribute("booking", booking);
            model.addAttribute("rooms", availableRooms);
            model.addAttribute("error", "Please select valid check-in and check-out dates.");
            return "book-room";
        }

        if (!booking.getCheckInDate().isBefore(booking.getCheckOutDate())) {
            model.addAttribute("booking", booking);
            model.addAttribute("rooms", availableRooms);
            model.addAttribute("error", "Check-out date must be after check-in date.");
            return "book-room";
        }

        var existingBookings = bookingRepository.findByRoomNumberAndStatusIn(
                booking.getRoomNumber(),
                java.util.List.of("PENDING", "CONFIRMED")
        );

        boolean conflictExists = existingBookings.stream().anyMatch(existing ->
                booking.getCheckInDate().isBefore(existing.getCheckOutDate()) &&
                        booking.getCheckOutDate().isAfter(existing.getCheckInDate())
        );

        if (conflictExists) {
            model.addAttribute("booking", booking);
            model.addAttribute("rooms", availableRooms);
            model.addAttribute("error", "This room is already booked for the selected dates.");
            return "book-room";
        }

        booking.setStatus("PENDING");
        Booking savedBooking = bookingRepository.save(booking);

        return "redirect:/booking-confirmation/" + savedBooking.getId();
    }

    @GetMapping("/bookings")
    public String viewBookings(
            @RequestParam(required = false, defaultValue = "") String guestName,
            @RequestParam(required = false, defaultValue = "") String roomNumber,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            Model model,
            HttpSession session) {

        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        var bookings = bookingRepository.findAll();

        if (!guestName.isBlank()) {
            bookings = bookings.stream()
                    .filter(booking -> booking.getGuestName() != null &&
                            booking.getGuestName().toLowerCase().contains(guestName.toLowerCase()))
                    .toList();
        }

        if (!roomNumber.isBlank()) {
            bookings = bookings.stream()
                    .filter(booking -> booking.getRoomNumber() != null &&
                            booking.getRoomNumber().equalsIgnoreCase(roomNumber))
                    .toList();
        }

        if (!"ALL".equals(status)) {
            bookings = bookings.stream()
                    .filter(booking -> booking.getStatus() != null &&
                            booking.getStatus().equalsIgnoreCase(status))
                    .toList();
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("selectedGuestName", guestName);
        model.addAttribute("selectedRoomNumber", roomNumber);
        model.addAttribute("selectedStatus", status);

        return "bookings";
    }

    @PostMapping("/bookings/approve/{id}")
    public String approveBooking(@PathVariable Long id, HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setStatus("CONFIRMED");

        Room room = roomRepository.findAll()
                .stream()
                .filter(r -> r.getRoomNumber().equals(booking.getRoomNumber()))
                .findFirst()
                .orElse(null);

        if (room != null) {
            room.setStatus("OCCUPIED");
            roomRepository.save(room);
        }

        bookingRepository.save(booking);

        return "redirect:/bookings?success=Booking approved successfully";
    }

    @PostMapping("/bookings/reject/{id}")
    public String rejectBooking(@PathVariable Long id, HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setStatus("REJECTED");
        bookingRepository.save(booking);

        return "redirect:/bookings?success=Booking rejected successfully";
    }

    @PostMapping("/bookings/complete/{id}")
    public String completeStay(@PathVariable Long id, HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        Booking booking = bookingRepository.findById(id).orElseThrow();

        booking.setStatus("COMPLETED");

        Room room = roomRepository.findAll()
                .stream()
                .filter(r -> r.getRoomNumber().equals(booking.getRoomNumber()))
                .findFirst()
                .orElse(null);

        if (room != null) {
            room.setStatus("CLEANING");
            roomRepository.save(room);
        }

        bookingRepository.save(booking);

        return "redirect:/bookings?success=Stay completed successfully";
    }

    @GetMapping("/booking-confirmation/{id}")
    public String showBookingConfirmation(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id).orElseThrow();

        model.addAttribute("booking", booking);

        return "booking-confirmation";
    }

}
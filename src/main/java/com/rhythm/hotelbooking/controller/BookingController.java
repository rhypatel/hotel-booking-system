package com.rhythm.hotelbooking.controller;

import com.rhythm.hotelbooking.model.Booking;
import com.rhythm.hotelbooking.model.GuestUser;
import com.rhythm.hotelbooking.model.Room;
import com.rhythm.hotelbooking.repository.BookingRepository;
import com.rhythm.hotelbooking.repository.GuestUserRepository;
import com.rhythm.hotelbooking.repository.RoomRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestUserRepository guestUserRepository;

    public BookingController(BookingRepository bookingRepository,
                             RoomRepository roomRepository,
                             GuestUserRepository guestUserRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.guestUserRepository = guestUserRepository;
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
                                @RequestParam(required = false) String cardholderName,
                                @RequestParam(required = false) String cardNumber,
                                @RequestParam(required = false) String expiryDate,
                                @RequestParam(required = false) String cvv,
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

        if (booking.getCheckInDate().isBefore(LocalDate.now())) {
            model.addAttribute("booking", booking);
            model.addAttribute("rooms", availableRooms);
            model.addAttribute("error", "Check-in date cannot be in the past.");
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
                java.util.List.of("PENDING", "CONFIRMED", "CHECKED_IN")
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

        boolean paymentStarted =
                (cardholderName != null && !cardholderName.isBlank()) ||
                        (cardNumber != null && !cardNumber.isBlank()) ||
                        (expiryDate != null && !expiryDate.isBlank()) ||
                        (cvv != null && !cvv.isBlank());

        if (paymentStarted) {
            if (cardholderName == null || cardholderName.isBlank()
                    || cardNumber == null || cardNumber.isBlank()
                    || expiryDate == null || expiryDate.isBlank()
                    || cvv == null || cvv.isBlank()) {

                model.addAttribute("booking", booking);
                model.addAttribute("rooms", availableRooms);
                model.addAttribute("error", "Payment is optional, but if you enter payment information, please complete all payment fields.");
                return "book-room";
            }

            String digitsOnly = cardNumber.replaceAll("\\D", "");

            if (!digitsOnly.matches("\\d{16}")) {
                model.addAttribute("booking", booking);
                model.addAttribute("rooms", availableRooms);
                model.addAttribute("error", "Card number must contain exactly 16 digits.");
                return "book-room";
            }

            if (!expiryDate.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
                model.addAttribute("booking", booking);
                model.addAttribute("rooms", availableRooms);
                model.addAttribute("error", "Expiration date must be in MM/YY format.");
                return "book-room";
            }

            if (!cvv.matches("\\d{3,4}")) {
                model.addAttribute("booking", booking);
                model.addAttribute("rooms", availableRooms);
                model.addAttribute("error", "CVV must be 3 or 4 digits.");
                return "book-room";
            }

            booking.setCardholderName(cardholderName);
            booking.setCardLastFour(digitsOnly.substring(12));
            booking.setPaymentStatus("PAYMENT_METHOD_RECORDED");

        } else {
            booking.setCardholderName("Not provided");
            booking.setCardLastFour("N/A");
            booking.setPaymentStatus("PAYMENT_PENDING_AT_CHECK_IN");
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

        if ("COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            return "redirect:/bookings?error=This stay has already been completed";
        }

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

        if (booking.getEmail() != null && !booking.getEmail().isBlank()) {
            GuestUser guestUser = guestUserRepository.findByEmail(booking.getEmail());

            if (guestUser != null) {
                guestUser.setLoyaltyPoints(guestUser.getLoyaltyPoints() + 100);
                guestUserRepository.save(guestUser);
            }
        }

        bookingRepository.save(booking);

        return "redirect:/bookings?success=Stay completed successfully and loyalty points awarded";
    }

    @GetMapping("/booking-confirmation/{id}")
    public String showBookingConfirmation(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id).orElseThrow();

        model.addAttribute("booking", booking);

        return "booking-confirmation";
    }
}
package com.rhythm.hotelbooking.controller;

import com.rhythm.hotelbooking.model.Room;
import com.rhythm.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.rhythm.hotelbooking.repository.BookingRepository;

import java.util.List;

@Controller
public class RoomController {

    private final RoomRepository roomRepository;

    private final BookingRepository bookingRepository;

    public RoomController(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/rooms")
    public String viewRooms(Model model, HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        model.addAttribute("rooms", roomRepository.findAllByOrderByFloorAscRoomNumberAsc());
        model.addAttribute("room", new Room());
        return "rooms";
    }

    @PostMapping("/rooms/add")
    public String addRoom(@ModelAttribute Room room, Model model, HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        int roomNumber;
        try {
            roomNumber = Integer.parseInt(room.getRoomNumber());
        } catch (NumberFormatException e) {
            model.addAttribute("room", new Room());
            model.addAttribute("rooms", roomRepository.findAllByOrderByFloorAscRoomNumberAsc());
            model.addAttribute("error", "Room number must be numeric.");
            return "rooms";
        }

        int floor = room.getFloor();
        int minRoom = floor * 100 + 1;
        int maxRoom = floor * 100 + 30;

        if (roomNumber < minRoom || roomNumber > maxRoom) {
            model.addAttribute("room", new Room());
            model.addAttribute("rooms", roomRepository.findAllByOrderByFloorAscRoomNumberAsc());
            model.addAttribute("error", "For floor " + floor + ", room number must be between " + minRoom + " and " + maxRoom + ".");
            return "rooms";
        }

        if (roomRepository.findAll().stream().anyMatch(r -> r.getRoomNumber().equals(room.getRoomNumber()))) {
            model.addAttribute("room", new Room());
            model.addAttribute("rooms", roomRepository.findAllByOrderByFloorAscRoomNumberAsc());
            model.addAttribute("error", "Room number already exists.");
            return "rooms";
        }

        int position = room.getPosition();

        room.setX(((position - 1) % 10) + 1);

        if ("LEFT".equalsIgnoreCase(room.getSide())) {
            room.setY(((position - 1) / 10) + 1);
        } else if ("RIGHT".equalsIgnoreCase(room.getSide())) {
            room.setY(((position - 1) / 10) + 4);
        } else {
            model.addAttribute("room", new Room());
            model.addAttribute("rooms", roomRepository.findAllByOrderByFloorAscRoomNumberAsc());
            model.addAttribute("error", "Side must be LEFT or RIGHT.");
            return "rooms";
        }

        room.setStatus("AVAILABLE");
        roomRepository.save(room);

        return "redirect:/rooms?success=Room added successfully";
    }

    @GetMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id, HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        roomRepository.deleteById(id);
        return "redirect:/rooms?success=Room deleted successfully";
    }

    @GetMapping("/floor")
    public String showFloorLayout(Model model, HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        var rooms = roomRepository.findAllByOrderByFloorAscRoomNumberAsc();

        var floors = rooms.stream()
                .map(Room::getFloor)
                .distinct()
                .sorted()
                .toList();

        long availableCount = rooms.stream().filter(r -> "AVAILABLE".equals(r.getStatus())).count();
        long occupiedCount = rooms.stream().filter(r -> "OCCUPIED".equals(r.getStatus())).count();
        long cleaningCount = rooms.stream().filter(r -> "CLEANING".equals(r.getStatus())).count();

        model.addAttribute("rooms", rooms);
        model.addAttribute("floors", floors);
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("occupiedCount", occupiedCount);
        model.addAttribute("cleaningCount", cleaningCount);

        return "floor";
    }

    @GetMapping("/toggle/{id}")
    public String toggleRoomStatus(@PathVariable Long id, HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        Room room = roomRepository.findById(id).orElseThrow();

        if ("AVAILABLE".equals(room.getStatus())) {
            room.setStatus("OCCUPIED");
        } else if ("OCCUPIED".equals(room.getStatus())) {
            room.setStatus("CLEANING");
        } else {
            room.setStatus("AVAILABLE");
        }

        roomRepository.save(room);

        return "redirect:/floor";
    }

    @PostMapping("/rooms/update-status/{id}")
    public String updateRoomStatus(@PathVariable Long id,
                                   @RequestParam String action,
                                   HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        Room room = roomRepository.findById(id).orElseThrow();

        if ("checkin".equals(action) && "AVAILABLE".equals(room.getStatus())) {
            room.setStatus("OCCUPIED");
        } else if ("checkout".equals(action) && "OCCUPIED".equals(room.getStatus())) {
            room.setStatus("CLEANING");
        } else if ("cleaned".equals(action) && "CLEANING".equals(room.getStatus())) {
            room.setStatus("AVAILABLE");
        }

        roomRepository.save(room);

        if ("checkin".equals(action)) {
            return "redirect:/rooms?success=Room checked in successfully";
        } else if ("checkout".equals(action)) {
            return "redirect:/rooms?success=Room checked out successfully";
        } else if ("cleaned".equals(action)) {
            return "redirect:/rooms?success=Room marked cleaned successfully";
        }

        return "redirect:/rooms";
    }

    @GetMapping("/rooms/details/{id}")
    public String roomDetails(@PathVariable Long id, Model model, HttpSession session) {
        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        Room room = roomRepository.findById(id).orElseThrow();

        var bookings = bookingRepository.findByRoomNumberOrderByCheckInDateAsc(room.getRoomNumber());

        model.addAttribute("room", room);
        model.addAttribute("bookings", bookings);

        return "room-details";
    }

}
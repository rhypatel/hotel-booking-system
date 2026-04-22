package com.rhythm.hotelbooking.controller;

import com.rhythm.hotelbooking.model.Room;
import com.rhythm.hotelbooking.repository.RoomRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MapController {

    private final RoomRepository roomRepository;

    public MapController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping("/map")
    public String showMap(@RequestParam(required = false, defaultValue = "ALL") String status,
                          Model model,
                          HttpSession session) {

        Object role = session.getAttribute("role");

        if (role == null || !role.equals("ADMIN")) {
            return "redirect:/login";
        }

        List<Room> rooms = roomRepository.findAll();

        if (!"ALL".equals(status)) {
            rooms = rooms.stream()
                    .filter(room -> status.equals(room.getStatus()))
                    .toList();
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("floors", List.of(1, 2, 3));
        model.addAttribute("selectedStatus", status);

        return "map";
    }
}
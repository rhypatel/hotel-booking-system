package com.rhythm.hotelbooking.controller;

import com.rhythm.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GuestMapController {

    private final RoomRepository roomRepository;

    public GuestMapController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping("/guest-map")
    public String showGuestMap(Model model) {
        var rooms = roomRepository.findAllByOrderByFloorAscRoomNumberAsc();

        var floors = rooms.stream()
                .map(room -> room.getFloor())
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("rooms", rooms);
        model.addAttribute("floors", floors);

        return "guest-map";
    }
}
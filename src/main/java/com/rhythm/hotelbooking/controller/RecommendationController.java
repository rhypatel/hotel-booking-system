package com.rhythm.hotelbooking.controller;

import com.rhythm.hotelbooking.model.Room;
import com.rhythm.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class RecommendationController {

    private final RoomRepository roomRepository;

    public RecommendationController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping("/recommend")
    public String showForm() {
        return "recommend";
    }

    @PostMapping("/recommend")
    public String processForm(
            @RequestParam int guests,
            @RequestParam int budget,
            @RequestParam String bedPreference,
            Model model) {

        String recommendation;

        if (guests >= 3 || bedPreference.equals("2")) {
            recommendation = "Double";
        } else if (budget > 200) {
            recommendation = "Suite";
        } else {
            recommendation = "Single";
        }

        List<Room> matchingRooms = roomRepository.findAllByOrderByFloorAscRoomNumberAsc()
                .stream()
                .filter(room -> "AVAILABLE".equals(room.getStatus()))
                .filter(room -> room.getType().equalsIgnoreCase(recommendation))
                .toList();

        model.addAttribute("recommendation", recommendation);
        model.addAttribute("matchingRooms", matchingRooms);

        return "result";
    }
}
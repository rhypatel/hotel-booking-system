package com.rhythm.hotelbooking.controller;

import com.rhythm.hotelbooking.model.Room;
import com.rhythm.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BrowseController {

    private final RoomRepository roomRepository;

    public BrowseController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping("/browse")
    public String browseRooms(
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(required = false, defaultValue = "ALL") String floor,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "NONE") String sort,
            Model model) {

        List<Room> rooms = roomRepository.findAllByOrderByFloorAscRoomNumberAsc();

        if (!"ALL".equals(type)) {
            rooms = rooms.stream()
                    .filter(room -> room.getType().equalsIgnoreCase(type))
                    .toList();
        }

        if (!"ALL".equals(floor)) {
            int selectedFloor = Integer.parseInt(floor);
            rooms = rooms.stream()
                    .filter(room -> room.getFloor() == selectedFloor)
                    .toList();
        }

        if (!"ALL".equals(status)) {
            rooms = rooms.stream()
                    .filter(room -> room.getStatus().equalsIgnoreCase(status))
                    .toList();
        }

        switch (sort) {
            case "PRICE_ASC" -> rooms = rooms.stream()
                    .sorted((a, b) -> Double.compare(a.getPricePerNight(), b.getPricePerNight()))
                    .toList();

            case "PRICE_DESC" -> rooms = rooms.stream()
                    .sorted((a, b) -> Double.compare(b.getPricePerNight(), a.getPricePerNight()))
                    .toList();

            case "FLOOR_ASC" -> rooms = rooms.stream()
                    .sorted((a, b) -> Integer.compare(a.getFloor(), b.getFloor()))
                    .toList();

            case "TYPE_ASC" -> rooms = rooms.stream()
                    .sorted((a, b) -> a.getType().compareToIgnoreCase(b.getType()))
                    .toList();
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedFloor", floor);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedSort", sort);

        return "browse";
    }
}
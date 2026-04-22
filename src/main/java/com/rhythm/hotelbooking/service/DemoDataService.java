package com.rhythm.hotelbooking.service;

import com.rhythm.hotelbooking.model.Room;
import com.rhythm.hotelbooking.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DemoDataService {

    private final RoomRepository roomRepository;
    private final JdbcTemplate jdbcTemplate;

    public DemoDataService(RoomRepository roomRepository, JdbcTemplate jdbcTemplate) {
        this.roomRepository = roomRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void seedIfEmpty() {
        if (roomRepository.count() == 0) {
            resetAndSeedDemoData();
        }
    }

    @Transactional
    public void resetAndSeedDemoData() {
        // Clear old data
        jdbcTemplate.execute("TRUNCATE TABLE booking RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE rooms RESTART IDENTITY CASCADE");

        // Rebuild clean demo room data
        for (int floor = 1; floor <= 3; floor++) {
            for (int r = 1; r <= 30; r++) {
                Room room = new Room();

                room.setRoomNumber(String.valueOf(floor * 100 + r));
                room.setFloor(floor);
                room.setPosition(r);
                room.setPricePerNight(100 + (r * 5));

                if (r <= 15) {
                    room.setSide("LEFT");
                } else {
                    room.setSide("RIGHT");
                }

                if (r % 3 == 0) {
                    room.setType("Suite");
                } else if (r % 2 == 0) {
                    room.setType("Double");
                } else {
                    room.setType("Single");
                }

                if (r % 5 == 0) {
                    room.setStatus("OCCUPIED");
                } else if (r % 4 == 0) {
                    room.setStatus("CLEANING");
                } else {
                    room.setStatus("AVAILABLE");
                }

                if (r <= 15) {
                    room.setX(((r - 1) % 10) + 1);
                    room.setY(((r - 1) / 10) + 1);
                } else {
                    room.setX(((r - 16) % 10) + 1);
                    room.setY(((r - 16) / 10) + 4);
                }

                roomRepository.save(room);
            }
        }
    }
}
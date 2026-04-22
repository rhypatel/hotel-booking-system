package com.rhythm.hotelbooking;

import com.rhythm.hotelbooking.model.Room;
import com.rhythm.hotelbooking.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HotelbookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelbookingApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(RoomRepository roomRepository) {
		return args -> {
			if (roomRepository.count() > 0) {
				return;
			}

			for (int f = 1; f <= 3; f++) {
				for (int r = 1; r <= 30; r++) {
					String roomNum = String.valueOf(f * 100 + r);

					String side = (r <= 15) ? "LEFT" : "RIGHT";

					String type;
					if (r % 3 == 0) {
						type = "Suite";
					} else if (r % 2 == 0) {
						type = "Double";
					} else {
						type = "Single";
					}

					String status;
					if (r % 5 == 0) {
						status = "OCCUPIED";
					} else if (r % 4 == 0) {
						status = "CLEANING";
					} else {
						status = "AVAILABLE";
					}

					int x;
					if (r <= 15) {
						x = ((r - 1) % 10) + 1;
					} else {
						x = ((r - 16) % 10) + 1;
					}

					int y;
					if (r <= 15) {
						y = ((r - 1) / 10) + 1;
					} else {
						y = ((r - 16) / 10) + 4;
					}

					Room room = new Room();
					room.setFloor(f);
					room.setPosition(r);
					room.setPricePerNight(100 + (r * 5));
					room.setRoomNumber(roomNum);
					room.setSide(side);
					room.setType(type);
					room.setStatus(status);
					room.setX(x);
					room.setY(y);

					roomRepository.save(room);
				}
			}
		};
	}
}
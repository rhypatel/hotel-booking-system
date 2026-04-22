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
			roomRepository.save(new Room("101", 1, "LEFT", 1, "Single", 100));
			roomRepository.save(new Room("102", 1, "LEFT", 2, "Double", 150));
			roomRepository.save(new Room("103", 1, "RIGHT", 1, "Suite", 250));
		};
	}
}
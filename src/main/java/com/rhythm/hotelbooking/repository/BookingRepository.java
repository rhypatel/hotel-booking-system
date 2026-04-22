package com.rhythm.hotelbooking.repository;

import com.rhythm.hotelbooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByRoomNumberAndStatusIn(String roomNumber, List<String> statuses);

    List<Booking> findByRoomNumberOrderByCheckInDateAsc(String roomNumber);
}
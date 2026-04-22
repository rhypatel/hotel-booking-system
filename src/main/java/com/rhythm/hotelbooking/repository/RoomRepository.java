package com.rhythm.hotelbooking.repository;

import com.rhythm.hotelbooking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByOrderByFloorAscPositionAsc();

    List<Room> findAllByOrderByFloorAscRoomNumberAsc();
}

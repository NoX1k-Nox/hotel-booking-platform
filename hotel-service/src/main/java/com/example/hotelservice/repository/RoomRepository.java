package com.example.hotelservice.repository;


import com.example.hotelservice.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);
    List<Room> findByAvailableTrueOrderByTimesBookedAscIdAsc();
}
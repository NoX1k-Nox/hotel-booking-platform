package com.example.hotelservice.service;

import com.example.hotelservice.entity.Room;
import com.example.hotelservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final Map<String, Long> reservationMap = new ConcurrentHashMap<>();

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public List<Room> findByHotel(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    @Transactional
    public Room save(Room room) {
        return roomRepository.save(room);
    }

    @Transactional
    public void delete(Long id) {
        roomRepository.deleteById(id);
    }

    @Transactional
    public synchronized boolean reserveRoom(Long roomId, String requestId) {
        if (reservationMap.containsKey(requestId)) {
            return true;
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

        if (!room.isAvailable()) {
            log.warn("Room {} not available", roomId);
            return false;
        }

        room.setAvailable(false);
        room.setTimesBooked(room.getTimesBooked() + 1);
        roomRepository.save(room);
        reservationMap.put(requestId, roomId);

        log.info("Room {} reserved by request {}", roomId, requestId);
        return true;
    }

    @Transactional
    public synchronized void releaseRoom(Long roomId, String requestId) {
        if (reservationMap.containsKey(requestId)) {
            reservationMap.remove(requestId);

            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));

            room.setAvailable(true);
            room.setTimesBooked(Math.max(0, room.getTimesBooked() - 1));
            roomRepository.save(room);

            log.info("Room {} released by request {}", roomId, requestId);
        }
    }

    public List<Room> recommendRooms() {
        List<Room> availableRooms = roomRepository.findAll()
                .stream()
                .filter(Room::isAvailable)
                .sorted(Comparator.comparingInt(Room::getTimesBooked))
                .toList();

        if (availableRooms.isEmpty()) {
            throw new RuntimeException("No available rooms for recommendation");
        }

        return availableRooms;
    }
}

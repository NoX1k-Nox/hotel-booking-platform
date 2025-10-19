package com.example.hotelservice.service;

import com.example.common.dto.RoomDTO;
import com.example.hotelservice.entity.Hotel;
import com.example.hotelservice.repository.HotelRepository;
import com.example.hotelservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    public List<Hotel> findAll() {
        return hotelRepository.findAll();
    }

    public Optional<Hotel> findById(Long id) {
        return hotelRepository.findById(id);
    }

    public Hotel save(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    public void delete(Long id) {
        hotelRepository.deleteById(id);
    }

    public List<RoomDTO> getRecommendedRooms() {
        return roomRepository.findByAvailableTrueOrderByTimesBookedAscIdAsc()
                .stream()
                .map(room -> new RoomDTO(
                        room.getId(),
                        room.getHotel().getId(),
                        room.getNumber(),
                        room.isAvailable(),
                        room.getTimesBooked()))
                .toList();
    }
}

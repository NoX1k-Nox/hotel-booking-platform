package com.example.bookingservice.service;

import com.example.bookingservice.dto.HotelRoomDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "hotel-service")
public interface HotelClient {

    @GetMapping("/api/rooms/hotel/{hotelId}")
    List<HotelRoomDto> getRoomsByHotel(@PathVariable Long hotelId);
}

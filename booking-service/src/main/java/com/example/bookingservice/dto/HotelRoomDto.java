package com.example.bookingservice.dto;

import lombok.Data;

@Data
public class HotelRoomDto {
    private Long id;
    private String number;
    private String type;
    private Double pricePerNight;
    private boolean available;
}

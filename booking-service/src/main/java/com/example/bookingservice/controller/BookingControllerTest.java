package com.example.bookingservice.controller;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.BookingStatus;
import com.example.bookingservice.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void testGetUserBookings() throws Exception {
        Booking booking = Booking.builder()
                .userId(1L)
                .hotelId(1L)
                .roomId(101L)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(2))
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.getCurrentUserBookings()).thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));

        verify(bookingService, times(1)).getCurrentUserBookings();
    }

    @Test
    void testCreateBooking() throws Exception {
        String bookingJson = """
                {
                    "hotelId": 1,
                    "roomId": 101,
                    "checkInDate": "2025-10-20",
                    "checkOutDate": "2025-10-22"
                }
                """;

        Booking createdBooking = Booking.builder()
                .userId(1L)
                .hotelId(1L)
                .roomId(101L)
                .checkInDate(LocalDate.of(2025, 10, 20))
                .checkOutDate(LocalDate.of(2025, 10, 22))
                .status(BookingStatus.PENDING)
                .build();

        when(bookingService.createBooking(any(Booking.class), eq(false))).thenReturn(createdBooking);

        mockMvc.perform(post("/bookings")
                        .param("autoSelect", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(bookingService, times(1)).createBooking(any(Booking.class), eq(false));
    }

    @Test
    void testCancelBooking() throws Exception {
        doNothing().when(bookingService).cancelBooking(1L);

        mockMvc.perform(delete("/bookings/1"))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).cancelBooking(1L);
    }
}

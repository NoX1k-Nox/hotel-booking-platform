package com.example.bookingservice.service;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.BookingStatus;
import com.example.bookingservice.repository.BookingRepository;
import com.example.common.dto.RoomDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBooking_successfulAutoSelect() {
        Booking booking = new Booking();
        booking.setCheckInDate(LocalDate.now());
        booking.setCheckOutDate(LocalDate.now().plusDays(2));

        RoomDTO room = new RoomDTO();
        room.setId(100L);

        when(restTemplate.getForEntity("http://hotel-service/api/rooms/recommend", RoomDTO[].class))
                .thenReturn(ResponseEntity.ok(new RoomDTO[]{room}));

        when(restTemplate.postForEntity(anyString(), isNull(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        when(bookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Booking result = bookingService.createBooking(booking, true);

        assertNotNull(result.getRoomId());
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        verify(bookingRepository, atLeast(2)).save(any());
    }

    @Test
    void cancelBooking_shouldSetStatusCancelled() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoomId(10L);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(restTemplate.postForEntity(anyString(), isNull(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void getCurrentUserBookings_returnsList() {
        Booking booking = new Booking();
        booking.setUserId(1L);

        BookingService spyService = spy(bookingService);
        doReturn(1L).when(spyService).getCurrentUserId();
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(booking));

        List<Booking> bookings = spyService.getCurrentUserBookings();
        assertEquals(1, bookings.size());
    }
}

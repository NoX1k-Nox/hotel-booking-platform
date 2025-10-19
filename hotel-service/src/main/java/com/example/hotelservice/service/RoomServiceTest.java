package com.example.hotelservice.service;

import com.example.hotelservice.entity.Hotel;
import com.example.hotelservice.entity.Room;
import com.example.hotelservice.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll_shouldReturnAllRooms() {
        Room room1 = new Room();
        Room room2 = new Room();

        when(roomRepository.findAll()).thenReturn(List.of(room1, room2));

        List<Room> result = roomService.findAll();

        assertEquals(2, result.size());
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    void findByHotel_shouldReturnRoomsByHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);

        Room room = new Room();
        room.setHotel(hotel);

        when(roomRepository.findByHotelId(hotel.getId())).thenReturn(List.of(room));

        List<Room> result = roomService.findByHotel(hotel.getId());

        assertEquals(1, result.size());
        assertEquals(hotel, result.get(0).getHotel());
        verify(roomRepository, times(1)).findByHotelId(hotel.getId());
    }

    @Test
    void reserveRoom_shouldReserveAvailableRoom() {
        Long roomId = 1L;
        String requestId = "req1";

        Room room = new Room();
        room.setId(roomId);
        room.setAvailable(true);
        room.setTimesBooked(0);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);

        boolean reserved = roomService.reserveRoom(roomId, requestId);

        assertTrue(reserved);
        assertFalse(room.isAvailable());
        assertEquals(1, room.getTimesBooked());
        verify(roomRepository, times(1)).save(room);
    }

    @Test
    void reserveRoom_shouldReturnFalseIfNotAvailable() {
        Long roomId = 1L;
        String requestId = "req1";

        Room room = new Room();
        room.setId(roomId);
        room.setAvailable(false);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        boolean reserved = roomService.reserveRoom(roomId, requestId);

        assertFalse(reserved);
        verify(roomRepository, never()).save(any());
    }

    @Test
    void releaseRoom_shouldMakeRoomAvailable() {
        Long roomId = 1L;
        String requestId = "req1";

        Room room = new Room();
        room.setId(roomId);
        room.setAvailable(false);
        room.setTimesBooked(2);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArguments()[0]);
        roomService.reserveRoom(roomId, requestId);

        roomService.releaseRoom(roomId, requestId);

        assertTrue(room.isAvailable());
        assertEquals(1, room.getTimesBooked());
        verify(roomRepository, times(2)).save(room);
    }

    @Test
    void recommendRooms_shouldReturnAvailableSortedRooms() {
        Room room1 = new Room();
        room1.setId(1L);
        room1.setAvailable(true);
        room1.setTimesBooked(3);

        Room room2 = new Room();
        room2.setId(2L);
        room2.setAvailable(true);
        room2.setTimesBooked(1);

        Room room3 = new Room();
        room3.setId(3L);
        room3.setAvailable(false);

        when(roomRepository.findAll()).thenReturn(List.of(room1, room2, room3));

        List<Room> recommended = roomService.recommendRooms();

        assertEquals(2, recommended.size());
        assertEquals(2L, recommended.get(0).getId());
        assertEquals(1L, recommended.get(1).getId());
    }
}

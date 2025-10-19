package com.example.hotelservice.controller;

import com.example.hotelservice.entity.Room;
import com.example.hotelservice.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Test
    void getRecommendedRooms_shouldReturnList() throws Exception {
        Room room1 = new Room();
        room1.setId(1L);

        when(roomService.recommendRooms()).thenReturn(List.of(room1));

        mockMvc.perform(get("/api/rooms/recommend"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(roomService, times(1)).recommendRooms();
    }

    @Test
    void reserveRoom_shouldReturnOk() throws Exception {
        Long roomId = 1L;
        String requestId = "test-request";

        when(roomService.reserveRoom(roomId, requestId)).thenReturn(true);

        mockMvc.perform(post("/api/rooms/{id}/confirm-availability", roomId)
                        .param("requestId", requestId))
                .andExpect(status().isOk());

        verify(roomService, times(1)).reserveRoom(roomId, requestId);
    }

    @Test
    void releaseRoom_shouldReturnOk() throws Exception {
        Long roomId = 1L;
        String requestId = "test-request";

        doNothing().when(roomService).releaseRoom(roomId, requestId);

        mockMvc.perform(post("/api/rooms/{id}/release", roomId)
                        .param("requestId", requestId))
                .andExpect(status().isOk());

        verify(roomService, times(1)).releaseRoom(roomId, requestId);
    }
}

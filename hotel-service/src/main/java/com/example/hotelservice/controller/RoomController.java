package com.example.hotelservice.controller;

import com.example.hotelservice.entity.Room;
import com.example.hotelservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<List<Room>> getAll() {
        return ResponseEntity.ok(roomService.findAll());
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<Room>> getByHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.findByHotel(hotelId));
    }

    @PostMapping
    public ResponseEntity<Room> create(@RequestBody Room room) {
        return ResponseEntity.ok(roomService.save(room));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm-availability")
    public ResponseEntity<String> confirmAvailability(@PathVariable Long id,
                                                      @RequestParam String requestId) {
        boolean success = roomService.reserveRoom(id, requestId);
        return success ? ResponseEntity.ok("Reserved") : ResponseEntity.status(409).body("Room not available");
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<String> releaseRoom(@PathVariable Long id,
                                              @RequestParam String requestId) {
        roomService.releaseRoom(id, requestId);
        return ResponseEntity.ok("Released");
    }
}

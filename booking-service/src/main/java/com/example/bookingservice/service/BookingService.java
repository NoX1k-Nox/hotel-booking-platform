package com.example.bookingservice.service;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.BookingStatus;
import com.example.bookingservice.repository.BookingRepository;
import com.example.common.dto.RoomDTO;
import com.example.common.util.RequestIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;
    private final String hotelServiceUrl = "http://hotel-service/api/rooms";

    @Transactional
    public Booking createBooking(Booking booking, boolean autoSelect) {
        Long currentUserId = getCurrentUserId();
        booking.setUserId(currentUserId);

        String bookingId = RequestIdGenerator.generate();
        log.info("[{}] Creating PENDING booking", bookingId);

        if (autoSelect) {
            RoomDTO recommendedRoom = selectRoomAuto();
            booking.setRoomId(recommendedRoom.getId());
            log.info("[{}] Auto-selected room id={}", bookingId, booking.getRoomId());
        }

        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        try {
            String url = hotelServiceUrl + "/" + booking.getRoomId() + "/confirm-availability?requestId=" + bookingId;
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                log.info("[{}] Booking CONFIRMED", bookingId);
            } else {
                compensateBooking(booking, bookingId);
            }
        } catch (Exception e) {
            log.error("[{}] Error confirming room availability: {}", bookingId, e.getMessage());
            compensateBooking(booking, bookingId);
        }

        return booking;
    }

    private void compensateBooking(Booking booking, String bookingId) {
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        log.info("[{}] Booking CANCELLED, sending release to Hotel Service", bookingId);
        try {
            String url = hotelServiceUrl + "/" + booking.getRoomId() + "/release?requestId=" + bookingId;
            restTemplate.postForEntity(url, null, String.class);
        } catch (Exception e) {
            log.error("[{}] Failed to release room in Hotel Service: {}", bookingId, e.getMessage());
        }
    }

    private RoomDTO selectRoomAuto() {
        String url = hotelServiceUrl + "/recommend";
        ResponseEntity<RoomDTO[]> response = restTemplate.getForEntity(url, RoomDTO[].class);

        RoomDTO[] rooms = response.getBody();
        if (rooms == null || rooms.length == 0) {
            throw new RuntimeException("No rooms available for auto-selection");
        }
        return rooms[0];
    }

    @Transactional(readOnly = true)
    public List<Booking> getCurrentUserBookings() {
        Long currentUserId = getCurrentUserId();
        return bookingRepository.findByUserId(currentUserId);
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        log.info("Booking {} cancelled successfully", id);

        try {
            String bookingId = RequestIdGenerator.generate();
            String url = hotelServiceUrl + "/" + booking.getRoomId() + "/release?requestId=" + bookingId;
            restTemplate.postForEntity(url, null, String.class);
            log.info("[{}] Released room {} in Hotel Service", bookingId, booking.getRoomId());
        } catch (Exception e) {
            log.error("Failed to release room for booking {}: {}", id, e.getMessage());
        }
    }

    Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return Long.parseLong(userDetails.getUsername());
        }
        throw new RuntimeException("Cannot get current user ID");
    }
}

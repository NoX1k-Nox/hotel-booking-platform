package com.example.bookingservice.controller;

import com.example.bookingservice.dto.CreateUserRequest;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.entity.User;
import com.example.bookingservice.service.BookingService;
import com.example.bookingservice.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @DeleteMapping("/{id}")
    @RolesAllowed("USER")
    public void cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
    }

    @PostMapping("/user")
    @RolesAllowed("ADMIN")
    public User createUser(@RequestBody CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "USER");

        return userService.save(user);
    }

    @GetMapping
    @RolesAllowed("USER")
    @Operation(summary = "История бронирований пользователя",
            description = "Возвращает список всех бронирований текущего пользователя")
    public List<Booking> getUserBookings() {
        return bookingService.getCurrentUserBookings();
    }

    @PostMapping
    @RolesAllowed("USER")
    @Operation(summary = "Создание бронирования",
            description = "Создаёт бронирование. autoSelect = true — автоматический выбор номера")
    public Booking createBooking(@RequestBody Booking booking,
                                 @RequestParam(defaultValue = "false") boolean autoSelect) {
        return bookingService.createBooking(booking, autoSelect);
    }
}

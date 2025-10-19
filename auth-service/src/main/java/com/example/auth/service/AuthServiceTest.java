package com.example.auth.service;

import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_success() {
        String username = "test";
        String password = "pass";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPass");
        when(jwtService.generateToken(username, Role.USER.name())).thenReturn("token123");

        User savedUser = User.builder().username(username).password("encodedPass").role(Role.USER).build();
        when(userRepository.save(any())).thenReturn(savedUser);

        Map<String, String> result = authService.register(username, password);
        assertEquals("token123", result.get("token"));
    }

    @Test
    void login_success() {
        String username = "test";
        String password = "pass";

        User user = User.builder().username(username).password("encodedPass").role(Role.USER).build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "encodedPass")).thenReturn(true);
        when(jwtService.generateToken(username, Role.USER.name())).thenReturn("token123");

        Map<String, String> result = authService.login(username, password);
        assertEquals("token123", result.get("token"));
    }

    @Test
    void login_invalidPassword_throws() {
        String username = "test";
        String password = "wrong";

        User user = User.builder().username(username).password("encodedPass").role(Role.USER).build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "encodedPass")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(username, password));
        assertEquals("Invalid credentials", ex.getMessage());
    }
}

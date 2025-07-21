package com.calendar.CalendarApplication.services;

import com.calendar.CalendarApplication.dtos.user.UserDto;
import com.calendar.CalendarApplication.entity.User;
import com.calendar.CalendarApplication.repository.UserRepository;
import com.calendar.CalendarApplication.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should create a new user when user does not exist")
    void createUserCase1() {
        UserDto dto = new UserDto("Gustavo", "email@email.com", "senha123", LocalDate.parse("2024-07-21"));
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("Gustavo", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should return existing user if already exists")
    void createUserCase2() {
        User existing = new User("Gustavo", "email@email.com", new BCryptPasswordEncoder().encode("senha123"), LocalDate.parse("2024-07-21"));
        when(userRepository.findByEmail("email@email.com")).thenReturn(Optional.of(existing));

        UserDto dto = new UserDto("Gustavo", "email@email.com", "senha123", LocalDate.parse("2024-07-21"));

        User result = userService.createUser(dto);

        assertEquals(existing, result);
        verify(userRepository, never()).save(any());
    }


    @Test
    @DisplayName("Should return token if authentication is valid")
    void authenticateUserCase1() {
        String password = "senha123";
        User user = new User("Gustavo", "email@email.com", new BCryptPasswordEncoder().encode(password), LocalDate.parse("2024-07-21"));

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user.getUsername())).thenReturn("fake-token");

        String result = userService.authenticateUser(user.getEmail(), password);

        assertEquals("fake-token", result);
    }

    @Test
    @DisplayName("Should return null if authentication fails")
    void authenticateUserCase2() {
        when(userRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());

        String result = userService.authenticateUser("email@email.com", "senha");

        assertNull(result);
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }
}
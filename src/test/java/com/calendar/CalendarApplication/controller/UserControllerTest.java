package com.calendar.CalendarApplication.controller;

import com.calendar.CalendarApplication.controller.UserController;
import com.calendar.CalendarApplication.dtos.user.*;
import com.calendar.CalendarApplication.entity.User;
import com.calendar.CalendarApplication.services.*;
import com.calendar.CalendarApplication.repository.UserRepository;
import com.calendar.CalendarApplication.utils.JwtUtil;
import com.calendar.CalendarApplication.dtos.event.GetEventsDto;
import com.calendar.CalendarApplication.entity.Notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.validation.Valid;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.*;

import java.util.Optional;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserService userService;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    UserRepository userRepository;

    @Mock
    EventService eventService;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    UserController userController;

    @BeforeEach
    void setup() {
    }

    @Test
    @DisplayName("Should return user successfully")
    void createUser_success() throws Exception {
        UserDto userDto = new UserDto("testuser", "test@email.com", "password123", LocalDate.of(2000, 1, 1));
        User createdUser = new User();
        createdUser.setUsername(userDto.username());
        createdUser.setEmail(userDto.email());
        createdUser.setBirthDate(userDto.birth_date());

        when(userService.createUser(userDto)).thenReturn(createdUser);

        Notification notification = new Notification();
        when(notificationService.newUserNotification(createdUser)).thenReturn(notification);

        doNothing().when(notificationService).sendNotificationToUser(eq(notification), any());

        ResponseEntity<?> response = userController.createUser(userDto);

        assertEquals(CREATED, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("Usuário criado com sucesso!", body.get("message"));
        assertNotNull(body.get("user"));

        verify(userService).createUser(userDto);
        verify(notificationService).newUserNotification(createdUser);
        verify(notificationService).sendNotificationToUser(eq(notification), any());
    }

    @Test
    @DisplayName("Should return that username already exists")
    void createUser_usernameAlreadyExists() throws Exception {
        UserDto userDto = new UserDto("existinguser", "email@domain.com", "pass", LocalDate.of(1990, 1, 1));

        when(userService.createUser(userDto)).thenReturn(null);

        ResponseEntity<?> response = userController.createUser(userDto);

        assertEquals(409, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Erro: Este nome de usuário já existe. Tente com outro nome de usuário", body.get("message"));
    }

    @Test
    @DisplayName("Should authenticate user successfully")
    void authUser_success() throws Exception {
        String email = "test@email.com";
        String password = "password123";
        LoginDto loginDto = new LoginDto(email, password);

        String fakeToken = "token123";
        when(userService.authenticateUser(email, password)).thenReturn(fakeToken);

        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setEmail(email);
        user.setBirthDate(LocalDate.of(2000, 1, 1));

        when(userService.getUser(email, password)).thenReturn(Optional.of(user));

        List<GetEventsDto> events = List.of();
        when(eventService.findEventsByUserId(user.getId())).thenReturn(events);

        ResponseEntity<?> response = userController.authUser(loginDto);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getHeaders().containsKey("Authorization"));
        assertEquals("Bearer " + fakeToken, response.getHeaders().getFirst("Authorization"));

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Login realizado com sucesso", body.get("message"));
        assertNotNull(body.get("user"));
        assertNotNull(body.get("user_events"));
    }

    @Test
    @DisplayName("Should return invalid user credentials status code")
    void authUser_invalidCredentials() throws Exception {
        LoginDto loginDto = new LoginDto("wrong@email.com", "badpassword");

        when(userService.authenticateUser(loginDto.email(), loginDto.password())).thenReturn(null);

        ResponseEntity<?> response = userController.authUser(loginDto);

        assertEquals(401, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("E-mail ou senha inválidos", body.get("message"));
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_success() throws Exception {
        String token = "valid-token";
        UpdateUserDto updateUserDto = new UpdateUserDto(1, "newname", "newemail@email.com", "", LocalDate.of(2000, 1, 1));
        String bearer = "Bearer " + token;

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setUsername("oldname");

        when(userRepository.findById(updateUserDto.id())).thenReturn(Optional.of(existingUser));
        when(jwtUtil.validateToken(token, existingUser.getUsername())).thenReturn(true);

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setUsername(updateUserDto.username());
        updatedUser.setEmail(updateUserDto.email());
        updatedUser.setBirthDate(updateUserDto.birthDate());

        when(userService.updateUser(updateUserDto)).thenReturn(updatedUser);

        ResponseEntity<?> response = userController.updateUser(updateUserDto, bearer);

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Usuário atualizado com sucesso!", body.get("message"));
        assertEquals(updatedUser, body.get("user"));
    }

    @Test
    @DisplayName("Should not update user because of invalid token")
    void updateUser_invalidToken() {
        String token = "invalid-token";
        UpdateUserDto updateUserDto = new UpdateUserDto(1, "name", "email@email.com", "" ,LocalDate.of(2000, 1, 1));
        String bearer = "Bearer " + token;

        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setUsername("oldname");

        when(userRepository.findById(updateUserDto.id())).thenReturn(Optional.of(existingUser));
        when(jwtUtil.validateToken(token, existingUser.getUsername())).thenReturn(false);

        ResponseEntity<?> response = userController.updateUser(updateUserDto, bearer);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Token inválido ou expirado", response.getBody());
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_success() {
        int userId = 1;
        String token = "valid-token";
        String bearer = "Bearer " + token;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("user1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(jwtUtil.validateToken(token, existingUser.getUsername())).thenReturn(true);
        when(userService.deleteUser(userId)).thenReturn("Usuário deletado com sucesso");

        ResponseEntity<?> response = userController.deleteUser(userId, bearer);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Usuário deletado com sucesso", response.getBody());
    }

    @Test
    @DisplayName("Should return user not found")
    void deleteUser_notFound() {
        int userId = 1;
        String token = "valid-token";
        String bearer = "Bearer " + token;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("user1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(jwtUtil.validateToken(token, existingUser.getUsername())).thenReturn(true);
        when(userService.deleteUser(userId)).thenReturn("Usuário não encontrado");

        ResponseEntity<?> response = userController.deleteUser(userId, bearer);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Usuário não encontrado", response.getBody());
    }

    @Test
    @DisplayName("Should not be able to run because of invalid token")
    void deleteUser_invalidToken() {
        int userId = 1;
        String token = "invalid-token";
        String bearer = "Bearer " + token;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("user1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(jwtUtil.validateToken(token, existingUser.getUsername())).thenReturn(false);

        ResponseEntity<?> response = userController.deleteUser(userId, bearer);

        assertEquals(401, response.getStatusCodeValue());
    }
}

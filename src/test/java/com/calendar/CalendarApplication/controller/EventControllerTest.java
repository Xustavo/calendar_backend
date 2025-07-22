package com.calendar.CalendarApplication.controller;

import com.calendar.CalendarApplication.controller.EventController;
import com.calendar.CalendarApplication.dtos.event.*;
import com.calendar.CalendarApplication.entity.Notification;
import com.calendar.CalendarApplication.entity.User;
import com.calendar.CalendarApplication.entity.Event;
import com.calendar.CalendarApplication.repository.EventRepository;
import com.calendar.CalendarApplication.repository.UserRepository;
import com.calendar.CalendarApplication.services.EventService;
import com.calendar.CalendarApplication.services.NotificationService;
import com.calendar.CalendarApplication.utils.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    JwtUtil jwtUtil;

    @Mock
    EventService eventService;

    @Mock
    EventRepository eventRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    EventController eventController;

    private final String validBearer = "Bearer valid-token";
    private final String invalidBearer = "Bearer invalid-token";

    @BeforeEach
    void setup() {
    }

    @Test
    void createEvent_success() {
        CreateEventDto eventDto = new CreateEventDto(1, Event.EventType.COMPROMISE,
                "Título", "Descrição", new Date());

        User user = new User();
        user.setId(1);
        user.setUsername("testuser");

        Optional<User> optionalUser = Optional.of(user);

        Event createdEvent = new Event();
        createdEvent.setId(10L);

        when(userRepository.findById(eventDto.user_id())).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken("valid-token", user.getUsername())).thenReturn(true);
        when(eventService.createEvent(eventDto, user)).thenReturn(createdEvent);

        Notification notification = new Notification();
        when(notificationService.newEventNotification(user, createdEvent)).thenReturn(notification);
        doNothing().when(notificationService).sendNotificationToUser(notification, optionalUser);

        ResponseEntity<?> response = eventController.createEvent(eventDto, validBearer);

        assertEquals(201, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Evento criado com sucesso!", body.get("message"));
        assertNotNull(body.get("evento:"));

        verify(eventService).createEvent(eventDto, user);
        verify(notificationService).newEventNotification(user, createdEvent);
        verify(notificationService).sendNotificationToUser(notification, optionalUser);
    }

    @Test
    void createEvent_invalidToken() {
        CreateEventDto eventDto = new CreateEventDto(1,
                Event.EventType.COMPROMISE, "Título", "Descrição", new Date());
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");

        when(userRepository.findById(eventDto.user_id())).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken("invalid-token", user.getUsername())).thenReturn(false);

        ResponseEntity<?> response = eventController.createEvent(eventDto, invalidBearer);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Token inválido ou expirado", response.getBody());
    }

    @Test
    void createEvent_missingBearer() {
        CreateEventDto eventDto = mock(CreateEventDto.class);

        ResponseEntity<?> response = eventController.createEvent(eventDto, "InvalidHeader");

        assertEquals(403, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("Erro"));
    }

    @Test
    void getEvent_success() {
        int userId = 1;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        List<GetEventsDto> events = List.of(new GetEventsDto(1L, Event.EventType.COMPROMISE , "Evento 1", "Descrição", new Date(), false));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken("valid-token", user.getUsername())).thenReturn(true);
        when(eventService.findEventsByUserId(userId)).thenReturn(events);

        ResponseEntity<?> response = eventController.getEvent(userId, validBearer);

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("events"));
        assertEquals(events, body.get("events"));
    }

    @Test
    void getEvent_noEventsFound() {
        int userId = 1;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken("valid-token", user.getUsername())).thenReturn(true);
        when(eventService.findEventsByUserId(userId)).thenReturn(null);

        ResponseEntity<?> response = eventController.getEvent(userId, validBearer);

        assertEquals(404, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Não foram encontrados eventos desse usuário", body.get("message"));
    }

    @Test
    void updateEvent_success() {
        UpdateEventDto updateEventDto = new UpdateEventDto(99L, Event.EventType.COMPROMISE, "Título atualizado", "Descrição", new Date(), false, 1);

        User user = new User();
        user.setId(1);
        user.setUsername("testuser");

        Event updatedEvent = new Event();
        updatedEvent.setId(1L);

        when(userRepository.findById((int) updateEventDto.userId())).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken("valid-token", user.getUsername())).thenReturn(true);
        when(eventService.updateEvent(updateEventDto)).thenReturn(updatedEvent);

        ResponseEntity<?> response = eventController.updateEvent(updateEventDto, validBearer);

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Evento atualizado com sucesso!", body.get("Message"));
        assertNotNull(body.get("evento:"));
    }

    @Test
    void updateEvent_eventNotFound() {
        UpdateEventDto updateEventDto = new UpdateEventDto(99L, Event.EventType.COMPROMISE, "Título", "Descrição", new Date(), false, 1);
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");

        when(userRepository.findById((int) updateEventDto.userId())).thenReturn(Optional.of(user));
        when(jwtUtil.validateToken("valid-token", user.getUsername())).thenReturn(true);
        when(eventService.updateEvent(updateEventDto)).thenReturn(null);

        ResponseEntity<?> response = eventController.updateEvent(updateEventDto, validBearer);

        assertEquals(404, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Evento não encontrado", body.get("Message"));
    }

    @Test
    void deleteEvent_success() {
        Long eventId = 1L;
        Event event = new Event();
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        event.setUser(user);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(jwtUtil.validateToken("valid-token", user.getUsername())).thenReturn(true);
        doNothing().when(eventService).deleteEvent(event);

        ResponseEntity<?> response = eventController.deleteEvent(eventId, validBearer);

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Evento deletado com sucesso!", body.get("message"));

        verify(eventService).deleteEvent(event);
    }

    @Test
    void deleteEvent_eventNotFound() {
        Long eventId = 999L;

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = eventController.deleteEvent(eventId, validBearer);

        assertEquals(404, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Evento não encontrado", body.get("message"));
    }

    @Test
    void deleteEvent_invalidToken() {
        Long eventId = 1L;
        Event event = new Event();
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        event.setUser(user);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(jwtUtil.validateToken("invalid-token", user.getUsername())).thenReturn(false);

        ResponseEntity<?> response = eventController.deleteEvent(eventId, invalidBearer);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Token inválido ou expirado", response.getBody());
    }
}

package com.calendar.CalendarApplication.services;

import com.calendar.CalendarApplication.dtos.event.CreateEventDto;
import com.calendar.CalendarApplication.entity.Event;
import com.calendar.CalendarApplication.entity.User;
import com.calendar.CalendarApplication.repository.EventRepository;
import com.calendar.CalendarApplication.repository.UserRepository;
import com.calendar.CalendarApplication.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Should return saved event when user is not null")
    void createEventCase1() {
        User user = new User();
        user.setId(1L);

        CreateEventDto dto = new CreateEventDto(user.getId(), Event.EventType.COMPROMISE, "Título", "Descrição", new Date());

        Event eventToSave = new Event(user, dto.eventType(), dto.title(), dto.description(), dto.date());
        Event savedEvent = new Event(user, dto.eventType(), dto.title(), dto.description(), dto.date());
        savedEvent.setId(1L);

        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        Event result = eventService.createEvent(dto, user);

        assertNotNull(result);
        assertEquals(savedEvent.getId(), result.getId());
        assertEquals("Título", result.getTitle());
        assertEquals(Event.EventType.COMPROMISE, result.getEventType());

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Should not create event because user is null")
    void createEventCase2() {
        CreateEventDto dto = new CreateEventDto(1, Event.EventType.COMPROMISE, "Título", "Descrição", new Date());

        Event result = eventService.createEvent(dto, null);

        assertNull(result);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void findEventsByUserId() {
    }
}
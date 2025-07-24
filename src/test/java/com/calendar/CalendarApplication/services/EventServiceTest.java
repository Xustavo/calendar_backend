package com.calendar.CalendarApplication.services;

import com.calendar.CalendarApplication.dtos.event.CreateEventDto;
import com.calendar.CalendarApplication.dtos.event.GetEventsDto;
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
import java.util.List;

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
    @DisplayName("Finds user correctly by its id")
    void findEventsByUserIdCase1() {
        long userId = 1L;

        Event event1 = new Event();
        event1.setId(10L);
        event1.setEventType(Event.EventType.COMPROMISE);
        event1.setTitle("Reunião");
        event1.setDescription("Com o time");
        event1.setDate(new Date());
        event1.setCompleted(true);

        Event event2 = new Event();
        event2.setId(20L);
        event2.setEventType(Event.EventType.TASK);
        event2.setTitle("Estudar");
        event2.setDescription("Matemática");
        event2.setDate(new Date());
        event2.setCompleted(false);

        List<Event> mockEvents = List.of(event1, event2);

        when(eventRepository.findByUserId(userId)).thenReturn(mockEvents);

        List<GetEventsDto> result = eventService.findEventsByUserId(userId);

        assertEquals(2, result.size());
        assertEquals("Reunião", result.get(0).title());
        assertEquals("Estudar", result.get(1).title());
        verify(eventRepository, times(1)).findByUserId(userId);
    }


    @Test
    @DisplayName("Does not find the user because the id is wrong")
    void findEventsByUserIdCase2() {

        long wrongUserId = 999L;
        when(eventRepository.findByUserId(wrongUserId)).thenReturn(List.of());

        List<GetEventsDto> result = eventService.findEventsByUserId(wrongUserId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(eventRepository, times(1)).findByUserId(wrongUserId);
    }

}
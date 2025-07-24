package com.calendar.CalendarApplication.repository;

import com.calendar.CalendarApplication.entity.Event;
import com.calendar.CalendarApplication.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class EventRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Test
    @DisplayName("Should return only events in the interval")
    void findByDateBetweenCase1() {
        User user = new User("gustavo", "gustavo@email.com", "pass", LocalDate.of(2000, 1, 1));
        user = userRepository.save(user);

        Date date1 = java.sql.Date.valueOf(LocalDate.of(2025, 7, 1));
        Date date2 = java.sql.Date.valueOf(LocalDate.of(2025, 7, 15));
        Date date3 = java.sql.Date.valueOf(LocalDate.of(2025, 8, 1));

        Event event1 = new Event(user, Event.EventType.COMPROMISE, "Evento 1", "Descrição 1", date1);
        Event event2 = new Event(user, Event.EventType.TASK, "Evento 2", "Descrição 2", date2);
        Event event3 = new Event(user, Event.EventType.COMPROMISE, "Evento 3", "Descrição 3", date3);

        eventRepository.saveAll(List.of(event1, event2, event3));

        Date startDate = java.sql.Date.valueOf(LocalDate.of(2025, 7, 1));
        Date endDate = java.sql.Date.valueOf(LocalDate.of(2025, 7, 31));

        List<Event> result = eventRepository.findByDateBetween(startDate, endDate);

        assertEquals(2, result.size());
        assertTrue(result.contains(event1));
        assertTrue(result.contains(event2));
        assertFalse(result.contains(event3));
    }

    @Test
    @DisplayName("Should return empty list")
    void findByDateBetweenCase2() {

        User user = new User("gustavo", "gustavo@email.com", "pass", LocalDate.of(2000, 1, 1));
        userRepository.save(user);

        Date startDate = java.sql.Date.valueOf(LocalDate.of(2020, 1, 1));
        Date endDate = java.sql.Date.valueOf(LocalDate.of(2020, 1, 31));

        List<Event> result = eventRepository.findByDateBetween(startDate, endDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
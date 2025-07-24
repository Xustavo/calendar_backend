package com.calendar.CalendarApplication.scheduling;

import com.calendar.CalendarApplication.entity.Event;
import com.calendar.CalendarApplication.entity.User;
import com.calendar.CalendarApplication.services.EventService;
import com.calendar.CalendarApplication.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTasksTest {
    @Mock
    EventService eventService;

    @Mock
    NotificationService notificationService;

    ScheduledTasks scheduledTasks;

    @BeforeEach
    void setup() {
        scheduledTasks = new ScheduledTasks(eventService, notificationService);
    }

    @Test
    void shouldSendNotificationsForUpcomingEvents() {
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");

        Event event1 = new Event();
        event1.setId(101L);
        event1.setUser(user);

        Event event2 = new Event();
        event2.setId(102L);
        event2.setUser(user);

        List<Event> events = List.of(event1, event2);

        when(eventService.getNearestEvents(any(Date.class), any(Date.class))).thenReturn(events);

        scheduledTasks.processDueDateTask(24);

        verify(eventService).getNearestEvents(any(Date.class), any(Date.class));

        verify(notificationService, times(2))
                .sendDueDateNotification(any(Event.class), eq(24), any(Optional.class));
    }
}
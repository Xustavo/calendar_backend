package com.calendar.CalendarApplication.repository;

import com.calendar.CalendarApplication.entity.Event;
import com.calendar.CalendarApplication.entity.Notification;
import com.calendar.CalendarApplication.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    EventRepository eventRepository;

    @Test
    @DisplayName("Should return user successfully")
    void findUserByNotificationIdCase1() {
        User user = new User("gustavo", "g@email.com", "pass", LocalDate.of(2000, 1, 1));
        user = userRepository.save(user);

        Event event = new Event(user, Event.EventType.COMPROMISE, "Evento teste", "Descrição do evento", new Date());
        event = eventRepository.save(event);

        Notification notification = new Notification("Notificação", "Descrição", event, user, false);
        notification = notificationRepository.save(notification);

        User result = notificationRepository.findUserByNotificationId(notification.getId());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }


    @Test
    @DisplayName("Should not return user successfully")
    void findUserByNotificationIdCase2() {
        long nonExistentNotificationId = 9999L;

        User user = notificationRepository.findUserByNotificationId(nonExistentNotificationId);

        assertNull(user);
    }



}
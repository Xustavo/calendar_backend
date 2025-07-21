package com.calendar.CalendarApplication.services;

import com.calendar.CalendarApplication.entity.Event;
import com.calendar.CalendarApplication.entity.Notification;
import com.calendar.CalendarApplication.entity.User;
import com.calendar.CalendarApplication.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @Test
    @DisplayName("Should act correctly when receiving the data")
    void newUserNotification() {
        User user = new User();
        Notification expectedNotification = new Notification();
        expectedNotification.setTitle("Bem Vindo à sua lista de Eventos e Tarefas");
        expectedNotification.setDescription("Aqui você pode salvar seus eventos e tarefas que você tem marcados para servir de lembrete!");
        expectedNotification.setUserId(user);
        expectedNotification.setHasSeen(false);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notificationToSave = invocation.getArgument(0);
            notificationToSave.setId(1L);
            return notificationToSave;
        });

        Notification result = notificationService.newUserNotification(user);

        assertNotNull(result);
        assertEquals("Bem Vindo à sua lista de Eventos e Tarefas", result.getTitle());
        assertEquals("Aqui você pode salvar seus eventos e tarefas que você tem marcados para servir de lembrete!", result.getDescription());
        assertEquals(user, result.getUser());
        assertFalse(result.getHasSeen());
        assertEquals(1L, result.getId());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should create notification correctly")
    void newEventNotification() {

        User user = new User();
        user.setId(1L);
        user.setUsername("gustavo");

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Reunião Importante");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification toSave = invocation.getArgument(0);
            toSave.setId(100L);
            return toSave;
        });

        Notification result = notificationService.newEventNotification(user, event);

        assertNotNull(result);
        assertEquals("Seu evento Reunião Importante foi salvo com sucesso!", result.getTitle());
        assertEquals("Seu evento Reunião Importante foi salvo com sucesso, notificaremos você quando estiver próximo da data!", result.getDescription());
        assertEquals(user, result.getUser());
        assertEquals(event, result.getEvent());
        assertFalse(result.getHasSeen());
        assertEquals(100L, result.getId());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
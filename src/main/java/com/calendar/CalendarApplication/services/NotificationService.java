package com.calendar.CalendarApplication.services;

import com.calendar.CalendarApplication.dtos.notification.NotificationResponseDto;
import com.calendar.CalendarApplication.dtos.notification.NotificationToUpdateDto;
import com.calendar.CalendarApplication.entity.Event;
import com.calendar.CalendarApplication.entity.Notification;
import com.calendar.CalendarApplication.entity.User;
import com.calendar.CalendarApplication.interfaces.notification.NotificationServiceInterface;
import com.calendar.CalendarApplication.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationService implements NotificationServiceInterface {

    private NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Notification newUserNotification(User user){
        Notification notification = new Notification();
        notification.setTitle("Bem Vindo à sua lista de Eventos e Tarefas");
        notification.setDescription("Aqui você pode salvar seus eventos e tarefas que você tem marcados para servir de lembrete!");
        notification.setUserId(user);
        notification.setHasSeen(false);

        var savedNotification = notificationRepository.save(notification);

        return savedNotification;
    };

    public Notification newEventNotification(User user, Event event){
        Notification notification = new Notification();
        notification.setTitle("Seu evento " + event.getTitle() + " foi salvo com sucesso!");
        notification.setDescription("Seu evento " + event.getTitle() + " foi salvo com sucesso, notificaremos você quando estiver próximo da data!");
        notification.setUserId(user);
        notification.setEvent(event);
        notification.setHasSeen(false);

        var savedNotification = notificationRepository.save(notification);

        return savedNotification;
    };

    public Notification updateNotification(NotificationToUpdateDto notification){
        var savedNotification = notificationRepository.findById(notification.id())
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        savedNotification.setTitle(notification.title());
        savedNotification.setDescription(notification.description());
        savedNotification.setHasSeen(notification.hasSeen());

        return notificationRepository.save(savedNotification);
    }

    //notification for all users
    public void sendNotificationToClients(Notification notification) {
        var dto = new NotificationResponseDto(
                notification.getTitle(),
                notification.getDescription(),
                notification.getHasSeen()
        );

        messagingTemplate.convertAndSend("/topic/notifications", dto);
    }

    //notification to specific user
    public void sendNotificationToUser(Notification notification, Optional<User> user) {
        var dto = new NotificationResponseDto(
                notification.getTitle(),
                notification.getDescription(),
                notification.getHasSeen()
        );

        messagingTemplate.convertAndSendToUser(user.get().getUsername(), "/queue/notifications", dto);
    }



    public void sendDueDateNotification(Event event, int hourTo, Optional<User> user) {
        String description;

        if (hourTo == 1) {
            description = "Seu evento \"" + event.getTitle() + "\" está previsto para a próxima hora.";
        } else {
            description = "Seu evento \"" + event.getTitle() + "\" está previsto para daqui a um dia.";
        }

        Notification notification = new Notification();
        notification.setTitle("Seu evento \"" + event.getTitle() + "\" está próximo!");
        notification.setDescription(description);
        notification.setHasSeen(false);

        sendNotificationToUser(notification, user);
    }

}

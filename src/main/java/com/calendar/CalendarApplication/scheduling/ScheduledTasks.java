package com.calendar.CalendarApplication.scheduling;

import com.calendar.CalendarApplication.entity.Event;
import com.calendar.CalendarApplication.entity.User;
import com.calendar.CalendarApplication.services.EventService;
import com.calendar.CalendarApplication.services.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {

    private static final Logger logger = Logger.getLogger(ScheduledTasks.class.getName());

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final EventService eventService;
    private final NotificationService notificationService;

    public ScheduledTasks(EventService eventService, NotificationService notificationService) {
        this.eventService = eventService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 86400000)
    public void dailyDueDateTask() {
        processDueDateTask(24);
    }

    @Scheduled(fixedRate = 3600000)
    public void hourlyDueDateTask() {
        processDueDateTask(1);
    }

    void processDueDateTask(int hoursAhead) {
        logger.info("Verificando eventos para as próximas " + hoursAhead + " horas: " + dateFormat.format(new Date()));

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, hoursAhead);
        Date futureTime = calendar.getTime();

        List<Event> upcomingEvents = eventService.getNearestEvents(now, futureTime);

        Map<User, List<Event>> eventosPorUsuario = upcomingEvents.stream()
                .collect(Collectors.groupingBy(Event::getUser));

        eventosPorUsuario.forEach((user, eventos) -> {
            eventos.forEach(event -> notificationService.sendDueDateNotification(event, hoursAhead, Optional.ofNullable(user)));
        });
    }


}

package com.calendar.CalendarApplication.entity;

import jakarta.persistence.*;

import javax.swing.text.html.Option;
import java.util.Date;
import java.util.Optional;

@Entity
@Table(name = "events")
public class Event {

    public enum EventType {COMPROMISE, TASK}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(referencedColumnName = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "is_completed")
    private boolean isCompleted;

    public Event(User user, EventType eventType , String title, String description, Date date) {
        this.user = user;
        this.eventType = eventType;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public Event() {}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(long l) {
        this.id = (int) l;
    }
}

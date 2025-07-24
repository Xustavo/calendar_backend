package com.calendar.CalendarApplication.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @ManyToOne(targetEntity = Event.class)
    @JoinColumn(referencedColumnName = "id")
    private Event event;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(referencedColumnName = "id")
    private User user;

    @Column(name = "has_seen", nullable = false)
    private boolean hasSeen;

    public Notification(String title, String description, Event eventId, User userId, boolean hasSeen) {
        this.title = title;
        this.description = description;
        this.event = eventId;
        this.user = userId;
        this.hasSeen = hasSeen;
    }

    public Notification() {}

    public long getId(){
        return id;
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

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event eventId) {
        this.event = eventId;
    }

    public User getUser() {
        return user;
    }

    public void setUserId(User userId) {
        this.user = userId;
    }

    public boolean getHasSeen() {
        return hasSeen;
    }

    public void setHasSeen(boolean hasSeen) {
        this.hasSeen = hasSeen;
    }

    public void setId(long l) {
        this.id = l;
    }
}

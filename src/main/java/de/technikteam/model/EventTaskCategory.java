package de.technikteam.model;

import java.util.ArrayList;
import java.util.List;

public class EventTaskCategory {
    private int id;
    private int eventId;
    private String name;
    private int displayOrder;
    private List<EventTask> tasks = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public List<EventTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<EventTask> tasks) {
        this.tasks = tasks;
    }
}
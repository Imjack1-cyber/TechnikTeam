package de.technikteam.model;

import java.time.LocalDateTime;

public class SchedulingSlot {
    private int id;
    private int responseId;
    private LocalDateTime slotStartTime;
    private LocalDateTime slotEndTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResponseId() {
        return responseId;
    }

    public void setResponseId(int responseId) {
        this.responseId = responseId;
    }

    public LocalDateTime getSlotStartTime() {
        return slotStartTime;
    }

    public void setSlotStartTime(LocalDateTime slotStartTime) {
        this.slotStartTime = slotStartTime;
    }

    public LocalDateTime getSlotEndTime() {
        return slotEndTime;
    }

    public void setSlotEndTime(LocalDateTime slotEndTime) {
        this.slotEndTime = slotEndTime;
    }
}
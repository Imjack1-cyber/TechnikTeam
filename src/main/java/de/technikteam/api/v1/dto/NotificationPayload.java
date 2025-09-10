package de.technikteam.api.v1.dto;

import java.util.List;

/**
 * A DTO representing a structured notification payload for both SSE and FCM.
 */
public class NotificationPayload {
    private String title;
    private String description;
    private String level; // Informational, Important, Warning
    private String url;
    private String androidImportance; // DEFAULT, HIGH
    private String androidChannelId; // e.g., downloads, reminders
    private List<Integer> progress; // [max, current]
    private boolean isSilent;

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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAndroidImportance() {
        return androidImportance;
    }

    public void setAndroidImportance(String androidImportance) {
        this.androidImportance = androidImportance;
    }

    public String getAndroidChannelId() {
        return androidChannelId;
    }

    public void setAndroidChannelId(String androidChannelId) {
        this.androidChannelId = androidChannelId;
    }

    public List<Integer> getProgress() {
        return progress;
    }

    public void setProgress(List<Integer> progress) {
        this.progress = progress;
    }

    public boolean isSilent() {
        return isSilent;
    }

    public void setSilent(boolean silent) {
        isSilent = silent;
    }
}
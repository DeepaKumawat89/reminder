package com.example.reminder2;

public class Reminder {
    private int id;
    private String title;
    private String description;
    private long timeInMillis;

    public Reminder(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public Reminder(int id, String title, String description, long timeInMillis) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timeInMillis = timeInMillis;
    }

    public Reminder(String title, String description, long timeInMillis) {
        this.id = 0; // Will be set by the database
        this.title = title;
        this.description = description;
        this.timeInMillis = timeInMillis;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }
}
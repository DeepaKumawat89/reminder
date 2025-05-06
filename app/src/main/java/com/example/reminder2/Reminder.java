package com.example.reminder2;


import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

public class Reminder implements Serializable {
    private String id;
    private String title;
    private String description;
    private long dateTimeInMillis;

    public Reminder() {
        // Generate a unique ID for each reminder
        this.id = UUID.randomUUID().toString();
    }

    public Reminder(String title, String description, long dateTimeInMillis) {
        this();
        this.title = title;
        this.description = description;
        this.dateTimeInMillis = dateTimeInMillis;
    }

    public String getId() {
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

    public long getDateTimeInMillis() {
        return dateTimeInMillis;
    }

    public void setDateTimeInMillis(long dateTimeInMillis) {
        this.dateTimeInMillis = dateTimeInMillis;
    }

    // Helper method to check if reminder is on a specific date
    public boolean isOnDate(Calendar date) {
        Calendar reminderDate = Calendar.getInstance();
        reminderDate.setTimeInMillis(dateTimeInMillis);

        return reminderDate.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                reminderDate.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                reminderDate.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH);
    }
}

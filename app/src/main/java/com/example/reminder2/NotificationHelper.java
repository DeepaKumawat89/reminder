package com.example.reminder2;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;

public class NotificationHelper {

    private static final String CHANNEL_ID = "reminder_channel";
    private static final String CHANNEL_NAME = "Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for upcoming reminders";

    private final Context context;
    private final AlarmManager alarmManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void scheduleReminder(Reminder reminder) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("title", reminder.getTitle());
        intent.putExtra("description", reminder.getDescription());

        // Create a unique request code based on the reminder ID
        int requestCode = reminder.getId();

        // Create the pending intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminder.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    public void cancelReminderNotification(int reminderId) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Cancel the pending intent
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public void scheduleAllReminders() {
        // Get all future reminders
        ReminderDatabaseHelper dbHelper = ReminderDatabaseHelper.getInstance(context);
        List<Reminder> reminders = dbHelper.getAllReminders();

        long currentTime = System.currentTimeMillis();

        for (Reminder reminder : reminders) {
            // Only schedule future reminders
            if (reminder.getTimeInMillis() > currentTime) {
                scheduleReminder(reminder);
            }
        }
    }
}
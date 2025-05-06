package com.example.reminder2;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        int notificationId = intent.getIntExtra("notificationId", 0);

        // Show the notification
        NotificationHelper notificationHelper = new NotificationHelper(context);
        Reminder reminder=new Reminder(title,description,notificationId);
        notificationHelper.scheduleReminder(reminder);
    }
}
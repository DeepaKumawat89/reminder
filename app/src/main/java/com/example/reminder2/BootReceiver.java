package com.example.reminder2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Reschedule all pending alarms
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.scheduleAllReminders();
        }
    }
}
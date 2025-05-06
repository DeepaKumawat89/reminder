package com.example.reminder2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_REMINDER = 1001;
    private static final int PERMISSION_REQUEST_CODE = 123;

    private CalendarView calendarView;
    private RecyclerView recyclerViewReminders;
    private ReminderAdapter reminderAdapter;
    private FloatingActionButton fabAddReminder;

    private ReminderDatabaseHelper dbHelper;
    private Calendar selectedDate;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        calendarView = findViewById(R.id.calendarView);
        recyclerViewReminders = findViewById(R.id.recyclerViewReminders);
        fabAddReminder = findViewById(R.id.fabAddReminder);

        // Initialize database helper
        dbHelper = ReminderDatabaseHelper.getInstance(this);

        // Initialize notification helper
        notificationHelper = new NotificationHelper(this);

        // Initialize the selected date to today
        selectedDate = Calendar.getInstance();
        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);

        // Set up RecyclerView
        recyclerViewReminders.setLayoutManager(new LinearLayoutManager(this));
        reminderAdapter = new ReminderAdapter(this, new ArrayList<>());
        recyclerViewReminders.setAdapter(reminderAdapter);

        // Set up calendar view
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            loadRemindersForSelectedDate();
        });

        // Set up FAB
        fabAddReminder.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddReminderActivity.class);
            intent.putExtra("selected_date", selectedDate.getTimeInMillis());
            startActivityForResult(intent, REQUEST_CODE_ADD_REMINDER);
        });

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }

        // Load reminders for the selected date (today)
        loadRemindersForSelectedDate();

        // Highlight dates with reminders
        highlightDatesWithReminders();

        // Schedule notifications for all pending reminders
        notificationHelper.scheduleAllReminders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload reminders when returning to the activity
        loadRemindersForSelectedDate();
        highlightDatesWithReminders();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_REMINDER && resultCode == RESULT_OK) {
            loadRemindersForSelectedDate();
            highlightDatesWithReminders();
            Toast.makeText(this, "Reminder added successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRemindersForSelectedDate() {
        List<Reminder> reminders = dbHelper.getRemindersForDate(selectedDate);
        reminderAdapter.updateReminders(reminders);
    }

    private void highlightDatesWithReminders() {
        // This method would ideally mark dates with reminders directly on the calendar view
        // Unfortunately, the default CalendarView doesn't support this functionality easily
        // For a production app, consider using a third-party calendar library
        // Instead, we'll just reload reminders when a date is selected

        // In a real-world application, you could use a calendar library like:
        // - Material CalendarView
        // - Customized CalendarView
        // - Custom calendar implementation
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, schedule notifications
                notificationHelper.scheduleAllReminders();
            } else {
                Toast.makeText(this, "Notification permission denied. Reminders won't show notifications.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
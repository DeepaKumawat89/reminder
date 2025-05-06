package com.example.reminder2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ReminderAdapter.OnReminderDeleteListener, OnDateSelectedListener {

    private static final int REQUEST_CODE_ADD_REMINDER = 1001;
    private static final int PERMISSION_REQUEST_CODE = 123;

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerViewReminders;
    private ReminderAdapter reminderAdapter;
    private FloatingActionButton fabAddReminder;
    private TextView textViewNoReminders;

    private ReminderDatabaseHelper dbHelper;
    private Calendar selectedDate;
    private NotificationHelper notificationHelper;
    private EventDecorator eventDecorator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        calendarView = findViewById(R.id.calendarView);
        recyclerViewReminders = findViewById(R.id.recyclerViewReminders);
        fabAddReminder = findViewById(R.id.fabAddReminder);
        textViewNoReminders = findViewById(R.id.textViewNoReminders);

        // Initialize database helper
        dbHelper = ReminderDatabaseHelper.getInstance(this);

        // Clean up past reminders
        int removedReminders = dbHelper.cleanupPastReminders();
        if (removedReminders > 0) {
            Toast.makeText(this, "Cleaned up " + removedReminders + " past reminders", Toast.LENGTH_SHORT).show();
        }

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
        calendarView.setOnDateChangedListener(this);
        calendarView.setSelectedDate(CalendarDay.today());

        // Initialize the event decorator
        eventDecorator = new EventDecorator(Color.RED, new HashSet<>());
        calendarView.addDecorator(eventDecorator);

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
        updateCalendarWithReminderDates();

        // Schedule notifications for all pending reminders
        notificationHelper.scheduleAllReminders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload reminders when returning to the activity
        loadRemindersForSelectedDate();
        updateCalendarWithReminderDates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_REMINDER && resultCode == RESULT_OK) {
            loadRemindersForSelectedDate();
            updateCalendarWithReminderDates();
            Toast.makeText(this, "Reminder added successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRemindersForSelectedDate() {
        List<Reminder> reminders = dbHelper.getRemindersForDate(selectedDate);
        reminderAdapter.updateReminders(reminders);

        // Show/hide "No reminders" text
        if (reminders.isEmpty()) {
            textViewNoReminders.setVisibility(android.view.View.VISIBLE);
            recyclerViewReminders.setVisibility(android.view.View.GONE);
        } else {
            textViewNoReminders.setVisibility(android.view.View.GONE);
            recyclerViewReminders.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void updateCalendarWithReminderDates() {
        // Get all dates with reminders
        List<Calendar> datesWithReminders = dbHelper.getDatesWithReminders();
        Set<CalendarDay> calendarDays = new HashSet<>();

        for (Calendar date : datesWithReminders) {
            calendarDays.add(CalendarDay.from(
                    date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH),
                    date.get(Calendar.DAY_OF_MONTH)));
        }

        // Update decorator with new dates
        eventDecorator.setDates(calendarDays);
        calendarView.invalidateDecorators();
    }

    @Override
    public void onReminderDelete(Reminder reminder) {
        // Delete reminder from database
        boolean success = dbHelper.deleteReminder(reminder.getId());
        if (success) {
            // Cancel notification
            notificationHelper.cancelReminderNotification(reminder.getId());

            // Reload UI
            loadRemindersForSelectedDate();
            updateCalendarWithReminderDates();
            Toast.makeText(this, "Reminder deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to delete reminder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        selectedDate.set(date.getYear(), date.getMonth(), date.getDay());
        loadRemindersForSelectedDate();
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
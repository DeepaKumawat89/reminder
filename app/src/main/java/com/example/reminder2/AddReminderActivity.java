package com.example.reminder2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddReminderActivity extends AppCompatActivity {

    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private Button buttonPickDate;
    private Button buttonPickTime;
    private TextView textViewSelectedDate;
    private TextView textViewSelectedTime;
    private Button buttonSubmit;

    private Calendar selectedDateTime;
    private ReminderDatabaseHelper dbHelper;
    private NotificationHelper notificationHelper;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        // Set up action bar with back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Reminder");
        }

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonPickDate = findViewById(R.id.buttonPickDate);
        buttonPickTime = findViewById(R.id.buttonPickTime);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        textViewSelectedTime = findViewById(R.id.textViewSelectedTime);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        // Initialize helpers
        dbHelper = ReminderDatabaseHelper.getInstance(this);
        notificationHelper = new NotificationHelper(this);

        // Format for displaying date and time
        dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        // Initialize date time
        selectedDateTime = Calendar.getInstance();

        // If there's a date passed from the main activity
        long selectedDateMillis = getIntent().getLongExtra("selected_date", -1);
        if (selectedDateMillis != -1) {
            selectedDateTime.setTimeInMillis(selectedDateMillis);
        }

        // Set time to current time + 1 hour by default
        selectedDateTime.add(Calendar.HOUR_OF_DAY, 1);

        // Update displayed date and time
        updateDateDisplay();
        updateTimeDisplay();

        // Set up click listeners
        buttonPickDate.setOnClickListener(v -> showDatePicker());
        buttonPickTime.setOnClickListener(v -> showTimePicker());
        buttonSubmit.setOnClickListener(v -> saveReminder());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);
                    updateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDateDisplay() {
        textViewSelectedDate.setText(dateFormat.format(selectedDateTime.getTime()));
    }

    private void updateTimeDisplay() {
        textViewSelectedTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void saveReminder() {
        String title = editTextTitle.getText() != null ? editTextTitle.getText().toString().trim() : "";
        String description = editTextDescription.getText() != null ? editTextDescription.getText().toString().trim() : "";

        // Validate input
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the selected time is in the past
        if (selectedDateTime.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create reminder object
        Reminder reminder = new Reminder(title, description, selectedDateTime.getTimeInMillis());

        // Save to database
        long result = dbHelper.addReminder(reminder);

        if (result != -1) {
            // Schedule notification
            notificationHelper.scheduleNotification(reminder);

            // Return to main activity
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to save reminder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
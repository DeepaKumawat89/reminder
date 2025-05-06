package com.example.reminder2;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddReminderActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextDescription;
    private TextView textViewSelectedDate;
    private Button buttonSelectTime;
    private Button buttonSave;

    private Calendar selectedDateTime;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add New Reminder");
        }

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        buttonSelectTime = findViewById(R.id.buttonSelectTime);
        buttonSave = findViewById(R.id.buttonSave);

        // Get the selected date from the intent
        long selectedDateMillis = getIntent().getLongExtra("selected_date", System.currentTimeMillis());
        selectedDateTime = Calendar.getInstance();
        selectedDateTime.setTimeInMillis(selectedDateMillis);

        // Set the current time
        Calendar now = Calendar.getInstance();
        selectedDateTime.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        selectedDateTime.set(Calendar.MINUTE, now.get(Calendar.MINUTE));

        // Update the date text
        updateDateTimeText();

        // Set up the time picker button
        buttonSelectTime.setOnClickListener(v -> showTimePickerDialog());

        // Set up the save button
        buttonSave.setOnClickListener(v -> saveReminder());
    }

    private void updateDateTimeText() {
        String dateStr = dateFormat.format(selectedDateTime.getTime());
        String timeStr = timeFormat.format(selectedDateTime.getTime());
        textViewSelectedDate.setText(String.format("Date: %s\nTime: %s", dateStr, timeStr));
        buttonSelectTime.setText(timeStr);
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeText();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void saveReminder() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // Validate input
        if (title.isEmpty()) {
            editTextTitle.setError("Title is required");
            return;
        }

        // Create a new reminder
        Reminder reminder = new Reminder(title, description, selectedDateTime.getTimeInMillis());

        // Save to database
        ReminderDatabaseHelper dbHelper = ReminderDatabaseHelper.getInstance(this);
        long id = dbHelper.addReminder(reminder);

        if (id > 0) {
            // Set the ID for the reminder (needed for notifications)
            reminder.setId((int) id);

            // Schedule notification
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.scheduleReminder(reminder);

            // Return result and close activity
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
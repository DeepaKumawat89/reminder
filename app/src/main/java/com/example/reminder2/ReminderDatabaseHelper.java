package com.example.reminder2;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReminderDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "reminders.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_REMINDERS = "reminders";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATETIME = "datetime";

    // Singleton instance
    private static ReminderDatabaseHelper instance;

    public static synchronized ReminderDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ReminderDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private ReminderDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_REMINDERS_TABLE = "CREATE TABLE " + TABLE_REMINDERS + "("
                + COLUMN_ID + " TEXT PRIMARY KEY,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_DATETIME + " INTEGER" + ")";
        db.execSQL(CREATE_REMINDERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);
    }

    // Add a new reminder
    public long addReminder(Reminder reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, reminder.getId());
        values.put(COLUMN_TITLE, reminder.getTitle());
        values.put(COLUMN_DESCRIPTION, reminder.getDescription());
        values.put(COLUMN_DATETIME, reminder.getDateTimeInMillis());

        long result = db.insert(TABLE_REMINDERS, null, values);
        db.close();
        return result;
    }

    // Get all reminders
    public List<Reminder> getAllReminders() {
        List<Reminder> reminderList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_REMINDERS + " ORDER BY " + COLUMN_DATETIME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Reminder reminder = new Reminder();
                reminder.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                reminder.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                reminder.setDateTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATETIME)));
                reminderList.add(reminder);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return reminderList;
    }

    // Get reminders for a specific date
    public List<Reminder> getRemindersForDate(Calendar date) {
        List<Reminder> allReminders = getAllReminders();
        List<Reminder> dateReminders = new ArrayList<>();

        for (Reminder reminder : allReminders) {
            if (reminder.isOnDate(date)) {
                dateReminders.add(reminder);
            }
        }

        return dateReminders;
    }

    // Get all dates that have reminders
    public List<Calendar> getDatesWithReminders() {
        List<Reminder> allReminders = getAllReminders();
        List<Calendar> dates = new ArrayList<>();

        for (Reminder reminder : allReminders) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(reminder.getDateTimeInMillis());

            // Reset time part
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Check if this date is already in our list
            boolean dateExists = false;
            for (Calendar existingDate : dates) {
                if (existingDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        existingDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        existingDate.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)) {
                    dateExists = true;
                    break;
                }
            }

            if (!dateExists) {
                dates.add(calendar);
            }
        }

        return dates;
    }

    // Get pending reminders (reminders that haven't occurred yet)
    public List<Reminder> getPendingReminders() {
        List<Reminder> pendingReminders = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        String selectQuery = "SELECT * FROM " + TABLE_REMINDERS +
                " WHERE " + COLUMN_DATETIME + " > " + currentTime +
                " ORDER BY " + COLUMN_DATETIME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Reminder reminder = new Reminder();
                reminder.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                reminder.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                reminder.setDateTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATETIME)));
                pendingReminders.add(reminder);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return pendingReminders;
    }
}
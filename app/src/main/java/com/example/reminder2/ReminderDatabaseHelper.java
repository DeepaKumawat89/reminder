package com.example.reminder2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReminderDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ReminderApp.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_REMINDERS = "reminders";

    // Table columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_TIME_MILLIS = "time_millis";

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
        String createTableQuery = "CREATE TABLE " + TABLE_REMINDERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_TIME_MILLIS + " INTEGER)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);
    }

    public long addReminder(Reminder reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, reminder.getTitle());
        values.put(COLUMN_DESCRIPTION, reminder.getDescription());
        values.put(COLUMN_TIME_MILLIS, reminder.getTimeInMillis());

        long id = db.insert(TABLE_REMINDERS, null, values);
        db.close();
        return id;
    }

    public List<Reminder> getAllReminders() {
        List<Reminder> reminderList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_REMINDERS + " ORDER BY " + COLUMN_TIME_MILLIS + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                long timeMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME_MILLIS));

                Reminder reminder = new Reminder(id, title, description, timeMillis);
                reminderList.add(reminder);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return reminderList;
    }

    public List<Reminder> getRemindersForDate(Calendar date) {
        List<Reminder> reminderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Calculate start and end of the day
        Calendar startOfDay = (Calendar) date.clone();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = (Calendar) startOfDay.clone();
        endOfDay.add(Calendar.DAY_OF_MONTH, 1);

        String selectQuery = "SELECT * FROM " + TABLE_REMINDERS +
                " WHERE " + COLUMN_TIME_MILLIS + " >= ? AND " + COLUMN_TIME_MILLIS + " < ?" +
                " ORDER BY " + COLUMN_TIME_MILLIS + " ASC";

        Cursor cursor = db.rawQuery(selectQuery,
                new String[]{String.valueOf(startOfDay.getTimeInMillis()),
                        String.valueOf(endOfDay.getTimeInMillis())});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                long timeMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME_MILLIS));

                Reminder reminder = new Reminder(id, title, description, timeMillis);
                reminderList.add(reminder);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return reminderList;
    }

    public boolean deleteReminder(int reminderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_REMINDERS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(reminderId)});
        db.close();
        return rowsAffected > 0;
    }

    public int cleanupPastReminders() {
        SQLiteDatabase db = this.getWritableDatabase();
        long currentTimeMillis = System.currentTimeMillis();

        int rowsAffected = db.delete(TABLE_REMINDERS,
                COLUMN_TIME_MILLIS + " < ?",
                new String[]{String.valueOf(currentTimeMillis)});

        db.close();
        return rowsAffected;
    }

    public List<Calendar> getDatesWithReminders() {
        List<Calendar> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT DISTINCT " + COLUMN_TIME_MILLIS + " FROM " + TABLE_REMINDERS;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                long timeMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME_MILLIS));
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timeMillis);
                // Normalize to start of day
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                // Check if this date is already in our list
                boolean dateExists = false;
                for (Calendar existingDate : dates) {
                    if (existingDate.getTimeInMillis() == calendar.getTimeInMillis()) {
                        dateExists = true;
                        break;
                    }
                }

                if (!dateExists) {
                    dates.add(calendar);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return dates;
    }
}
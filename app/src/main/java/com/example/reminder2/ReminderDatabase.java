//package com.example.reminder2;
//
//import android.content.Context;
//import androidx.room.Database;
//import androidx.room.Room;
//import androidx.room.RoomDatabase;
//
//@Database(entities = {Reminder.class}, version = 1, exportSchema = false)
//public abstract class ReminderDatabase extends RoomDatabase {
//    private static ReminderDatabase instance;
//
//    public abstract ReminderDao reminderDao();
//
//    public static synchronized ReminderDatabase getInstance(Context context) {
//        if (instance == null) {
//            instance = Room.databaseBuilder(context.getApplicationContext(),
//                            ReminderDatabase.class, "reminder_db")
//                    .allowMainThreadQueries() // For demo only
//                    .fallbackToDestructiveMigration()
//                    .build();
//        }
//        return instance;
//    }
//}

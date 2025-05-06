package com.example.reminder2;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ViewAllRemindersActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> reminderList;
    ArrayAdapter<String> adapter;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_reminders);

        listView = findViewById(R.id.listView);
        reminderList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reminderList);
        listView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadReminders();
    }

    private void loadReminders() {
        String userEmail = auth.getCurrentUser().getEmail();

        firestore.collection("users")
                .document(userEmail)
                .collection("reminder")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reminderList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(ViewAllRemindersActivity.this, "No reminders found!", Toast.LENGTH_SHORT).show();
                    }
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getString("title");
                        String date = document.getString("date");
                        String time = document.getString("time");

                        // Print data to check what we're getting
                        Log.d("ViewAllReminders", "Fetched reminder: " + title + " " + date + " " + time);

                        reminderList.add(title + "\n" + date + " " + time);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewAllRemindersActivity.this, "Failed to load reminders", Toast.LENGTH_SHORT).show();
                });
    }

}

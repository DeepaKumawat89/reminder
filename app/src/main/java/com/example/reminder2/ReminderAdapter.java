package com.example.reminder2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private final Context context;
    private List<Reminder> reminders;
    private final SimpleDateFormat timeFormat;
    private OnReminderDeleteListener deleteListener;

    public interface OnReminderDeleteListener {
        void onReminderDelete(Reminder reminder);
    }

    public ReminderAdapter(Context context, List<Reminder> reminders) {
        this.context = context;
        this.reminders = reminders;
        this.timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        // Set delete listener if context implements the interface
        if (context instanceof OnReminderDeleteListener) {
            this.deleteListener = (OnReminderDeleteListener) context;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);

        holder.textViewTitle.setText(reminder.getTitle());
        holder.textViewDescription.setText(reminder.getDescription());

        Calendar reminderTime = Calendar.getInstance();
        reminderTime.setTimeInMillis(reminder.getTimeInMillis());
        holder.textViewTime.setText(timeFormat.format(reminderTime.getTime()));

        // Set delete click listener
        holder.imageViewDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onReminderDelete(reminder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public void updateReminders(List<Reminder> newReminders) {
        this.reminders = newReminders;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewTime;
        ImageView imageViewDelete;

        ViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            imageViewDelete = itemView.findViewById(R.id.imageViewDelete);
        }
    }
}
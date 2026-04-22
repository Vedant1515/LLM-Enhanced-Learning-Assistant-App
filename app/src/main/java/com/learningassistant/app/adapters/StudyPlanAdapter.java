package com.learningassistant.app.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.learningassistant.app.R;
import com.learningassistant.app.models.StudyDay;
import java.util.List;

public class StudyPlanAdapter extends RecyclerView.Adapter<StudyPlanAdapter.StudyDayViewHolder> {

    private final List<StudyDay> studyDays;

    public StudyPlanAdapter(List<StudyDay> studyDays) {
        this.studyDays = studyDays;
    }

    @NonNull
    @Override
    public StudyDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_day, parent, false);
        return new StudyDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudyDayViewHolder holder, int position) {
        StudyDay day = studyDays.get(position);
        holder.tvDayLabel.setText(day.getDayLabel());
        holder.tvDescription.setText(day.getTaskDescription());

        if (day.isToday()) {
            holder.tvDayLabel.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAccentYellow));
            holder.tvDayLabel.setTypeface(null, Typeface.BOLD);
            holder.tvDescription.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorTextPrimary));
            holder.tvDescription.setTypeface(null, Typeface.BOLD);
        } else {
            holder.tvDayLabel.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary));
            holder.tvDayLabel.setTypeface(null, Typeface.NORMAL);
            holder.tvDescription.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorTextMuted));
            holder.tvDescription.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public int getItemCount() { return studyDays.size(); }

    static class StudyDayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayLabel, tvDescription;

        StudyDayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayLabel = itemView.findViewById(R.id.tvDayLabel);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
        }
    }
}

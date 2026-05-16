package com.learningassistant.app.adapters;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.learningassistant.app.R;
import com.learningassistant.app.models.QuizResult;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    private final List<QuizResult> results;
    private final List<Boolean> expandedStates;

    public HistoryAdapter(List<QuizResult> results) {
        this.results = results;
        this.expandedStates = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            expandedStates.add(false);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_entry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        QuizResult result = results.get(position);
        boolean isExpanded = expandedStates.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy | HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(new Date(result.getTimestamp()));
        holder.tvTimestamp.setText(formattedTime + " | Topic: " + result.getTopic());
        holder.tvScore.setText(result.getCorrectCount() + "/" + result.getTotalQuestions());

        int maxVisible = isExpanded ? 3 : 1;
        HistoryQuestionAdapter questionAdapter = new HistoryQuestionAdapter(
                result.getQuestions() != null ? result.getQuestions() : new ArrayList<>(),
                maxVisible);
        holder.rvQuestions.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvQuestions.setNestedScrollingEnabled(false);
        holder.rvQuestions.setAdapter(questionAdapter);

        holder.ivChevron.setRotation(isExpanded ? 180f : 0f);

        holder.ivChevron.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;
            boolean expanded = !expandedStates.get(pos);
            expandedStates.set(pos, expanded);

            float fromDeg = expanded ? 0f : 180f;
            float toDeg = expanded ? 180f : 0f;
            ObjectAnimator anim = ObjectAnimator.ofFloat(holder.ivChevron, "rotation", fromDeg, toDeg);
            anim.setDuration(300);
            anim.start();

            questionAdapter.setMaxVisible(expanded ? 3 : 1);
            questionAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTimestamp, tvScore;
        RecyclerView rvQuestions;
        ImageView ivChevron;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvScore = itemView.findViewById(R.id.tvScore);
            rvQuestions = itemView.findViewById(R.id.rvQuestions);
            ivChevron = itemView.findViewById(R.id.ivChevron);
        }
    }
}

package com.learningassistant.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.learningassistant.app.R;
import com.learningassistant.app.models.QuizQuestion;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private final List<QuizQuestion> questions;
    private final Map<Integer, String> selectedAnswers = new HashMap<>();

    public QuizAdapter(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_question, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuizQuestion q = questions.get(position);
        holder.tvNumber.setText("Q" + (position + 1) + ".");
        holder.tvQuestion.setText(q.getQuestion());

        Button[] optionBtns = {holder.btnOption0, holder.btnOption1, holder.btnOption2, holder.btnOption3};
        List<String> options = q.getOptions();

        for (int i = 0; i < optionBtns.length; i++) {
            if (options != null && i < options.size()) {
                optionBtns[i].setVisibility(View.VISIBLE);
                optionBtns[i].setText(options.get(i));
                optionBtns[i].setEnabled(true);
                final int optionIndex = i;
                final String optionText = options.get(i);

                // Apply currently selected state
                String selected = selectedAnswers.get(position);
                if (optionText.equals(selected)) {
                    applySelected(holder.itemView.getContext(), optionBtns[i]);
                } else {
                    applyUnselected(holder.itemView.getContext(), optionBtns[i]);
                }

                optionBtns[i].setOnClickListener(v -> {
                    selectedAnswers.put(position, optionText);
                    questions.get(position).setUserAnswer(optionText);
                    // Update button states
                    for (int j = 0; j < optionBtns.length; j++) {
                        if (options.get(j).equals(optionText)) {
                            applySelected(v.getContext(), optionBtns[j]);
                        } else {
                            applyUnselected(v.getContext(), optionBtns[j]);
                        }
                    }
                });
            } else {
                optionBtns[i].setVisibility(View.GONE);
            }
        }
    }

    private void applySelected(android.content.Context ctx, Button btn) {
        btn.setBackground(ctx.getDrawable(R.drawable.shape_radio_selected));
        btn.setTextColor(ctx.getColor(R.color.colorPrimary));
    }

    private void applyUnselected(android.content.Context ctx, Button btn) {
        btn.setBackground(ctx.getDrawable(R.drawable.shape_radio_unselected));
        btn.setTextColor(ctx.getColor(R.color.colorTextPrimary));
    }

    @Override
    public int getItemCount() { return questions.size(); }

    public Map<Integer, String> getAnswers() {
        return new HashMap<>(selectedAnswers);
    }

    public boolean hasAnyAnswer() {
        return !selectedAnswers.isEmpty();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvQuestion;
        Button btnOption0, btnOption1, btnOption2, btnOption3;

        QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvQuestionNumber);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            btnOption0 = itemView.findViewById(R.id.btnOption0);
            btnOption1 = itemView.findViewById(R.id.btnOption1);
            btnOption2 = itemView.findViewById(R.id.btnOption2);
            btnOption3 = itemView.findViewById(R.id.btnOption3);
        }
    }
}

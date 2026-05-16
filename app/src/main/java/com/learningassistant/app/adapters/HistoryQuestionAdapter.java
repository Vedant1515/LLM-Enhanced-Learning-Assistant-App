package com.learningassistant.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.learningassistant.app.R;
import com.learningassistant.app.models.QuizQuestion;
import java.util.List;

public class HistoryQuestionAdapter extends RecyclerView.Adapter<HistoryQuestionAdapter.VH> {

    private final List<QuizQuestion> questions;
    private int maxVisible;

    public HistoryQuestionAdapter(List<QuizQuestion> questions, int maxVisible) {
        this.questions = questions;
        this.maxVisible = maxVisible;
    }

    public void setMaxVisible(int maxVisible) {
        this.maxVisible = maxVisible;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_question, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        QuizQuestion q = questions.get(position);
        holder.tvQuestionNumber.setText((position + 1) + ". " + q.getQuestion());

        String userAnswer = q.getUserAnswer();
        String correctAnswer = q.getCorrectAnswer();
        List<String> options = q.getOptions();

        boolean isCorrect = q.isCorrect();

        if (userAnswer != null && !userAnswer.isEmpty()) {
            holder.tvUserAnswer.setText(userAnswer);
            if (isCorrect) {
                holder.dotUserAnswer.setBackgroundResource(R.drawable.shape_dot_green);
                holder.tvUserAnswerLabel.setTextColor(
                        holder.itemView.getContext().getResources().getColor(R.color.colorSuccess));
                holder.tvUserAnswerLabel.setText("Correct!");
                holder.rowCorrectAnswer.setVisibility(View.GONE);
            } else {
                holder.dotUserAnswer.setBackgroundResource(R.drawable.shape_dot_red);
                holder.tvUserAnswerLabel.setTextColor(
                        holder.itemView.getContext().getResources().getColor(R.color.colorError));
                holder.tvUserAnswerLabel.setText("Your Answer");
                holder.rowCorrectAnswer.setVisibility(View.VISIBLE);
                holder.tvCorrectAnswer.setText(correctAnswer != null ? correctAnswer : "");
            }
        } else {
            holder.tvUserAnswer.setText("Not answered");
            holder.rowCorrectAnswer.setVisibility(View.VISIBLE);
            holder.tvCorrectAnswer.setText(correctAnswer != null ? correctAnswer : "");
        }

        String otherOption = null;
        if (options != null) {
            for (String opt : options) {
                if (!opt.equals(userAnswer) && !opt.equals(correctAnswer)) {
                    otherOption = opt;
                    break;
                }
            }
        }
        if (otherOption != null) {
            holder.tvOtherAnswer.setText(otherOption);
            holder.rowOtherAnswer.setVisibility(View.VISIBLE);
        } else {
            holder.rowOtherAnswer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (questions == null) return 0;
        return Math.min(maxVisible, questions.size());
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvQuestionNumber, tvUserAnswer, tvUserAnswerLabel, tvCorrectAnswer, tvOtherAnswer;
        View dotUserAnswer, rowCorrectAnswer, rowOtherAnswer;

        VH(@NonNull View itemView) {
            super(itemView);
            tvQuestionNumber = itemView.findViewById(R.id.tvQuestionNumber);
            tvUserAnswer = itemView.findViewById(R.id.tvUserAnswer);
            tvUserAnswerLabel = itemView.findViewById(R.id.tvUserAnswerLabel);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
            tvOtherAnswer = itemView.findViewById(R.id.tvOtherAnswer);
            dotUserAnswer = itemView.findViewById(R.id.dotUserAnswer);
            rowCorrectAnswer = itemView.findViewById(R.id.rowCorrectAnswer);
            rowOtherAnswer = itemView.findViewById(R.id.rowOtherAnswer);
        }
    }
}

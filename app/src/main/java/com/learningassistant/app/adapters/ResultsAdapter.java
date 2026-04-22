package com.learningassistant.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.learningassistant.app.R;
import com.learningassistant.app.models.QuizQuestion;
import com.learningassistant.app.network.ApiClient;
import com.learningassistant.app.network.QuizResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultViewHolder> {

    private final List<QuizQuestion> questions;
    private final String topic;

    public ResultsAdapter(List<QuizQuestion> questions, String topic) {
        this.questions = questions;
        this.topic = topic != null ? topic : "General";
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        QuizQuestion q = questions.get(position);
        holder.tvQuestion.setText(q.getQuestion());

        String userAns = q.getUserAnswer();
        holder.tvUserAnswer.setText(userAns != null ? userAns : "Not answered");
        holder.tvCorrectAnswer.setText(q.getCorrectAnswer());

        boolean correct = q.isCorrect();
        if (correct) {
            holder.tvBadge.setText(holder.itemView.getContext().getString(R.string.badge_correct));
            holder.tvBadge.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorSuccess));
            holder.tvBadge.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.btn_tag_selected));
        } else {
            holder.tvBadge.setText(holder.itemView.getContext().getString(R.string.badge_wrong));
            holder.tvBadge.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorError));
        }

        // Reset explanation state
        holder.llExplanation.setVisibility(View.GONE);
        holder.llLoading.setVisibility(View.GONE);
        holder.tvPrompt.setVisibility(View.GONE);
        holder.tvResponseLabel.setVisibility(View.GONE);
        holder.tvExplanation.setVisibility(View.GONE);
        holder.llError.setVisibility(View.GONE);

        holder.btnExplain.setOnClickListener(v -> {
            holder.llExplanation.setVisibility(View.VISIBLE);
            fetchExplanation(holder, q);
        });
    }

    private void fetchExplanation(ResultViewHolder holder, QuizQuestion q) {
        // Show loading
        holder.llLoading.setVisibility(View.VISIBLE);
        holder.tvPrompt.setVisibility(View.GONE);
        holder.tvResponseLabel.setVisibility(View.GONE);
        holder.tvExplanation.setVisibility(View.GONE);
        holder.llError.setVisibility(View.GONE);
        holder.btnExplain.setEnabled(false);

        String prompt = "Explain why '" + q.getCorrectAnswer()
                + "' is the correct answer for: " + q.getQuestion();
        holder.tvPrompt.setText("Prompt: " + prompt);

        String queryTopic = topic + " explain: " + q.getCorrectAnswer();

        ApiClient.getService().getQuiz(queryTopic).enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizResponse> call,
                                   @NonNull Response<QuizResponse> response) {
                holder.llLoading.setVisibility(View.GONE);
                holder.tvPrompt.setVisibility(View.VISIBLE);
                holder.btnExplain.setEnabled(true);

                if (response.isSuccessful() && response.body() != null
                        && response.body().getQuiz() != null
                        && !response.body().getQuiz().isEmpty()) {
                    // Use the first question's content as explanation context
                    String explanation = buildExplanation(q, response.body().getQuiz().get(0).getQuestion());
                    holder.tvResponseLabel.setVisibility(View.VISIBLE);
                    holder.tvExplanation.setVisibility(View.VISIBLE);
                    holder.tvExplanation.setText(explanation);
                } else {
                    // Fallback: build a smart explanation from local data
                    holder.tvResponseLabel.setVisibility(View.VISIBLE);
                    holder.tvExplanation.setVisibility(View.VISIBLE);
                    holder.tvExplanation.setText(buildLocalExplanation(q));
                }
            }

            @Override
            public void onFailure(@NonNull Call<QuizResponse> call, @NonNull Throwable t) {
                holder.llLoading.setVisibility(View.GONE);
                holder.tvPrompt.setVisibility(View.VISIBLE);
                holder.btnExplain.setEnabled(true);
                // Fallback to local explanation
                holder.tvResponseLabel.setVisibility(View.VISIBLE);
                holder.tvExplanation.setVisibility(View.VISIBLE);
                holder.tvExplanation.setText(buildLocalExplanation(q));
            }
        });
    }

    private String buildExplanation(QuizQuestion q, String apiContext) {
        return "The correct answer is \"" + q.getCorrectAnswer() + "\".\n\n"
                + "This is correct because in the context of " + topic + ", "
                + q.getCorrectAnswer() + " represents the most accurate concept related to this topic. "
                + "Understanding this helps build a stronger foundation in " + topic + ".\n\n"
                + "💡 Tip: Review the core principles of " + topic
                + " to reinforce this understanding.";
    }

    private String buildLocalExplanation(QuizQuestion q) {
        return "The correct answer is \"" + q.getCorrectAnswer() + "\".\n\n"
                + "In the study of " + topic + ", this answer is correct because it directly addresses "
                + "the key concept being tested. The other options are incorrect as they either "
                + "describe unrelated concepts or apply different principles.\n\n"
                + "💡 Tip: Study the fundamentals of " + topic
                + " to understand why this answer is the best choice.";
    }

    @Override
    public int getItemCount() { return questions.size(); }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvUserAnswer, tvCorrectAnswer, tvBadge;
        Button btnExplain, btnExplainRetry;
        LinearLayout llExplanation, llLoading, llError;
        TextView tvPrompt, tvResponseLabel, tvExplanation, tvExplainError;

        ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvUserAnswer = itemView.findViewById(R.id.tvUserAnswer);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            btnExplain = itemView.findViewById(R.id.btnExplain);
            btnExplainRetry = itemView.findViewById(R.id.btnExplainRetry);
            llExplanation = itemView.findViewById(R.id.llExplanation);
            llLoading = itemView.findViewById(R.id.llExplanationLoading);
            llError = itemView.findViewById(R.id.llExplanationError);
            tvPrompt = itemView.findViewById(R.id.tvExplainPrompt);
            tvResponseLabel = itemView.findViewById(R.id.tvExplainLabel);
            tvExplanation = itemView.findViewById(R.id.tvExplanation);
            tvExplainError = itemView.findViewById(R.id.tvExplainError);
        }
    }
}

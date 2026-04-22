package com.learningassistant.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.cardview.widget.CardView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.gson.Gson;
import com.learningassistant.app.R;
import com.learningassistant.app.models.QuizQuestion;
import com.learningassistant.app.network.ApiClient;
import com.learningassistant.app.network.QuizResponse;
import com.learningassistant.app.utils.AnimationUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizActivity extends AppCompatActivity {

    private String taskTitle, taskTopic, taskDescription;
    private List<QuizQuestion> currentQuestions = new ArrayList<>();

    // Track user's selected answer per question index
    private final Map<Integer, String> selectedAnswers = new HashMap<>();

    // Root views
    private TextView tvTaskTitle, tvQuizTopic, tvQuizDescription;
    private ConstraintLayout layoutLoading, layoutError;
    private ScrollView scrollQuiz;
    private Button btnSubmit, btnRetry, btnGetHint, btnStudyPlan;

    // Question container (questions inflated here directly — no RecyclerView in ScrollView)
    private LinearLayout llQuestionsContainer;

    // AI response card
    private CardView cardAiResponse;
    private LinearLayout llAiLoading, llAiError;
    private TextView tvAiLoadingText, tvAiPrompt, tvAiResponseLabel, tvAiResponse, tvAiErrorMsg;
    private Button btnAiRetry;

    private enum AiMode { HINT, STUDY_PLAN }
    private AiMode lastAiMode = AiMode.HINT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        taskTitle = getIntent().getStringExtra("task_title");
        taskTopic = getIntent().getStringExtra("task_topic");
        taskDescription = getIntent().getStringExtra("task_description");

        if (taskTitle == null) taskTitle = "Generated Task";
        if (taskTopic == null) taskTopic = "General";
        if (taskDescription == null) taskDescription = "Test your knowledge with AI-generated questions.";

        initViews();
        setupListeners();
        loadQuiz();
    }

    private void initViews() {
        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvQuizTopic = findViewById(R.id.tvQuizTopic);
        tvQuizDescription = findViewById(R.id.tvQuizDescription);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutError = findViewById(R.id.layoutError);
        scrollQuiz = findViewById(R.id.scrollQuiz);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnRetry = findViewById(R.id.btnRetry);
        btnGetHint = findViewById(R.id.btnGetHint);
        btnStudyPlan = findViewById(R.id.btnStudyPlan);
        llQuestionsContainer = findViewById(R.id.llQuestionsContainer);
        cardAiResponse = findViewById(R.id.cardAiResponse);
        llAiLoading = findViewById(R.id.llAiLoading);
        llAiError = findViewById(R.id.llAiError);
        tvAiLoadingText = findViewById(R.id.tvAiLoadingText);
        tvAiPrompt = findViewById(R.id.tvAiPrompt);
        tvAiResponseLabel = findViewById(R.id.tvAiResponseLabel);
        tvAiResponse = findViewById(R.id.tvAiResponse);
        tvAiErrorMsg = findViewById(R.id.tvAiErrorMsg);
        btnAiRetry = findViewById(R.id.btnAiRetry);

        tvTaskTitle.setText(taskTitle);
        tvQuizTopic.setText(taskTopic);
        tvQuizDescription.setText(taskDescription);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        btnRetry.setOnClickListener(v -> loadQuiz());

        btnGetHint.setOnClickListener(v -> {
            lastAiMode = AiMode.HINT;
            fetchHint();
        });

        btnStudyPlan.setOnClickListener(v -> {
            lastAiMode = AiMode.STUDY_PLAN;
            fetchStudyPlan();
        });

        btnAiRetry.setOnClickListener(v -> {
            if (lastAiMode == AiMode.HINT) fetchHint();
            else fetchStudyPlan();
        });

        btnSubmit.setOnClickListener(v -> submitQuiz());
    }

    // -----------------------------------------------------------------------
    // Network
    // -----------------------------------------------------------------------
    private void loadQuiz() {
        showLoading();
        ApiClient.getService().getQuiz(taskTopic).enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizResponse> call,
                                   @NonNull Response<QuizResponse> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getQuiz() != null
                        && !response.body().getQuiz().isEmpty()) {
                    currentQuestions.clear();
                    currentQuestions.addAll(response.body().getQuiz());
                    selectedAnswers.clear();
                    inflateQuestions();
                    showQuiz();
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<QuizResponse> call, @NonNull Throwable t) {
                showError();
            }
        });
    }

    // -----------------------------------------------------------------------
    // Inflate questions directly into LinearLayout — avoids RecyclerView
    // height mis-measurement when nested inside ScrollView
    // -----------------------------------------------------------------------
    private void inflateQuestions() {
        llQuestionsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < currentQuestions.size(); i++) {
            QuizQuestion q = currentQuestions.get(i);
            View card = inflater.inflate(R.layout.item_quiz_question, llQuestionsContainer, false);

            TextView tvNumber = card.findViewById(R.id.tvQuestionNumber);
            TextView tvQuestion = card.findViewById(R.id.tvQuestion);
            Button[] optBtns = {
                card.findViewById(R.id.btnOption0),
                card.findViewById(R.id.btnOption1),
                card.findViewById(R.id.btnOption2),
                card.findViewById(R.id.btnOption3)
            };

            tvNumber.setText("Q" + (i + 1) + ".");
            tvQuestion.setText(q.getQuestion());

            List<String> options = q.getOptions();
            final int qIndex = i;

            for (int j = 0; j < optBtns.length; j++) {
                if (options != null && j < options.size()) {
                    optBtns[j].setVisibility(View.VISIBLE);
                    optBtns[j].setText(options.get(j));
                    optBtns[j].setTextColor(getColor(R.color.colorTextPrimary));
                    optBtns[j].setBackground(getDrawable(R.drawable.shape_radio_unselected));

                    final String optionText = options.get(j);
                    final int optIdx = j;
                    optBtns[j].setOnClickListener(v -> {
                        // Store selected answer
                        selectedAnswers.put(qIndex, optionText);
                        currentQuestions.get(qIndex).setUserAnswer(optionText);

                        // Update button visuals for this question's options
                        for (int k = 0; k < optBtns.length; k++) {
                            if (options.get(k).equals(optionText)) {
                                optBtns[k].setBackground(getDrawable(R.drawable.shape_radio_selected));
                                optBtns[k].setTextColor(getColor(R.color.colorPrimary));
                            } else {
                                optBtns[k].setBackground(getDrawable(R.drawable.shape_radio_unselected));
                                optBtns[k].setTextColor(getColor(R.color.colorTextPrimary));
                            }
                        }
                    });
                } else {
                    optBtns[j].setVisibility(View.GONE);
                }
            }

            llQuestionsContainer.addView(card);
        }
    }

    // -----------------------------------------------------------------------
    // AI utilities
    // -----------------------------------------------------------------------
    private void fetchHint() {
        if (currentQuestions.isEmpty()) return;
        String firstQuestion = currentQuestions.get(0).getQuestion();
        String prompt = "Generate a helpful hint for this question without revealing the answer: "
                + firstQuestion;

        showAiLoading(getString(R.string.hint_loading));
        tvAiPrompt.setText("Prompt: " + prompt);

        String queryTopic = taskTopic + " hint: " + firstQuestion;
        ApiClient.getService().getQuiz(queryTopic).enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizResponse> call,
                                   @NonNull Response<QuizResponse> response) {
                showAiResponse(buildLocalHint(firstQuestion));
            }

            @Override
            public void onFailure(@NonNull Call<QuizResponse> call, @NonNull Throwable t) {
                showAiResponse(buildLocalHint(firstQuestion));
            }
        });
    }

    private void fetchStudyPlan() {
        String prompt = "Create a 7-day study plan for a student learning "
                + taskTopic + " based on their quiz performance.";

        showAiLoading(getString(R.string.plan_loading));
        tvAiPrompt.setText("Prompt: " + prompt);

        ApiClient.getService().getQuiz(taskTopic + " study plan").enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizResponse> call,
                                   @NonNull Response<QuizResponse> response) {
                showAiResponse(buildStudyPlan(taskTopic));
            }

            @Override
            public void onFailure(@NonNull Call<QuizResponse> call, @NonNull Throwable t) {
                showAiResponse(buildStudyPlan(taskTopic));
            }
        });
    }

    private String buildLocalHint(String question) {
        return "💡 Hint: Review the core principles of " + taskTopic + ".\n\n"
                + "For this question, consider the most fundamental and widely-accepted definition. "
                + "Eliminate options that are clearly unrelated to the main concept being asked.\n\n"
                + "Focus on: what is being asked in \"" + question.replaceAll("\\?.*", "") + "?\"";
    }

    private String buildStudyPlan(String topic) {
        String[] days = {"Day 1 (Mon)", "Day 2 (Tue)", "Day 3 (Wed)", "Day 4 (Thu)",
                         "Day 5 (Fri)", "Day 6 (Sat)", "Day 7 (Sun)"};
        String[] tasks = {
                "Read: Introduction & fundamentals of " + topic,
                "Study: Core concepts and terminology in " + topic,
                "Practice: Solve basic " + topic + " exercises",
                "Explore: Intermediate patterns in " + topic,
                "Build: Mini hands-on project applying " + topic,
                "Challenge: Advanced topics in " + topic,
                "Review: Mock quiz & consolidation of " + topic
        };
        StringBuilder sb = new StringBuilder("📅 7-Day Study Plan — " + topic + "\n\n");
        for (int i = 0; i < 7; i++) {
            sb.append(days[i]).append(": ").append(tasks[i]).append("\n");
        }
        sb.append("\n💪 Tip: Review yesterday's notes each morning before starting!");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // UI state helpers
    // -----------------------------------------------------------------------
    private void showAiLoading(String text) {
        cardAiResponse.setVisibility(View.VISIBLE);
        llAiLoading.setVisibility(View.VISIBLE);
        tvAiLoadingText.setText(text);
        tvAiPrompt.setVisibility(View.GONE);
        tvAiResponseLabel.setVisibility(View.GONE);
        tvAiResponse.setVisibility(View.GONE);
        llAiError.setVisibility(View.GONE);
    }

    private void showAiResponse(String response) {
        llAiLoading.setVisibility(View.GONE);
        tvAiPrompt.setVisibility(View.VISIBLE);
        tvAiResponseLabel.setVisibility(View.VISIBLE);
        tvAiResponse.setVisibility(View.VISIBLE);
        tvAiResponse.setText(response);
        llAiError.setVisibility(View.GONE);
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        scrollQuiz.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
    }

    private void showError() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        scrollQuiz.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
    }

    private void showQuiz() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        scrollQuiz.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);
        AnimationUtils.fadeIn(scrollQuiz, 400);
        AnimationUtils.fadeIn(btnSubmit, 400);
    }

    // -----------------------------------------------------------------------
    // Submit
    // -----------------------------------------------------------------------
    private void submitQuiz() {
        if (selectedAnswers.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_answer_all), Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure all selected answers are written back to the model
        for (Map.Entry<Integer, String> entry : selectedAnswers.entrySet()) {
            currentQuestions.get(entry.getKey()).setUserAnswer(entry.getValue());
        }

        String questionsJson = new Gson().toJson(currentQuestions);
        Intent intent = new Intent(QuizActivity.this, ResultsActivity.class);
        intent.putExtra("questions_json", questionsJson);
        intent.putExtra("task_topic", taskTopic);
        intent.putExtra("task_title", taskTitle);
        startActivity(intent);
        AnimationUtils.slideInRight(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnimationUtils.slideInLeft(this);
    }
}

package com.learningassistant.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.learningassistant.app.R;
import com.learningassistant.app.adapters.ResultsAdapter;
import com.learningassistant.app.adapters.StudyPlanAdapter;
import com.learningassistant.app.models.QuizQuestion;
import com.learningassistant.app.models.StudyDay;
import com.learningassistant.app.utils.AnimationUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {

    private TextView tvScore, tvStudyPlanPrompt;
    private RecyclerView rvResults, rvStudyPlan;
    private Button btnContinue;
    private List<QuizQuestion> questions = new ArrayList<>();
    private String taskTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        taskTopic = getIntent().getStringExtra("task_topic");
        if (taskTopic == null) taskTopic = "General";

        String questionsJson = getIntent().getStringExtra("questions_json");
        if (questionsJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<QuizQuestion>>() {}.getType();
            List<QuizQuestion> parsed = gson.fromJson(questionsJson, type);
            if (parsed != null) questions = parsed;
        }

        initViews();
        displayResults();
        displayStudyPlan();
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvStudyPlanPrompt = findViewById(R.id.tvStudyPlanPrompt);
        rvResults = findViewById(R.id.rvResults);
        rvStudyPlan = findViewById(R.id.rvStudyPlan);
        btnContinue = findViewById(R.id.btnContinue);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setNestedScrollingEnabled(false);
        rvStudyPlan.setLayoutManager(new LinearLayoutManager(this));
        rvStudyPlan.setNestedScrollingEnabled(false);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            AnimationUtils.slideInLeft(this);
            finish();
        });
    }

    private void displayResults() {
        int score = 0;
        for (QuizQuestion q : questions) {
            if (q.isCorrect()) score++;
        }
        tvScore.setText(score + " / " + questions.size());

        ResultsAdapter adapter = new ResultsAdapter(questions, taskTopic);
        rvResults.setAdapter(adapter);
    }

    private void displayStudyPlan() {
        String prompt = "Create a 7-day study plan for a student learning "
                + taskTopic + " based on their quiz performance.";
        tvStudyPlanPrompt.setText("Prompt: " + prompt);

        List<StudyDay> studyDays = generateStudyPlan(taskTopic);
        StudyPlanAdapter adapter = new StudyPlanAdapter(studyDays);
        rvStudyPlan.setAdapter(adapter);
    }

    private List<StudyDay> generateStudyPlan(String topic) {
        String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        String[] taskDescs = {
                "Introduction to " + topic + " fundamentals",
                "Core concepts & key terminology in " + topic,
                "Practice: Solve basic " + topic + " problems",
                "Intermediate patterns and techniques in " + topic,
                "Hands-on mini-project using " + topic,
                "Advanced topics & edge cases in " + topic,
                "Review & mock quiz on " + topic
        };

        List<StudyDay> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(new StudyDay(dayLabels[i], taskDescs[i], i == 0));
        }
        return days;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnimationUtils.slideInLeft(this);
    }
}

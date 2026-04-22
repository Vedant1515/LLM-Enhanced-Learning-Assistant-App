package com.learningassistant.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.learningassistant.app.R;
import com.learningassistant.app.adapters.TaskAdapter;
import com.learningassistant.app.models.Task;
import com.learningassistant.app.utils.AnimationUtils;
import com.learningassistant.app.utils.SessionManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvGreeting, tvTasksDue;
    private RecyclerView rvTasks;
    private Button btnLogout;
    private SessionManager sessionManager;
    private TaskAdapter taskAdapter;
    private final List<Task> tasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);
        initViews();
        loadDashboard();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvTasksDue = findViewById(R.id.tvTasksDue);
        rvTasks = findViewById(R.id.rvTasks);
        btnLogout = findViewById(R.id.btnLogout);

        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(tasks);
        rvTasks.setAdapter(taskAdapter);

        taskAdapter.setOnTaskClickListener(task -> {
            Intent intent = new Intent(DashboardActivity.this, QuizActivity.class);
            intent.putExtra("task_title", task.getTitle());
            intent.putExtra("task_topic", task.getTopic());
            intent.putExtra("task_description", task.getDescription());
            startActivity(intent);
            AnimationUtils.slideInRight(this);
        });

        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void loadDashboard() {
        String username = sessionManager.getUsername();
        tvGreeting.setText(String.format("Hello, %s", username));

        List<String> interests = sessionManager.getInterests();
        if (interests == null || interests.size() < 2) {
            interests = Arrays.asList("Android Dev", "Data Structures");
        }

        tasks.clear();
        tasks.add(Task.createDummy(interests.get(0)));
        tasks.add(Task.createAdvanced(interests.get(interests.size() > 1 ? 1 : 0)));
        if (interests.size() > 2) {
            tasks.add(Task.createPractice(interests.get(2)));
        } else {
            tasks.add(Task.createPractice(interests.get(0)));
        }
        if (interests.size() > 3) {
            tasks.add(Task.createReview(interests.get(3)));
        } else {
            tasks.add(Task.createReview(interests.get(interests.size() > 1 ? 1 : 0)));
        }

        taskAdapter.notifyDataSetChanged();
        tvTasksDue.setText(String.format("You have %d tasks due", tasks.size()));

        // Stagger-animate the RecyclerView items
        rvTasks.post(() -> {
            List<View> views = new ArrayList<>();
            LinearLayoutManager lm = (LinearLayoutManager) rvTasks.getLayoutManager();
            if (lm != null) {
                for (int i = 0; i < taskAdapter.getItemCount(); i++) {
                    View child = lm.findViewByPosition(i);
                    if (child != null) views.add(child);
                }
            }
            if (!views.isEmpty()) {
                AnimationUtils.staggerFadeIn(views, 100);
            }
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> doLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doLogout() {
        sessionManager.logout();
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        // Clear the entire back stack so pressing Back on LoginActivity exits the app,
        // not returns to Dashboard
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}

package com.learningassistant.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.flexbox.FlexboxLayout;
import com.learningassistant.app.R;
import com.learningassistant.app.adapters.InterestTagAdapter;
import com.learningassistant.app.network.ApiClient;
import com.learningassistant.app.network.ApiResponse;
import com.learningassistant.app.network.SaveInterestsRequest;
import com.learningassistant.app.utils.AnimationUtils;
import com.learningassistant.app.utils.SessionManager;
import java.util.Arrays;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InterestsActivity extends AppCompatActivity {

    private static final List<String> ALL_TOPICS = Arrays.asList(
            "Algorithms", "Data Structures", "Web Development", "Testing", "Databases",
            "Android Dev", "Python", "Machine Learning", "Cloud Computing", "Networking",
            "Cybersecurity", "UI/UX Design", "DevOps", "Java", "JavaScript",
            "Operating Systems", "Software Engineering", "AI Ethics", "APIs", "Git & GitHub"
    );

    private TextView tvSelectedCount;
    private Button btnNext;
    private InterestTagAdapter tagAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        sessionManager = new SessionManager(this);
        initViews();
        setupTags();
        setupListeners();
    }

    private void initViews() {
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        btnNext = findViewById(R.id.btnNext);
        FlexboxLayout flexbox = findViewById(R.id.flexboxTags);
        tagAdapter = new InterestTagAdapter(this, flexbox, ALL_TOPICS);
        tagAdapter.setOnSelectionChangedListener(count ->
                tvSelectedCount.setText(String.format("%d / 10 selected", count)));
    }

    private void setupTags() {
        tvSelectedCount.setText("0 / 10 selected");
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            List<String> selected = tagAdapter.getSelectedTags();
            if (selected.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_select_interests), Toast.LENGTH_SHORT).show();
                return;
            }

            sessionManager.saveInterests(selected);
            sessionManager.setLoggedIn(true);

            String username = sessionManager.getUsername();
            ApiClient.getService()
                    .saveInterests(new SaveInterestsRequest(username, selected))
                    .enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            // Proceed regardless — local save already done
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            // Backend offline — local save is the source of truth
                        }
                    });

            Intent intent = new Intent(InterestsActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            AnimationUtils.slideInRight(this);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnimationUtils.slideInLeft(this);
    }
}

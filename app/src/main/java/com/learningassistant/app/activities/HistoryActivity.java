package com.learningassistant.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.learningassistant.app.R;
import com.learningassistant.app.adapters.HistoryAdapter;
import com.learningassistant.app.models.QuizResult;
import com.learningassistant.app.network.ApiClient;
import com.learningassistant.app.network.HistoryResponse;
import com.learningassistant.app.utils.AnimationUtils;
import com.learningassistant.app.utils.SessionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private LinearLayout layoutEmpty;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        AnimationUtils.slideInRight(this);

        sessionManager = new SessionManager(this);
        rvHistory  = findViewById(R.id.rvHistory);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // Show local data immediately, then refresh from backend
        List<QuizResult> local = sessionManager.getQuizHistory();
        if (local != null && !local.isEmpty()) {
            showHistory(local);
        }

        loadFromBackend();
    }

    private void loadFromBackend() {
        String username = sessionManager.getUsername();
        if (username == null || username.isEmpty()) {
            if (sessionManager.getQuizHistory().isEmpty()) showEmpty();
            return;
        }

        ApiClient.getService().getHistory(username).enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    List<QuizResult> backendHistory = response.body().getHistory();
                    if (backendHistory != null && !backendHistory.isEmpty()) {
                        showHistory(backendHistory);
                    } else {
                        // Backend returned empty — fall back to local
                        List<QuizResult> local = sessionManager.getQuizHistory();
                        if (local != null && !local.isEmpty()) {
                            showHistory(local);
                        } else {
                            showEmpty();
                        }
                    }
                } else {
                    fallbackToLocal();
                }
            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
                fallbackToLocal();
            }
        });
    }

    private void fallbackToLocal() {
        List<QuizResult> local = sessionManager.getQuizHistory();
        if (local != null && !local.isEmpty()) {
            showHistory(local);
        } else {
            showEmpty();
        }
    }

    private void showHistory(List<QuizResult> history) {
        Collections.sort(history, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

        rvHistory.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        HistoryAdapter adapter = new HistoryAdapter(history);
        rvHistory.setAdapter(adapter);

        rvHistory.post(() -> {
            List<View> views = new ArrayList<>();
            LinearLayoutManager lm = (LinearLayoutManager) rvHistory.getLayoutManager();
            if (lm != null) {
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    View child = lm.findViewByPosition(i);
                    if (child != null) views.add(child);
                }
            }
            if (!views.isEmpty()) AnimationUtils.staggerFadeIn(views, 80);
        });
    }

    private void showEmpty() {
        rvHistory.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        AnimationUtils.fadeIn(layoutEmpty, 400);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnimationUtils.slideInLeft(this);
    }
}

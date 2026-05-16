package com.learningassistant.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.learningassistant.app.R;
import com.learningassistant.app.models.QuizQuestion;
import com.learningassistant.app.models.QuizResult;
import com.learningassistant.app.network.ApiClient;
import com.learningassistant.app.network.ProfileResponse;
import com.learningassistant.app.network.QuizResponse;
import com.learningassistant.app.utils.AnimationUtils;
import com.learningassistant.app.utils.QRCodeUtils;
import com.learningassistant.app.utils.SessionManager;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvUsername, tvEmail, tvAvatar;
    private TextView tvTotalQuestions, tvCorrectAnswers, tvIncorrectAnswers;
    private CardView cardAiSummary;
    private TextView tvAiPrompt, tvAiResponse;
    private ProgressBar progressAiSummary;
    private Button btnRetryAi;
    private View layoutAiSummaryTrigger;

    private int totalQ, totalCorrect, totalIncorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        AnimationUtils.slideInRight(this);

        sessionManager = new SessionManager(this);
        initViews();
        loadUserData();
        loadStats();
        setupClickListeners();
    }

    private void initViews() {
        tvUsername           = findViewById(R.id.tvUsername);
        tvEmail              = findViewById(R.id.tvEmail);
        tvAvatar             = findViewById(R.id.tvAvatar);
        tvTotalQuestions     = findViewById(R.id.tvTotalQuestions);
        tvCorrectAnswers     = findViewById(R.id.tvCorrectAnswers);
        tvIncorrectAnswers   = findViewById(R.id.tvIncorrectAnswers);
        cardAiSummary        = findViewById(R.id.cardAiSummary);
        tvAiPrompt           = findViewById(R.id.tvAiPrompt);
        tvAiResponse         = findViewById(R.id.tvAiResponse);
        progressAiSummary    = findViewById(R.id.progressAiSummary);
        btnRetryAi           = findViewById(R.id.btnRetryAi);
        layoutAiSummaryTrigger = findViewById(R.id.layoutAiSummaryTrigger);
    }

    private void loadUserData() {
        String username = sessionManager.getUsername();
        String email    = sessionManager.getUser().getEmail();
        String initials = sessionManager.getUser().getInitials();
        if (initials == null || initials.isEmpty()) initials = "AB";

        tvUsername.setText(username);
        tvEmail.setText(email != null && !email.isEmpty() ? email : "student@learnai.app");
        tvAvatar.setText(initials);
    }

    private void loadStats() {
        // Show local stats immediately
        totalQ         = sessionManager.getTotalQuestions();
        totalCorrect   = sessionManager.getTotalCorrect();
        totalIncorrect = sessionManager.getTotalIncorrect();

        if (totalQ == 0 && totalCorrect == 0 && totalIncorrect == 0) {
            totalQ = 10; totalCorrect = 10; totalIncorrect = 10;
        }

        updateStatViews();

        // Refresh from backend in background
        String username = sessionManager.getUsername();
        if (username != null && !username.isEmpty()) {
            ApiClient.getService().getProfile(username).enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().isSuccess()) {
                        ProfileResponse.ProfileData pd = response.body().getProfile();
                        if (pd != null) {
                            totalQ         = pd.getTotalQuestions();
                            totalCorrect   = pd.getTotalCorrect();
                            totalIncorrect = pd.getTotalIncorrect();

                            // Sync backend totals to local (overwrite, not accumulate)
                            sessionManager.setTotalStats(totalQ, totalCorrect, totalIncorrect);

                            if (!pd.getUpgradeTier().isEmpty()) {
                                sessionManager.saveUpgradeTier(pd.getUpgradeTier());
                            }

                            if (totalQ == 0 && totalCorrect == 0 && totalIncorrect == 0) {
                                totalQ = 10; totalCorrect = 10; totalIncorrect = 10;
                            }
                            runOnUiThread(() -> {
                                updateStatViews();
                                loadCurrentPlan();
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    // Backend offline — local stats already shown
                }
            });
        }

        List<View> statViews = new ArrayList<>();
        statViews.add(findViewById(R.id.cardTotalQuestions));
        statViews.add(findViewById(R.id.cardCorrectAnswers));
        statViews.add(findViewById(R.id.cardIncorrect));
        AnimationUtils.staggerFadeIn(statViews, 100);
    }

    private void updateStatViews() {
        tvTotalQuestions.setText(String.valueOf(totalQ));
        tvCorrectAnswers.setText(String.valueOf(totalCorrect));
        tvIncorrectAnswers.setText(String.valueOf(totalIncorrect));
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        layoutAiSummaryTrigger.setOnClickListener(v -> triggerAiSummary());
        btnRetryAi.setOnClickListener(v -> triggerAiSummary());
        findViewById(R.id.btnShare).setOnClickListener(v -> showQrShareSheet());
        AnimationUtils.bounceIn(findViewById(R.id.btnShare));
        loadCurrentPlan();
    }

    private void loadCurrentPlan() {
        TextView tvPlanName        = findViewById(R.id.tvPlanName);
        TextView tvPlanDescription = findViewById(R.id.tvPlanDescription);
        TextView tvPlanPrice       = findViewById(R.id.tvPlanPrice);
        ImageView ivPlanIcon       = findViewById(R.id.ivPlanIcon);

        String savedTier = sessionManager.getUpgradeTier();

        if (savedTier == null || savedTier.isEmpty()) {
            tvPlanName.setText("Free");
            tvPlanName.setTextColor(ContextCompat.getColor(this, R.color.colorTextMuted));
            tvPlanDescription.setText("Upgrade to unlock more features");
            tvPlanPrice.setText("");
            ivPlanIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorTextMuted));
            return;
        }

        String description;
        String price;
        int nameColor;
        int iconColor;

        switch (savedTier) {
            case "Starter":
                description = "Improved Quiz generation";
                price       = "$4.99/month";
                nameColor   = ContextCompat.getColor(this, R.color.colorPrimary);
                iconColor   = ContextCompat.getColor(this, R.color.colorPrimary);
                break;
            case "Intermediate":
                description = "Advanced AI quiz personalisation";
                price       = "$9.99/month";
                nameColor   = ContextCompat.getColor(this, R.color.colorAccentYellow);
                iconColor   = ContextCompat.getColor(this, R.color.colorAccentYellow);
                break;
            case "Advanced":
                description = "Full LLM study coach + analytics";
                price       = "$19.99/month";
                nameColor   = ContextCompat.getColor(this, R.color.colorSuccess);
                iconColor   = ContextCompat.getColor(this, R.color.colorSuccess);
                break;
            default:
                description = savedTier + " plan active";
                price       = "";
                nameColor   = ContextCompat.getColor(this, R.color.colorPrimary);
                iconColor   = ContextCompat.getColor(this, R.color.colorPrimary);
                break;
        }

        tvPlanName.setText(savedTier);
        tvPlanName.setTextColor(nameColor);
        tvPlanDescription.setText(description);
        tvPlanPrice.setText(price);
        tvPlanPrice.setTextColor(nameColor);
        ivPlanIcon.setColorFilter(iconColor);
        ivPlanIcon.setImageResource(R.drawable.ic_star);

        AnimationUtils.bounceIn(findViewById(R.id.cardCurrentPlan));
    }

    // -------------------------------------------------------------------------
    // AI Summary
    // -------------------------------------------------------------------------

    private void triggerAiSummary() {
        cardAiSummary.setVisibility(View.VISIBLE);
        progressAiSummary.setVisibility(View.VISIBLE);
        tvAiResponse.setText("");
        btnRetryAi.setVisibility(View.GONE);
        tvAiResponse.setVisibility(View.GONE);

        List<String> recentTopics = new ArrayList<>();
        List<QuizResult> history = sessionManager.getQuizHistory();
        for (int i = Math.max(0, history.size() - 3); i < history.size(); i++) {
            recentTopics.add(history.get(i).getTopic());
        }
        if (recentTopics.isEmpty()) recentTopics.add("general programming");

        String topicsStr = String.join(", ", recentTopics);
        String prompt = "Prompt: Summarise the learning gaps for a student who answered "
                + totalIncorrect + " out of " + totalQ
                + " questions incorrectly on topics including " + topicsStr;
        tvAiPrompt.setText(prompt);

        ApiClient.getService().getQuiz(topicsStr + " common mistakes and how to improve")
                .enqueue(new Callback<QuizResponse>() {
                    @Override
                    public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
                        progressAiSummary.setVisibility(View.GONE);
                        tvAiResponse.setVisibility(View.VISIBLE);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getQuiz() != null) {
                            List<QuizQuestion> questions = response.body().getQuiz();
                            StringBuilder sb = new StringBuilder("Based on your performance, key areas to review:\n\n");
                            for (int i = 0; i < questions.size(); i++) {
                                sb.append(i + 1).append(". ").append(questions.get(i).getQuestion()).append("\n\n");
                            }
                            sb.append("Focus on practicing these concepts to improve your score!");
                            tvAiResponse.setText(sb.toString());
                        } else {
                            showAiError();
                        }
                    }

                    @Override
                    public void onFailure(Call<QuizResponse> call, Throwable t) {
                        progressAiSummary.setVisibility(View.GONE);
                        tvAiResponse.setVisibility(View.VISIBLE);
                        showAiError();
                    }
                });
    }

    private void showAiError() {
        tvAiResponse.setText("Could not generate summary. Make sure the backend is running.");
        btnRetryAi.setVisibility(View.VISIBLE);
    }

    // -------------------------------------------------------------------------
    // QR Share bottom sheet
    // -------------------------------------------------------------------------

    private void showQrShareSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.BottomSheetTheme);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.dialog_qr_share, null);
        sheet.setContentView(sheetView);

        ImageView ivQr      = sheetView.findViewById(R.id.ivQrCode);
        ProgressBar progress = sheetView.findViewById(R.id.progressQr);
        TextView tvData     = sheetView.findViewById(R.id.tvQrProfileData);
        Button btnShareQr   = sheetView.findViewById(R.id.btnShareQrImage);
        Button btnShareTxt  = sheetView.findViewById(R.id.btnShareText);
        Button btnClose     = sheetView.findViewById(R.id.btnCloseDialog);

        String username   = sessionManager.getUsername();
        String profileText = buildProfileText(username);
        String qrContent  = buildQrContent(username);
        tvData.setText(profileText);

        btnClose.setOnClickListener(v -> sheet.dismiss());
        btnShareTxt.setOnClickListener(v -> {
            sheet.dismiss();
            shareAsText(profileText);
        });

        final Bitmap[] qrHolder = {null};

        new Thread(() -> {
            try {
                Bitmap qr = QRCodeUtils.generate(qrContent, 512);
                qrHolder[0] = qr;
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    ivQr.setImageBitmap(qr);
                    ivQr.setVisibility(View.VISIBLE);
                    btnShareQr.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    tvData.setText("Could not generate QR code.");
                });
            }
        }).start();

        btnShareQr.setOnClickListener(v -> {
            if (qrHolder[0] != null) {
                sheet.dismiss();
                shareQrImage(qrHolder[0], profileText);
            }
        });

        sheet.show();
    }

    private String buildQrContent(String username) {
        return "LearnAI Profile\n"
                + "User: " + username + "\n"
                + "Total: " + totalQ
                + " | Correct: " + totalCorrect
                + " | Incorrect: " + totalIncorrect + "\n"
                + "App: LearnAI (SIT708)";
    }

    private String buildProfileText(String username) {
        return "LearnAI Profile\n"
                + "Username: " + username + "\n"
                + "Total Questions: " + totalQ + "\n"
                + "Correctly Answered: " + totalCorrect + "\n"
                + "Incorrect Answers: " + totalIncorrect + "\n"
                + "Keep learning with LearnAI!";
    }

    private void shareQrImage(Bitmap bitmap, String caption) {
        try {
            File imagesDir = new File(getCacheDir(), "images");
            imagesDir.mkdirs();
            File qrFile = new File(imagesDir, "profile_qr.png");
            FileOutputStream fos = new FileOutputStream(qrFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", qrFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, caption);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Profile QR via"));
        } catch (Exception e) {
            shareAsText(caption);
        }
    }

    private void shareAsText(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Share Profile via"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnimationUtils.slideInLeft(this);
    }
}

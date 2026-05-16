package com.learningassistant.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.learningassistant.app.R;
import com.learningassistant.app.models.User;
import com.learningassistant.app.network.ApiClient;
import com.learningassistant.app.network.AuthResponse;
import com.learningassistant.app.network.LoginRequest;
import com.learningassistant.app.utils.AnimationUtils;
import com.learningassistant.app.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvSignUp;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        initViews();
        setupListeners();
        AnimationUtils.bounceIn(btnLogin);
    }

    private void initViews() {
        tilUsername   = findViewById(R.id.tilUsername);
        tilPassword   = findViewById(R.id.tilPassword);
        etUsername    = findViewById(R.id.etUsername);
        etPassword    = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            AnimationUtils.slideInRight(this);
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText() != null
                ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null
                ? etPassword.getText().toString() : "";

        tilUsername.setError(null);
        tilPassword.setError(null);

        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.error_empty_username));
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_empty_password));
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_short_password));
            return;
        }

        setLoading(true);

        ApiClient.getService().login(new LoginRequest(username, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            AuthResponse.UserData userData = response.body().getUser();
                            String email = userData != null ? userData.getEmail() : "";
                            String phone = userData != null ? userData.getPhone() : "";
                            String tier  = userData != null ? userData.getUpgradeTier() : "";

                            User user = new User(username, email, password, phone);
                            sessionManager.saveUser(user);
                            sessionManager.setLoggedIn(true);
                            if (!tier.isEmpty()) {
                                sessionManager.saveUpgradeTier(tier);
                            }
                            navigateToDashboard();
                        } else {
                            String msg = (response.body() != null)
                                    ? response.body().getMessage() : "Login failed";
                            tilPassword.setError(msg);
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        setLoading(false);
                        // Offline fallback — accept any credentials locally
                        User user = new User(username, "", password, "");
                        sessionManager.saveUser(user);
                        sessionManager.setLoggedIn(true);
                        Toast.makeText(LoginActivity.this,
                                "Backend offline — logged in locally", Toast.LENGTH_SHORT).show();
                        navigateToDashboard();
                    }
                });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        AnimationUtils.slideInRight(this);
        finish();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
    }
}

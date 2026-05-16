package com.learningassistant.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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
import com.learningassistant.app.network.RegisterRequest;
import com.learningassistant.app.utils.AnimationUtils;
import com.learningassistant.app.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilEmail, tilConfirmEmail,
            tilPassword, tilConfirmPassword, tilPhone;
    private TextInputEditText etUsername, etEmail, etConfirmEmail,
            etPassword, etConfirmPassword, etPhone;
    private Button btnCreateAccount;
    private TextView tvLogin;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sessionManager = new SessionManager(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        tilUsername       = findViewById(R.id.tilUsername);
        tilEmail          = findViewById(R.id.tilEmail);
        tilConfirmEmail   = findViewById(R.id.tilConfirmEmail);
        tilPassword       = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilPhone          = findViewById(R.id.tilPhone);

        etUsername       = findViewById(R.id.etUsername);
        etEmail          = findViewById(R.id.etEmail);
        etConfirmEmail   = findViewById(R.id.etConfirmEmail);
        etPassword       = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone          = findViewById(R.id.etPhone);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLogin          = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnCreateAccount.setOnClickListener(v -> attemptSignup());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            AnimationUtils.slideInLeft(this);
        });
    }

    private void attemptSignup() {
        clearErrors();

        String username       = getText(etUsername);
        String email          = getText(etEmail);
        String confirmEmail   = getText(etConfirmEmail);
        String password       = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);
        String phone          = getText(etPhone);

        if (username.isEmpty()) { tilUsername.setError(getString(R.string.error_empty_username)); return; }
        if (username.length() < 3) { tilUsername.setError(getString(R.string.error_short_username)); return; }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email)); return;
        }
        if (!email.equals(confirmEmail)) { tilConfirmEmail.setError(getString(R.string.error_email_mismatch)); return; }
        if (password.length() < 6) { tilPassword.setError(getString(R.string.error_short_password)); return; }
        if (!password.equals(confirmPassword)) { tilConfirmPassword.setError(getString(R.string.error_password_mismatch)); return; }
        if (phone.isEmpty()) { tilPhone.setError(getString(R.string.error_empty_phone)); return; }

        setLoading(true);

        final String finalUsername = username;
        final String finalEmail    = email;
        final String finalPassword = password;
        final String finalPhone    = phone;

        ApiClient.getService()
                .register(new RegisterRequest(finalUsername, finalEmail, finalPassword, finalPhone))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            saveLocallyAndProceed(finalUsername, finalEmail, finalPassword, finalPhone);
                        } else if (response.code() == 409) {
                            tilUsername.setError("Username already taken");
                        } else {
                            String msg = (response.body() != null)
                                    ? response.body().getMessage() : "Registration failed";
                            Toast.makeText(SignupActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        setLoading(false);
                        // Offline fallback
                        Toast.makeText(SignupActivity.this,
                                "Backend offline — saved locally", Toast.LENGTH_SHORT).show();
                        saveLocallyAndProceed(finalUsername, finalEmail, finalPassword, finalPhone);
                    }
                });
    }

    private void saveLocallyAndProceed(String username, String email,
                                        String password, String phone) {
        User user = new User(username, email, password, phone);
        sessionManager.saveUser(user);

        Intent intent = new Intent(SignupActivity.this, InterestsActivity.class);
        startActivity(intent);
        AnimationUtils.slideInRight(this);
    }

    private void clearErrors() {
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilConfirmEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilPhone.setError(null);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        btnCreateAccount.setEnabled(!loading);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnimationUtils.slideInLeft(this);
    }
}

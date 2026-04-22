package com.learningassistant.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.learningassistant.app.R;
import com.learningassistant.app.models.User;
import com.learningassistant.app.utils.AnimationUtils;
import com.learningassistant.app.utils.SessionManager;

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
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilConfirmEmail = findViewById(R.id.tilConfirmEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilPhone = findViewById(R.id.tilPhone);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etConfirmEmail = findViewById(R.id.etConfirmEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone = findViewById(R.id.etPhone);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLogin = findViewById(R.id.tvLogin);
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

        String username = getText(etUsername);
        String email = getText(etEmail);
        String confirmEmail = getText(etConfirmEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);
        String phone = getText(etPhone);

        if (username.isEmpty()) { tilUsername.setError(getString(R.string.error_empty_username)); return; }
        if (username.length() < 3) { tilUsername.setError(getString(R.string.error_short_username)); return; }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email)); return;
        }
        if (!email.equals(confirmEmail)) { tilConfirmEmail.setError(getString(R.string.error_email_mismatch)); return; }
        if (password.length() < 6) { tilPassword.setError(getString(R.string.error_short_password)); return; }
        if (!password.equals(confirmPassword)) { tilConfirmPassword.setError(getString(R.string.error_password_mismatch)); return; }
        if (phone.isEmpty()) { tilPhone.setError(getString(R.string.error_empty_phone)); return; }

        User user = new User(username, email, password, phone);
        sessionManager.saveUser(user);
        // Do NOT set logged in yet — wait until interests are selected

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnimationUtils.slideInLeft(this);
    }
}

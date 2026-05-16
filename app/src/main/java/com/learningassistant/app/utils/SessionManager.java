package com.learningassistant.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.learningassistant.app.models.QuizResult;
import com.learningassistant.app.models.User;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private static final String PREFS_NAME = "learn_ai_prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_AVATAR_INITIALS = "avatarInitials";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_INTERESTS = "interests";
    private static final String KEY_QUIZ_HISTORY = "quiz_history";
    private static final String KEY_STAT_TOTAL = "stat_total";
    private static final String KEY_STAT_CORRECT = "stat_correct";
    private static final String KEY_STAT_INCORRECT = "stat_incorrect";
    private static final String KEY_UPGRADE_TIER = "upgrade_tier";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    public void saveUser(User user) {
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_AVATAR_INITIALS, user.getInitials());
        editor.putString(KEY_PHONE, user.getPhoneNumber());
        if (user.getInterests() != null && !user.getInterests().isEmpty()) {
            editor.putString(KEY_INTERESTS, gson.toJson(user.getInterests()));
        }
        editor.apply();
    }

    public User getUser() {
        User user = new User();
        user.setUsername(prefs.getString(KEY_USERNAME, ""));
        user.setEmail(prefs.getString(KEY_EMAIL, ""));
        user.setPhoneNumber(prefs.getString(KEY_PHONE, ""));
        user.setAvatarInitials(prefs.getString(KEY_AVATAR_INITIALS, "??"));
        user.setInterests(getInterests());
        return user;
    }

    public void setLoggedIn(boolean loggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, loggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public void saveInterests(List<String> interests) {
        editor.putString(KEY_INTERESTS, gson.toJson(interests));
        editor.apply();
    }

    public List<String> getInterests() {
        String json = prefs.getString(KEY_INTERESTS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Student");
    }

    public void saveQuizResult(QuizResult result) {
        List<QuizResult> history = getQuizHistory();
        history.add(result);
        editor.putString(KEY_QUIZ_HISTORY, gson.toJson(history));
        editor.apply();
    }

    public List<QuizResult> getQuizHistory() {
        String json = prefs.getString(KEY_QUIZ_HISTORY, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<QuizResult>>() {}.getType();
        List<QuizResult> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public void clearHistory() {
        editor.remove(KEY_QUIZ_HISTORY);
        editor.apply();
    }

    public void saveTotalStats(int totalQuestions, int correct, int incorrect) {
        editor.putInt(KEY_STAT_TOTAL, prefs.getInt(KEY_STAT_TOTAL, 0) + totalQuestions);
        editor.putInt(KEY_STAT_CORRECT, prefs.getInt(KEY_STAT_CORRECT, 0) + correct);
        editor.putInt(KEY_STAT_INCORRECT, prefs.getInt(KEY_STAT_INCORRECT, 0) + incorrect);
        editor.apply();
    }

    /** Overwrite totals with absolute values (used when syncing from backend). */
    public void setTotalStats(int totalQuestions, int correct, int incorrect) {
        editor.putInt(KEY_STAT_TOTAL, totalQuestions);
        editor.putInt(KEY_STAT_CORRECT, correct);
        editor.putInt(KEY_STAT_INCORRECT, incorrect);
        editor.apply();
    }

    public int getTotalQuestions() {
        return prefs.getInt(KEY_STAT_TOTAL, 0);
    }

    public int getTotalCorrect() {
        return prefs.getInt(KEY_STAT_CORRECT, 0);
    }

    public int getTotalIncorrect() {
        return prefs.getInt(KEY_STAT_INCORRECT, 0);
    }

    public void saveUpgradeTier(String tier) {
        editor.putString(KEY_UPGRADE_TIER, tier);
        editor.apply();
    }

    public String getUpgradeTier() {
        return prefs.getString(KEY_UPGRADE_TIER, "");
    }
}

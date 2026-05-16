package com.learningassistant.app.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProfileResponse {
    @SerializedName("success") private boolean success;
    @SerializedName("profile") private ProfileData profile;

    public boolean     isSuccess() { return success; }
    public ProfileData getProfile(){ return profile; }

    public static class ProfileData {
        @SerializedName("username")        private String username;
        @SerializedName("email")           private String email;
        @SerializedName("phone")           private String phone;
        @SerializedName("interests")       private List<String> interests;
        @SerializedName("upgrade_tier")    private String upgradeTier;
        @SerializedName("total_questions") private int totalQuestions;
        @SerializedName("total_correct")   private int totalCorrect;
        @SerializedName("total_incorrect") private int totalIncorrect;

        public String       getUsername()      { return username    != null ? username    : ""; }
        public String       getEmail()         { return email       != null ? email       : ""; }
        public String       getPhone()         { return phone       != null ? phone       : ""; }
        public List<String> getInterests()     { return interests   != null ? interests   : java.util.Collections.emptyList(); }
        public String       getUpgradeTier()   { return upgradeTier != null ? upgradeTier : ""; }
        public int          getTotalQuestions(){ return totalQuestions; }
        public int          getTotalCorrect()  { return totalCorrect; }
        public int          getTotalIncorrect(){ return totalIncorrect; }
    }
}

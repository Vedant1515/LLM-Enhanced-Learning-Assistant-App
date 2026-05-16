package com.learningassistant.app.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AuthResponse {
    @SerializedName("success") private boolean success;
    @SerializedName("message") private String message;
    @SerializedName("user")    private UserData user;

    public boolean isSuccess()  { return success; }
    public String  getMessage() { return message; }
    public UserData getUser()   { return user; }

    public static class UserData {
        @SerializedName("username")     private String username;
        @SerializedName("email")        private String email;
        @SerializedName("phone")        private String phone;
        @SerializedName("interests")    private List<String> interests;
        @SerializedName("upgrade_tier") private String upgradeTier;

        public String       getUsername()   { return username   != null ? username   : ""; }
        public String       getEmail()      { return email      != null ? email      : ""; }
        public String       getPhone()      { return phone      != null ? phone      : ""; }
        public List<String> getInterests()  { return interests  != null ? interests  : java.util.Collections.emptyList(); }
        public String       getUpgradeTier(){ return upgradeTier != null ? upgradeTier : ""; }
    }
}

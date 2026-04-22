package com.learningassistant.app.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private List<String> interests;
    private String avatarInitials;

    public User() {
        this.interests = new ArrayList<>();
    }

    public User(String username, String email, String password, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.interests = new ArrayList<>();
        this.avatarInitials = getInitials();
    }

    public String getInitials() {
        if (username == null || username.isEmpty()) return "??";
        return username.length() >= 2
                ? username.substring(0, 2).toUpperCase()
                : username.substring(0, 1).toUpperCase();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username;
        this.avatarInitials = getInitials();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public String getAvatarInitials() { return avatarInitials; }
    public void setAvatarInitials(String avatarInitials) { this.avatarInitials = avatarInitials; }
}

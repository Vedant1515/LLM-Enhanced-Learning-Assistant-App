package com.learningassistant.app.network;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("username") private String username;
    @SerializedName("email")    private String email;
    @SerializedName("password") private String password;
    @SerializedName("phone")    private String phone;

    public RegisterRequest(String username, String email, String password, String phone) {
        this.username = username;
        this.email    = email;
        this.password = password;
        this.phone    = phone;
    }
}

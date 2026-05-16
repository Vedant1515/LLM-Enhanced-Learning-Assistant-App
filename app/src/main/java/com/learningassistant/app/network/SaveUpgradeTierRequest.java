package com.learningassistant.app.network;

import com.google.gson.annotations.SerializedName;

public class SaveUpgradeTierRequest {
    @SerializedName("username") private String username;
    @SerializedName("tier")     private String tier;

    public SaveUpgradeTierRequest(String username, String tier) {
        this.username = username;
        this.tier     = tier;
    }
}

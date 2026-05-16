package com.learningassistant.app.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SaveInterestsRequest {
    @SerializedName("username")  private String username;
    @SerializedName("interests") private List<String> interests;

    public SaveInterestsRequest(String username, List<String> interests) {
        this.username  = username;
        this.interests = interests;
    }
}

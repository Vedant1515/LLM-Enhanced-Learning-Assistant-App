package com.learningassistant.app.network;

import com.google.gson.annotations.SerializedName;
import com.learningassistant.app.models.QuizResult;
import java.util.List;

public class HistoryResponse {
    @SerializedName("success") private boolean success;
    @SerializedName("history") private List<QuizResult> history;

    public boolean         isSuccess() { return success; }
    public List<QuizResult> getHistory() {
        return history != null ? history : java.util.Collections.emptyList();
    }
}

package com.learningassistant.app.network;

import com.google.gson.annotations.SerializedName;
import com.learningassistant.app.models.QuizQuestion;
import java.util.List;

public class SaveQuizResultRequest {
    @SerializedName("username")        private String username;
    @SerializedName("topic")           private String topic;
    @SerializedName("taskTitle")       private String taskTitle;
    @SerializedName("questions")       private List<QuizQuestion> questions;
    @SerializedName("correctCount")    private int correctCount;
    @SerializedName("totalQuestions")  private int totalQuestions;
    @SerializedName("timestamp")       private long timestamp;

    public SaveQuizResultRequest(String username, String topic, String taskTitle,
                                  List<QuizQuestion> questions, int correctCount, long timestamp) {
        this.username       = username;
        this.topic          = topic;
        this.taskTitle      = taskTitle;
        this.questions      = questions;
        this.correctCount   = correctCount;
        this.totalQuestions = questions != null ? questions.size() : 0;
        this.timestamp      = timestamp;
    }
}

package com.learningassistant.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.UUID;

public class QuizResult {
    @SerializedName("id")
    private String id;

    @SerializedName("topic")
    private String topic;

    @SerializedName("taskTitle")
    private String taskTitle;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("totalQuestions")
    private int totalQuestions;

    @SerializedName("correctCount")
    private int correctCount;

    @SerializedName("incorrectCount")
    private int incorrectCount;

    @SerializedName("questions")
    private List<QuizQuestion> questions;

    public QuizResult() {}

    public QuizResult(String topic, String taskTitle, List<QuizQuestion> questions, int correctCount) {
        this.id = UUID.randomUUID().toString();
        this.topic = topic;
        this.taskTitle = taskTitle;
        this.timestamp = System.currentTimeMillis();
        this.totalQuestions = questions != null ? questions.size() : 0;
        this.correctCount = correctCount;
        this.incorrectCount = this.totalQuestions - correctCount;
        this.questions = questions;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public int getIncorrectCount() { return incorrectCount; }
    public void setIncorrectCount(int incorrectCount) { this.incorrectCount = incorrectCount; }

    public List<QuizQuestion> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestion> questions) { this.questions = questions; }
}

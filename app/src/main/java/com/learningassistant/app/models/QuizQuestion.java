package com.learningassistant.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizQuestion {
    @SerializedName("question")
    private String question;

    @SerializedName("options")
    private List<String> options;

    @SerializedName("correct_answer")
    private String correctAnswer;

    private String userAnswer;
    private boolean answered;

    public QuizQuestion() {}

    public QuizQuestion(String question, List<String> options, String correctAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.answered = false;
    }

    public boolean isCorrect() {
        if (userAnswer == null || correctAnswer == null) return false;
        return correctAnswer.equalsIgnoreCase(userAnswer.trim());
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
        this.answered = (userAnswer != null && !userAnswer.isEmpty());
    }

    public boolean isAnswered() { return answered; }
    public void setAnswered(boolean answered) { this.answered = answered; }
}

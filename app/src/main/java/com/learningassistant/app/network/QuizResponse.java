package com.learningassistant.app.network;

import com.google.gson.annotations.SerializedName;
import com.learningassistant.app.models.QuizQuestion;
import java.util.List;

public class QuizResponse {
    @SerializedName("quiz")
    private List<QuizQuestion> quiz;

    public List<QuizQuestion> getQuiz() { return quiz; }
    public void setQuiz(List<QuizQuestion> quiz) { this.quiz = quiz; }
}

package com.learningassistant.app.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("/")
    Call<ResponseBody> getHome();

    @GET("/getQuiz")
    Call<QuizResponse> getQuiz(@Query("topic") String topic);
}

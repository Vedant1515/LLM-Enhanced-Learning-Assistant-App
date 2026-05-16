package com.learningassistant.app.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("/getQuiz")
    Call<QuizResponse> getQuiz(@Query("topic") String topic);

    @POST("/register")
    Call<AuthResponse> register(@Body RegisterRequest body);

    @POST("/login")
    Call<AuthResponse> login(@Body LoginRequest body);

    @POST("/saveInterests")
    Call<ApiResponse> saveInterests(@Body SaveInterestsRequest body);

    @POST("/saveQuizResult")
    Call<ApiResponse> saveQuizResult(@Body SaveQuizResultRequest body);

    @GET("/getHistory")
    Call<HistoryResponse> getHistory(@Query("username") String username);

    @GET("/getProfile")
    Call<ProfileResponse> getProfile(@Query("username") String username);

    @POST("/saveUpgradeTier")
    Call<ApiResponse> saveUpgradeTier(@Body SaveUpgradeTierRequest body);
}

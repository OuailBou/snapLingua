package com.example.snap.services;

import com.example.snap.models.TranslateResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TranslationApiService {
    // MyMemory usa el endpoint "get", no "translate"
    @GET("get")
    Call<TranslateResponse> translate(
            @Query("q") String text,
            @Query("langpair") String langPair
    );
}
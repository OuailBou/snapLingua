package com.example.snap.services;

import android.content.Context;

import com.example.snap.models.TranslateResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkTranslationService {
    private Context context;
    private ExecutorService executorService;
    private TranslationApiService apiService;

    public NetworkTranslationService(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        this.apiService = ApiClient.getApiService();
    }

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(String error);
    }

    public void translateText(String text, String sourceLang, String targetLang,
                              TranslationCallback callback) {

        executorService.execute(() -> {
            try {
                // Validar entrada
                if (text == null || text.trim().isEmpty()) {
                    callback.onError("Texto vacío");
                    return;
                }

                String langPair = sourceLang + "|" + targetLang;

                Call<TranslateResponse> call = apiService.translate(text, langPair);

                call.enqueue(new Callback<TranslateResponse>() {
                    @Override
                    public void onResponse(Call<TranslateResponse> call, Response<TranslateResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String translatedText = response.body().getTranslatedText();
                            if (translatedText != null && !translatedText.isEmpty()) {
                                callback.onSuccess(translatedText);
                            } else {
                                callback.onError("Traducción vacía recibida");
                            }
                        } else {
                            String errorMsg = "Error: " + response.code();
                            if (response.errorBody() != null) {
                                try {
                                    errorMsg = response.errorBody().string();
                                } catch (Exception e) {
                                    errorMsg = "Error parsing response";
                                }
                            }
                            callback.onError(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<TranslateResponse> call, Throwable t) {
                        callback.onError("Error de red: " + t.getMessage());
                    }
                });

            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        });
    }
}
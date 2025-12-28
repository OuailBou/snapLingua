package com.example.snap.models;

public class TranslateResponse {
    private ResponseData responseData;

    public String getTranslatedText() {
        return (responseData != null) ? responseData.translatedText : null;
    }

    public static class ResponseData {
        public String translatedText;
    }
}

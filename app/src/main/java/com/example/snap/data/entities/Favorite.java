package com.example.snap.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class Favorite {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;

    private String userId;
    private String originalText;
    private String translatedText;
    private String sourceLang;
    private String targetLang;
    private long addedDate;
    private boolean isExpression;

    public Favorite(String userId, String originalText, String translatedText, String sourceLang, String targetLang, boolean isExpression) {
        this.userId = userId;
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
        this.isExpression = isExpression;
        this.addedDate = System.currentTimeMillis();
    }

    // Getters y Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public String getSourceLang() { return sourceLang; }
    public void setSourceLang(String sourceLang) { this.sourceLang = sourceLang; }

    public String getTargetLang() { return targetLang; }
    public void setTargetLang(String targetLang) { this.targetLang = targetLang; }

    public long getAddedDate() { return addedDate; }
    public void setAddedDate(long addedDate) { this.addedDate = addedDate; }

    public boolean isExpression() { return isExpression; }
    void setExpression(boolean expression) { isExpression = expression; }
}

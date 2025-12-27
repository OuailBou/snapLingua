package com.example.snap;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.snap.ui.base.BaseActivity;
import com.example.snap.ui.components.BottomNavigationComponent;
import com.example.snap.ui.components.LanguageSelector;

import java.util.Locale;

public class VoiceActivity extends BaseActivity implements TextToSpeech.OnInitListener {

    private LanguageSelector languageSelector;
    private BottomNavigationComponent bottomNavigation;

    private ImageButton btnRecord;
    private ImageButton btnPlay;
    private TextView tvResult;

    private TextToSpeech tts;

    private String sourceLang = "es";
    private String targetLang = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        initializeViews();
        setupLanguageSelector();
        setupListeners();
        setupBottomNavigation();

        tts = new TextToSpeech(this, this);
        showWelcomeMessage();
    }

    @Override
    protected void onSessionUpdated() {
        if (bottomNavigation != null) {
            bottomNavigation.updateUserButtonState();
        }
    }

    private void initializeViews() {
        languageSelector = findViewById(R.id.languageSelector);
        btnRecord = findViewById(R.id.record_button);
        btnPlay = findViewById(R.id.speak_button);
        tvResult = findViewById(R.id.translated_text);
    }

    private void setupLanguageSelector() {
        languageSelector.setOnLanguageChangeListener((src, tgt, sIndex, tIndex) -> {
            sourceLang = src;
            targetLang = tgt;
        });
    }

    private void setupListeners() {

        btnRecord.setOnClickListener(v -> {
            showMessage("Grabación de voz próximamente");
        });

        btnPlay.setOnClickListener(v -> speakText(tvResult.getText().toString()));
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (bottomNavigation != null) {
            bottomNavigation.attachToScreen("audio");

        }
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(new Locale(targetLang));
        }
    }

    private void speakText(String text) {
        if (text == null || text.isEmpty()) {
            showMessage("No hay texto para reproducir");
            return;
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void showWelcomeMessage() {
        if (!isUserLoggedIn()) {
            showMessage("Modo Invitado — voz activa");
        } else {
            showMessage("Hola " + getCurrentUser());
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}

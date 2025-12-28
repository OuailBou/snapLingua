package com.example.snap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.snap.ui.base.BaseActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {

    public static final String PREFS_NAME = "SnapPrefs";
    public static final String KEY_AUTO_TTS = "auto_tts";
    public static final String KEY_SAVE_HISTORY = "save_history";
    public static final String KEY_DEFAULT_SOURCE_LANG = "default_source_lang";
    public static final String KEY_DEFAULT_TARGET_LANG = "default_target_lang";

    private SwitchMaterial switchAutoTts;
    private SwitchMaterial switchSaveHistory;
    private android.widget.LinearLayout containerDefaultSource;
    private android.widget.LinearLayout containerDefaultTarget;
    private android.widget.TextView tvDefaultSource;
    private android.widget.TextView tvDefaultTarget;
    private ImageView btnBack;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            userId = "guest";
        }

        initializeViews();
        loadPreferences();
        setupListeners();
    }

    private void initializeViews() {
        switchAutoTts = findViewById(R.id.switchAutoTts);
        switchSaveHistory = findViewById(R.id.switchSaveHistory);
        containerDefaultSource = findViewById(R.id.containerDefaultSource);
        containerDefaultTarget = findViewById(R.id.containerDefaultTarget);
        tvDefaultSource = findViewById(R.id.tvDefaultSource);
        tvDefaultTarget = findViewById(R.id.tvDefaultTarget);
        btnBack = findViewById(R.id.btnBack);
    }

    private String getPrefsName() {
        return PREFS_NAME + "_" + userId;
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(getPrefsName(), MODE_PRIVATE);
        boolean autoTts = prefs.getBoolean(KEY_AUTO_TTS, true);
        boolean saveHistory = prefs.getBoolean(KEY_SAVE_HISTORY, true);
        String sourceLang = prefs.getString(KEY_DEFAULT_SOURCE_LANG, "es");
        String targetLang = prefs.getString(KEY_DEFAULT_TARGET_LANG, "en");

        switchAutoTts.setChecked(autoTts);
        switchSaveHistory.setChecked(saveHistory);
        tvDefaultSource.setText(com.example.snap.utils.LanguageHelper.getLanguageName(sourceLang));
        tvDefaultTarget.setText(com.example.snap.utils.LanguageHelper.getLanguageName(targetLang));
    }

    private void setupListeners() {
        SharedPreferences prefs = getSharedPreferences(getPrefsName(), MODE_PRIVATE);

        switchAutoTts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_AUTO_TTS, isChecked).apply();
        });

        switchSaveHistory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_SAVE_HISTORY, isChecked).apply();
        });

        containerDefaultSource.setOnClickListener(v -> showLanguageDialog(true));
        containerDefaultTarget.setOnClickListener(v -> showLanguageDialog(false));

        btnBack.setOnClickListener(v -> finish());
    }

    private void showLanguageDialog(boolean isSource) {
        String[] languages = com.example.snap.utils.LanguageHelper.getAvailableLanguages();

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(isSource ? "Seleccionar Idioma Origen" : "Seleccionar Idioma Destino")
                .setItems(languages, (dialog, which) -> {
                    String code = com.example.snap.utils.LanguageHelper.getLanguageCode(which);
                    SharedPreferences prefs = getSharedPreferences(getPrefsName(), MODE_PRIVATE);

                    if (isSource) {
                        prefs.edit().putString(KEY_DEFAULT_SOURCE_LANG, code).apply();
                        tvDefaultSource.setText(languages[which]);
                    } else {
                        prefs.edit().putString(KEY_DEFAULT_TARGET_LANG, code).apply();
                        tvDefaultTarget.setText(languages[which]);
                    }
                })
                .show();
    }
}

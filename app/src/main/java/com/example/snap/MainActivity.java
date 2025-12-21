package com.example.snap;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.snap.presentation.viewmodel.TranslationViewModel;

public class MainActivity extends AppCompatActivity {

    // Vistas
    private Spinner spinnerInput;
    private Spinner spinnerOutput;
    private EditText etInput;
    private TextView tvOutput;
    private ImageView btnClear;
    private ImageView btnSwap;
    private ImageView btnVolume;
    private ImageView btnStar;

    private ImageView btnCopy;
    private Button chip1;
    private Button chip2;
    private Button chip3;
    private ProgressBar progressBar;

    private TranslationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupSpinners();


        viewModel = new ViewModelProvider(this).get(TranslationViewModel.class);

        setupObservers();

        setupListeners();

        Toast.makeText(this, "Traductor listo con ViewModel", Toast.LENGTH_SHORT).show();
    }

    private void setupObservers() {
        viewModel.getCurrentTranslation().observe(this, translatedText -> {
            hideProgress();
            if (translatedText != null) {
                tvOutput.setText(translatedText);

                // Animación de entrada de texto
                tvOutput.setAlpha(0);
                tvOutput.animate().alpha(1).setDuration(500).start();
            }
        });
    }

    private void initializeViews() {
        spinnerInput = findViewById(R.id.spinnerInput);
        spinnerOutput = findViewById(R.id.spinnerOutput);
        etInput = findViewById(R.id.etInput);
        tvOutput = findViewById(R.id.tvOutput);
        btnClear = findViewById(R.id.btnClear);
        btnSwap = findViewById(R.id.btnSwap);
        btnVolume = findViewById(R.id.btnVolume);
        btnStar = findViewById(R.id.btnStar);
        btnCopy = findViewById(R.id.btnCopy);
        chip1 = findViewById(R.id.chip1);
        chip2 = findViewById(R.id.chip2);
        chip3 = findViewById(R.id.chip3);
        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupSpinners() {
        String[] languages = {"Español", "Inglés", "Francés", "Alemán", "Italiano", "Portugués"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerInput.setAdapter(adapter);
        spinnerOutput.setAdapter(adapter);

        spinnerInput.setSelection(0); // Español
        spinnerOutput.setSelection(1); // Inglés
    }

    private void setupListeners() {
        // Escuchar cambios en el idioma de entrada para actualizar los textos de los chips
        spinnerInput.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateQuickTranslationChips();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        btnClear.setOnClickListener(v -> {
            etInput.setText("");
            tvOutput.setText("La traducción aparecerá aquí");
            hideProgress();
        });

        btnSwap.setOnClickListener(v -> {
            int inputPos = spinnerInput.getSelectedItemPosition();
            int outputPos = spinnerOutput.getSelectedItemPosition();
            spinnerInput.setSelection(outputPos);
            spinnerOutput.setSelection(inputPos);

            String inputText = etInput.getText().toString();
            if (!inputText.isEmpty()) {
                performTranslation();
            }
        });

        btnCopy.setOnClickListener(v -> {
            String textToCopy = tvOutput.getText().toString();
            if (!textToCopy.isEmpty() && !textToCopy.equals("Traducción aparecerá aquí")
                    && !textToCopy.equals("Traduciendo...")) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Traducción", textToCopy);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(this, "Texto copiado al portapapeles", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "No hay nada para copiar", Toast.LENGTH_SHORT).show();
            }
        });

        etInput.setOnEditorActionListener((v, actionId, event) -> {
            performTranslation();
            return true;
        });

        View.OnClickListener chipListener = v -> {
            Button b = (Button) v;
            etInput.setText(b.getText().toString());
            performTranslation();
        };
        chip1.setOnClickListener(chipListener);
        chip2.setOnClickListener(chipListener);
        chip3.setOnClickListener(chipListener);


        updateQuickTranslationChips();
    }


    private void performTranslation() {
        String text = etInput.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Escribe algo para traducir", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress();
        tvOutput.setText("Traduciendo...");

        String sourceLang = getLanguageCode(spinnerInput.getSelectedItemPosition());
        String targetLang = getLanguageCode(spinnerOutput.getSelectedItemPosition());

        viewModel.translateText(text, sourceLang, targetLang);
    }

    private String getLanguageCode(int position) {
        switch (position) {
            case 0: return "es";
            case 1: return "en";
            case 2: return "fr";
            case 3: return "de";
            case 4: return "it";
            case 5: return "pt";
            default: return "en";
        }
    }

    private void updateQuickTranslationChips() {
        String langCode = getLanguageCode(spinnerInput.getSelectedItemPosition());

        switch (langCode) {
            case "es":
                chip1.setText("Hola");
                chip2.setText("¿Cómo estás?");
                chip3.setText("Gracias");
                break;
            case "en":
                chip1.setText("Hello");
                chip2.setText("How are you?");
                chip3.setText("Thank you");
                break;
            case "fr":
                chip1.setText("Bonjour");
                chip2.setText("Comment ça va?");
                chip3.setText("Merci");
                break;
            case "de":
                chip1.setText("Hallo");
                chip2.setText("Wie geht es dir?");
                chip3.setText("Danke");
                break;
            // Añade más casos si lo deseas
            default:
                chip1.setText("Hello");
                chip2.setText("How are you?");
                chip3.setText("Thank you");
        }
    }

    private void showProgress() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        btnSwap.setEnabled(false);
    }

    private void hideProgress() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        btnSwap.setEnabled(true);
    }
}

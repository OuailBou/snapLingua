package com.example.snap;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.snap.presentation.viewmodel.TranslationViewModel;

public class MainActivity extends AppCompatActivity {

    // Vistas
    private Spinner spinnerInput, spinnerOutput;
    private EditText etInput;
    private TextView tvOutput;
    private ImageView btnClear, btnSwap, btnVolume, btnStar, btnCopy;
    private Button chip1, chip2, chip3;
    private ProgressBar progressBar;

    private TranslationViewModel viewModel;
    private String currentUserId; // Almacena el usuario logueado o null

    @Override
    protected void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Recuperar el ID del usuario: primero del Intent, si no hay, de SharedPreferences
        currentUserId = getIntent().getStringExtra("USER_ID");
        
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        
        // Si viene del Intent, asegurarse de que también esté en SharedPreferences
        if (currentUserId != null) {
            android.util.Log.d("MainActivity", "Usuario recibido del Intent: " + currentUserId);
            // Guardar en SharedPreferences para asegurar persistencia
            prefs.edit().putString("active_email", currentUserId).commit();
            android.util.Log.d("MainActivity", "Sesión guardada/actualizada en SharedPreferences");
        } else {
            // Si no viene del Intent, buscar en SharedPreferences (sesión persistente)
            currentUserId = prefs.getString("active_email", null);
            android.util.Log.d("MainActivity", "Sesión leída de SharedPreferences: " + currentUserId);
        }

        // 2. Inicializar componentes
        initializeViews();
        setupSpinners();

        // 3. Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(TranslationViewModel.class);

        // 4. Configurar flujos de datos
        setupObservers();
        setupListeners();

        // Mensaje de bienvenida dinámico
        if (currentUserId == null) {
            Toast.makeText(this, "Modo Invitado: Inicia sesión para guardar", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sesión activa: " + currentUserId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verificamos si la sesión sigue activa en el disco
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String activeEmail = prefs.getString("active_email", null);

        // Actualizar currentUserId solo si hay un cambio válido
        if (activeEmail == null && currentUserId != null) {
            // La sesión fue cerrada externamente, actualizar a null
            android.util.Log.d("MainActivity", "Sesión cerrada, actualizando a invitado");
            currentUserId = null;
        } else if (activeEmail != null) {
            // Hay sesión activa, actualizar currentUserId
            android.util.Log.d("MainActivity", "Sesión activa en onResume: " + activeEmail);
            currentUserId = activeEmail;
        }
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

    private void setupObservers() {
        viewModel.getCurrentTranslation().observe(this, translatedText -> {
            hideProgress();
            if (translatedText != null) {
                tvOutput.setText(translatedText);
                // Animación de entrada
                tvOutput.setAlpha(0);
                tvOutput.animate().alpha(1).setDuration(500).start();
            }
        });
    }

    private void setupListeners() {
        // Listener para actualizar chips según idioma seleccionado
        spinnerInput.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateQuickTranslationChips();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Botón Limpiar
        btnClear.setOnClickListener(v -> {
            etInput.setText("");
            tvOutput.setText("La traducción aparecerá aquí");
            hideProgress();
        });

        // Botón Intercambiar idiomas (Swap)
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

        // Botón Copiar al portapapeles
        btnCopy.setOnClickListener(v -> {
            String textToCopy = tvOutput.getText().toString();
            if (!textToCopy.isEmpty() && !textToCopy.equals("La traducción aparecerá aquí")
                    && !textToCopy.equals("Traduciendo...")) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Traducción", textToCopy);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Texto copiado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No hay nada para copiar", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón Star (Guardar como favorito)
        btnStar.setOnClickListener(v -> {
            String inputText = etInput.getText().toString().trim();
            String outputText = tvOutput.getText().toString().trim();
            
            if (currentUserId == null || currentUserId.trim().isEmpty()) {
                Toast.makeText(this, "Inicia sesión para guardar favoritos", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (inputText.isEmpty() || outputText.isEmpty() || 
                outputText.equals("La traducción aparecerá aquí") || 
                outputText.equals("Traduciendo...")) {
                Toast.makeText(this, "No hay traducción para guardar", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String sourceLang = getLanguageCode(spinnerInput.getSelectedItemPosition());
            String targetLang = getLanguageCode(spinnerOutput.getSelectedItemPosition());
            
            // Guardar como favorito
            viewModel.saveFavorite(currentUserId, inputText, outputText, sourceLang, targetLang);
            Toast.makeText(this, "Agregado a favoritos ⭐", Toast.LENGTH_SHORT).show();
        });

        // Enter en el teclado
        etInput.setOnEditorActionListener((v, actionId, event) -> {
            performTranslation();
            return true;
        });

        // Listeners para los Chips
        View.OnClickListener chipListener = v -> {
            Button b = (Button) v;
            etInput.setText(b.getText().toString());
            performTranslation();
        };
        chip1.setOnClickListener(chipListener);
        chip2.setOnClickListener(chipListener);
        chip3.setOnClickListener(chipListener);

        // --- NAVEGACIÓN BOTTOM BAR ---

        // Botón Texto (Ir a la pantalla de texto/principal)
        View btnNavTexto = findViewById(R.id.nav_texto);
        if (btnNavTexto != null) {
            btnNavTexto.setOnClickListener(v -> {
                // Al ya estar en MainActivity, simplemente limpiamos o hacemos scroll arriba
                btnClear.performClick();
                Toast.makeText(this, "Modo Texto", Toast.LENGTH_SHORT).show();
            });
        }

        // Botón Cámara (Redirigir a Texto por ahora)
        View btnNavCamara = findViewById(R.id.nav_camara);
        if (btnNavCamara != null) {
            btnNavCamara.setOnClickListener(v -> {
                // Como pides que lleve a la pantalla de texto, realizamos la misma acción
                btnClear.performClick();
                Toast.makeText(this, "Modo Cámara (Redirigido a Texto)", Toast.LENGTH_SHORT).show();
            });
        }

        // Botón Usuario
        View btnNavUsuario = findViewById(R.id.nav_usuario);
        if (btnNavUsuario != null) {
            btnNavUsuario.setOnClickListener(v -> {
                if (currentUserId != null && !currentUserId.trim().isEmpty()) {
                    // SI HAY USUARIO: Ir a Estadísticas
                    Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                    intent.putExtra("USER_ID", currentUserId);
                    startActivity(intent);
                } else {
                    // NO HAY USUARIO: Ir a Login
                    Toast.makeText(this, "Inicia sesión para ver tu historial", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }

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

        // Se incluye el currentUserId para el historial
        viewModel.translateText(text, sourceLang, targetLang, currentUserId);
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
                chip1.setText("Hola"); chip2.setText("¿Cómo estás?"); chip3.setText("Gracias");
                break;
            case "en":
                chip1.setText("Hello"); chip2.setText("How are you?"); chip3.setText("Thank you");
                break;
            case "fr":
                chip1.setText("Bonjour"); chip2.setText("Comment ça va?"); chip3.setText("Merci");
                break;
            case "de":
                chip1.setText("Hallo"); chip2.setText("Wie geht es dir?"); chip3.setText("Danke");
                break;
            default:
                chip1.setText("Hello"); chip2.setText("How are you?"); chip3.setText("Thank you");
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
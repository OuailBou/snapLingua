package com.example.snap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.snap.data.entities.User;
import com.example.snap.data.repository.UserRepository;
import com.example.snap.presentation.viewmodel.TranslationViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private TranslationViewModel viewModel;
    private UserRepository userRepository;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(TranslationViewModel.class);
        userRepository = new UserRepository(getApplication());
        executorService = Executors.newSingleThreadExecutor();

        // Configurar listeners
        setupListeners();
        setupBottomNavigation();
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Verificando...");

        // Verificar credenciales en segundo plano
        executorService.execute(() -> {
            User user = userRepository.login(email, password);
            
            runOnUiThread(() -> {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar Sesión");

                if (user != null) {
                    // Login exitoso
                    saveSession(email);
                    Toast.makeText(this, "Bienvenido, " + email, Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USER_ID", email);
                    intent.putExtra("FROM_LOGIN", true);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Credenciales incorrectas. ¿Deseas registrarte?", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void attemptRegister() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "La contraseña debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");

        // Verificar si el usuario ya existe y registrar si no existe
        executorService.execute(() -> {
            try {
                // Verificar en la BD de forma síncrona
                User existingUser = userRepository.getUserByEmailSync(email);
                
                if (existingUser != null) {
                    // El usuario ya existe
                    runOnUiThread(() -> {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Registrarse");
                        Toast.makeText(LoginActivity.this, "Este correo ya está registrado. Usa 'Iniciar Sesión'", Toast.LENGTH_LONG).show();
                    });
                } else {
                    // El usuario no existe, registrarlo
                    User newUser = new User(email, email.split("@")[0], password);
                    userRepository.registerSync(newUser);
                    
                    runOnUiThread(() -> {
                        saveSession(email);
                        Toast.makeText(LoginActivity.this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();
                        
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("USER_ID", email);
                        intent.putExtra("FROM_LOGIN", true);
                        startActivity(intent);
                        finish();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Registrarse");
                    Toast.makeText(LoginActivity.this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveSession(String email) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        prefs.edit().putString("active_email", email).apply();
        android.util.Log.d("LoginActivity", "Sesión guardada: " + email);
    }

    private void setupBottomNavigation() {
        View btnNavTexto = findViewById(R.id.nav_texto);
        View btnNavCamara = findViewById(R.id.nav_camara);
        View btnNavUsuario = findViewById(R.id.nav_usuario);

        if (btnNavTexto != null) {
            btnNavTexto.setOnClickListener(v -> {
                Toast.makeText(this, "Inicia sesión primero", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnNavCamara != null) {
            btnNavCamara.setOnClickListener(v -> {
                Toast.makeText(this, "Inicia sesión primero", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnNavUsuario != null) {
            btnNavUsuario.setOnClickListener(v -> {
                // Ya estamos en la pantalla de usuario/login
                Toast.makeText(this, "Ya estás en la pantalla de inicio de sesión", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

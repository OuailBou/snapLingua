package com.example.snap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.snap.presentation.viewmodel.TranslationViewModel;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private TranslationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        viewModel = new ViewModelProvider(this).get(TranslationViewModel.class);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            performSignIn(email);
        });

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String name = email.split("@")[0];

            if (email.isEmpty()) {
                Toast.makeText(this, "Introduce un email para registrarte", Toast.LENGTH_SHORT).show();
                return;
            }
            performSignUp(email, name);
        });
    }

    private void performSignIn(String email) {
        viewModel.getUserData(email).observe(this, user -> {
            if (user != null) {
                startMainActivity(email);
            } else {
                Toast.makeText(this, "Usuario no encontrado. ¡Regístrate!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSignUp(String email, String name) {
        String userId = email.replace(".", "_");

        viewModel.insertUser(userId, name, email,
                () -> {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    startMainActivity(userId);
                },
                () -> {
                    Toast.makeText(this, "Este correo ya está registrado", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void startMainActivity(String userId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }
}
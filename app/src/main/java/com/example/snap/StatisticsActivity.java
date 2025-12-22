package com.example.snap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snap.data.entities.Favorite;
import com.example.snap.data.entities.TranslationHistory;
import com.example.snap.presentation.viewmodel.TranslationViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvUserEmail, tvFavoriteLangs, tvSavedWords;
    private RecyclerView rvHistory;
    private Button btnLogout;
    private TranslationViewModel viewModel;
    private String currentUserId;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Obtener usuario actual
        currentUserId = getIntent().getStringExtra("USER_ID");
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        
        if (currentUserId == null) {
            currentUserId = prefs.getString("active_email", null);
        }

        // Si no hay usuario, redirigir a Login
        if (currentUserId == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Inicializar vistas
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvFavoriteLangs = findViewById(R.id.tvFavoriteLangs);
        tvSavedWords = findViewById(R.id.tvSavedWords);
        rvHistory = findViewById(R.id.rvHistory);
        btnLogout = findViewById(R.id.btnLogout);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(TranslationViewModel.class);

        // Configurar UI
        tvUserEmail.setText(currentUserId);

        // Configurar RecyclerView para historial
        historyAdapter = new HistoryAdapter(new ArrayList<>());
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);

        // Configurar listeners
        setupListeners();
        
        // Cargar datos
        loadStatistics();
        
        // Configurar navegación
        setupBottomNavigation();
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> {
            // Cerrar sesión
            SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
            prefs.edit().remove("active_email").apply();
            
            Toast.makeText(this, "Sesión cerrada - Modo Invitado", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(StatisticsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadStatistics() {
        // Cargar historial (últimas 10 traducciones)
        viewModel.getHistoryByUserId(currentUserId).observe(this, historyList -> {
            if (historyList != null && !historyList.isEmpty()) {
                // Limitar a las últimas 10
                List<TranslationHistory> last10 = historyList.size() > 10 
                    ? historyList.subList(0, 10) 
                    : historyList;
                historyAdapter.updateData(last10);
            } else {
                historyAdapter.updateData(new ArrayList<>());
            }
        });

        // Cargar favoritos
        viewModel.getFavoritesByUser(currentUserId).observe(this, favorites -> {
            if (favorites != null && !favorites.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Math.min(favorites.size(), 10); i++) {
                    Favorite fav = favorites.get(i);
                    sb.append("⭐ ")
                      .append(fav.getOriginalText())
                      .append(" → ")
                      .append(fav.getTranslatedText())
                      .append("\n");
                }
                tvSavedWords.setText(sb.toString().trim());
            } else {
                tvSavedWords.setText("No hay palabras guardadas.");
            }
        });

        // Cargar idiomas favoritos (más usados en historial)
        viewModel.getHistoryByUserId(currentUserId).observe(this, historyList -> {
            if (historyList != null && !historyList.isEmpty()) {
                Map<String, Integer> langCount = new HashMap<>();
                
                for (TranslationHistory history : historyList) {
                    String pair = history.getSourceLanguage() + " → " + history.getTargetLanguage();
                    langCount.put(pair, langCount.getOrDefault(pair, 0) + 1);
                }
                
                // Encontrar los 3 más usados
                List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(langCount.entrySet());
                sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Math.min(3, sortedList.size()); i++) {
                    Map.Entry<String, Integer> entry = sortedList.get(i);
                    sb.append(entry.getKey())
                      .append(" (")
                      .append(entry.getValue())
                      .append(" veces)\n");
                }
                
                tvFavoriteLangs.setText(sb.toString().isEmpty() 
                    ? "Aún no tienes traducciones" 
                    : sb.toString().trim());
            } else {
                tvFavoriteLangs.setText("Aún no tienes traducciones");
            }
        });
    }

    private void setupBottomNavigation() {
        View btnNavTexto = findViewById(R.id.nav_texto);
        View btnNavCamara = findViewById(R.id.nav_camara);
        View btnNavUsuario = findViewById(R.id.nav_usuario);

        if (btnNavTexto != null) {
            btnNavTexto.setOnClickListener(v -> {
                Intent intent = new Intent(StatisticsActivity.this, MainActivity.class);
                intent.putExtra("USER_ID", currentUserId);
                startActivity(intent);
                finish();
            });
        }

        if (btnNavCamara != null) {
            btnNavCamara.setOnClickListener(v -> {
                // Redirigir a pantalla de texto (MainActivity)
                Intent intent = new Intent(StatisticsActivity.this, MainActivity.class);
                intent.putExtra("USER_ID", currentUserId);
                startActivity(intent);
                finish();
            });
        }

        if (btnNavUsuario != null) {
            btnNavUsuario.setOnClickListener(v -> {
                // Ya estamos en estadísticas
                Toast.makeText(this, "Ya estás en tu perfil", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Adapter simple para el historial
    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        
        private List<TranslationHistory> historyList;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

        public HistoryAdapter(List<TranslationHistory> historyList) {
            this.historyList = historyList;
        }

        public void updateData(List<TranslationHistory> newList) {
            this.historyList = newList;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setPadding(16, 16, 16, 16);
            textView.setTextSize(14);
            textView.setBackgroundColor(0xFFFFFFFF);
            
            // Agregar margen inferior
            android.view.ViewGroup.MarginLayoutParams params = 
                new android.view.ViewGroup.MarginLayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                );
            params.setMargins(0, 0, 0, 8);
            textView.setLayoutParams(params);
            
            return new ViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TranslationHistory history = historyList.get(position);
            String date = dateFormat.format(new Date(history.getTimestamp()));
            String text = date + "\n" +
                         history.getSourceText() + " → " + history.getTranslatedText() +
                         "\n(" + history.getSourceLanguage() + " → " + history.getTargetLanguage() + ")";
            holder.textView.setText(text);
        }

        @Override
        public int getItemCount() {
            return historyList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(TextView textView) {
                super(textView);
                this.textView = textView;
            }
        }
    }
}

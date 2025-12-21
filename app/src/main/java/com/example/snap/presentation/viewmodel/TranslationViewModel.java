package com.example.snap.presentation.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.snap.data.entities.TranslationHistory;
import com.example.snap.data.repository.TranslationRepository;
import com.example.snap.services.NetworkTranslationService; // Importa tu servicio

import java.util.List;

public class TranslationViewModel extends AndroidViewModel {

    private TranslationRepository repository;
    private NetworkTranslationService networkService; // Instancia del servicio de red
    private LiveData<List<TranslationHistory>> allTranslations;
    private MutableLiveData<String> currentTranslation;

    public TranslationViewModel(Application application) {
        super(application);
        repository = new TranslationRepository(application);
        // INICIALIZAMOS EL SERVICIO DE RED
        networkService = new NetworkTranslationService(application);
        allTranslations = repository.getAllTranslations();
        currentTranslation = new MutableLiveData<>();
    }

    public LiveData<List<TranslationHistory>> getAllTranslations() {
        return allTranslations;
    }

    public LiveData<String> getCurrentTranslation() {
        return currentTranslation;
    }

    public void translateText(String text, String sourceLang, String targetLang) {
        // LLAMADA A LA API REAL
        networkService.translateText(text, sourceLang, targetLang, new NetworkTranslationService.TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                // Actualizamos la UI con el texto real de la API
                currentTranslation.postValue(translatedText);

                // Guardamos en el historial de Room
                saveToHistory(text, translatedText, sourceLang, targetLang);
            }

            @Override
            public void onError(String error) {
                // Si falla la red, mostramos el error
                currentTranslation.postValue("Error: " + error + " (modo offline)");
            }
        });
    }

    // ELIMINADOS LOS MÉTODOS SIMULATE... YA NO SON NECESARIOS

    private void saveToHistory(String sourceText, String translatedText,
                               String sourceLang, String targetLang) {
        TranslationHistory history = new TranslationHistory(
                "default_user",
                sourceText,
                translatedText,
                sourceLang,
                targetLang,
                "TEXT"
        );
        repository.insert(history);
    }

    public void deleteFromHistory(long id) {
        repository.deleteById(id);
    }

    public void clearAllHistory() {
        repository.deleteAll();
    }
}

package com.example.snap.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.snap.data.entities.Favorite;
import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert
    void insert(Favorite favorite);

    @Delete
    void delete(Favorite favorite);

    // Obtener favoritos solo del usuario activo
    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY addedDate DESC")
    LiveData<List<Favorite>> getAllFavoritesByUser(String userId);

    // Estadísticas de idiomas solo para el usuario activo
    @Query("SELECT targetLang FROM favorites WHERE userId = :userId GROUP BY targetLang ORDER BY COUNT(*) DESC LIMIT 3")
    LiveData<List<String>> getFavoriteLanguagesByUser(String userId);

    // Saber si una frase ya está en favoritos para ese usuario (para pintar la estrella de color)
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND originalText = :text LIMIT 1)")
    LiveData<Boolean> isFavorite(String userId, String text);

    // Borrar favoritos de un usuario (por si borra su cuenta)
    @Query("DELETE FROM favorites WHERE userId = :userId")
    void deleteByUser(String userId);
}

package com.example.snap.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.snap.data.entities.TranslationHistory;
import java.util.List;

@Dao
public interface TranslationHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TranslationHistory history);

    @Query("SELECT * FROM translation_history WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<TranslationHistory>> getAllHistoryByUser(String userId);

    @Query("DELETE FROM translation_history WHERE userId = :userId")
    void deleteHistoryByUser(String userId);

    @Delete
    void delete(TranslationHistory history);
}

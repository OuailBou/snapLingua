package com.example.snap.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.snap.data.dao.FavoriteDao;
import com.example.snap.data.dao.TranslationHistoryDao;
import com.example.snap.data.dao.UserDao;
import com.example.snap.data.entities.Favorite;
import com.example.snap.data.entities.TranslationHistory;
import com.example.snap.data.entities.User;

@Database(
        entities = {User.class, TranslationHistory.class, Favorite.class},
        // CAMBIO: Versión 3 para aplicar los cambios de User (password/email único)
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract TranslationHistoryDao translationHistoryDao();
    public abstract FavoriteDao favoriteDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "translation_db"
                            )
                            // Esto evitará el crash borrando los datos viejos
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

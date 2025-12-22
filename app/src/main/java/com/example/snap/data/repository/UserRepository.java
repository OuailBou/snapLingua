package com.example.snap.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.snap.data.database.AppDatabase;
import com.example.snap.data.dao.UserDao;
import com.example.snap.data.entities.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private UserDao userDao;
    private ExecutorService executorService;

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void register(User user) {
        executorService.execute(() -> userDao.register(user));
    }

    // Nota: El login debe ser síncrono o usar un callback para validar credenciales
    public User login(String email, String password) {
        return userDao.login(email, password);
    }
    public LiveData<User> getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }
}
package com.example.snap.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true)})
public class User {
    @PrimaryKey
    @NonNull
    private String id; // Email del usuario

    private String name;

    @NonNull
    private String email;

    private String password;
    private long createdAt = System.currentTimeMillis();

    // Usuario no registrado
    public User() {}

    // Usuario registrado
    public User(@NonNull String email, String name, String password) {
        this.id = email; // Usamos el email como identificador único
        this.email = email;
        this.name = name;
        this.password = password;
    }

    // Getters y Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @NonNull
    public String getEmail() { return email; }
    public void setEmail(@NonNull String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
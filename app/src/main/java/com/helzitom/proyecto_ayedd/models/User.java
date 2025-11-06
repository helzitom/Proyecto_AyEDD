package com.helzitom.proyecto_ayedd.models;

//Clase de usuario
public class User implements java.io.Serializable{
    private String userId;
    private String name;
    private String lastname;
    private String username;
    private String type;
    private String email;
    private long createdAt;
    private boolean isVerified;

    // Constructor vacío requerido por Firebase
    public User() {
    }

    // Constructor con parámetros
    public User(String name, String lastname, String username, String email, String type) {
        this.name = name;
        this.lastname = lastname;
        this.username = username;
        this.email = email;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
        this.isVerified = false;
    }

    // Getters y Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getFullName() {
        return name + " " + lastname;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", isVerified=" + isVerified +
                ", type=" + type +
                '}';
    }
}
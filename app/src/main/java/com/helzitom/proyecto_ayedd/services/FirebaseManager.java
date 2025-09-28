package com.helzitom.proyecto_ayedd.services;


import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isInitialized = false;

    private FirebaseManager() {
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (!isInitialized) {
            try {
                FirebaseApp.initializeApp(context);
                db = FirebaseFirestore.getInstance();
                auth = FirebaseAuth.getInstance();
                isInitialized = true;
                Log.d(TAG, "Firebase inicializado correctamente");
            } catch (Exception e) {
                Log.e(TAG, "Error inicializando Firebase", e);
            }
        }
    }

    public FirebaseFirestore getFirestore() {
        if (!isInitialized) {
            throw new IllegalStateException("FirebaseManager no ha sido inicializado. Llama initialize() primero.");
        }
        return db;
    }

    public FirebaseAuth getAuth() {
        if (!isInitialized) {
            throw new IllegalStateException("FirebaseManager no ha sido inicializado. Llama initialize() primero.");
        }
        return auth;
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}

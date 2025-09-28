package com.helzitom.proyecto_ayedd.services;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConnectionService {
    private static final String TAG = "ConnectionService";
    private FirebaseFirestore db;

    public ConnectionService() {
        this.db = FirebaseManager.getInstance().getFirestore();
    }

    public void testConnection() {
        if (db != null) {
            Log.d(TAG, "🔍 Verificando conexión");

            db.collection("test")
                    .limit(1)
                    .get()
                    .addOnSuccessListener(result -> {
                        Log.d(TAG, "Conexión exitosa");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error de conexión", e);
                    });
        } else {
            Log.e(TAG, "Firestore no disponible");
        }
    }
}
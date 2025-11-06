package com.helzitom.proyecto_ayedd.services;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.helzitom.proyecto_ayedd.models.User;

public class UserService {
    private static final String TAG = "UserService";
    private static final String COLLECTION_USERS = "usuarios"; // O "users" si usas ese nombre
    private FirebaseFirestore db;

    public UserService() {
        this.db = FirebaseManager.getInstance().getFirestore();
    }


    // ========== INTERFACES ==========

    public interface TipoUsuarioCallback {
        void onSuccess(String tipo);
        void onError(String error);
    }

    public interface UserCallback {
        void onSuccess(User usuario);
        void onError(String error);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }
}
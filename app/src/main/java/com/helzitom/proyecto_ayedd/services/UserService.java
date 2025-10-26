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

    // ========== OBTENER USUARIO ==========

    // Obtener tipo de usuario por UID
    public void obtenerTipoUsuario(String uid, TipoUsuarioCallback callback) {
        Log.d(TAG, "🔍 Obteniendo tipo de usuario: " + uid);

        db.collection(COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String type = documentSnapshot.getString("type");
                        if (type != null) {
                            Log.d(TAG, "✅ Tipo de usuario: " + type);
                            callback.onSuccess(type);
                        } else {
                            callback.onError("Tipo de usuario no definido");
                        }
                    } else {
                        Log.w(TAG, "⚠️ Usuario no encontrado en base de datos");
                        callback.onError("Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo tipo de usuario", e);
                    callback.onError("Error al obtener información del usuario");
                });
    }

    // Obtener usuario completo por UID
    public void obtenerUsuario(String uid, UserCallback callback) {
        Log.d(TAG, "🔍 Obteniendo usuario: " + uid);

        db.collection(COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User usuario = documentSnapshot.toObject(User.class);
                        if (usuario != null) {
                            Log.d(TAG, "✅ Usuario obtenido: " + usuario.getType());
                            callback.onSuccess(usuario);
                        } else {
                            callback.onError("Error al cargar datos del usuario");
                        }
                    } else {
                        Log.w(TAG, "⚠️ Usuario no encontrado en Firestore");
                        callback.onError("Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo usuario", e);
                    callback.onError("Error al obtener información del usuario");
                });
    }

    // ========== CREAR USUARIO ==========

    // Crear usuario en Firestore
    public void crearUsuario(User usuario, UserCallback callback) {
        Log.d(TAG, "📝 Creando usuario en Firestore: " + usuario.getEmail());

        db.collection(COLLECTION_USERS)
                .document(usuario.getUserId())
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Usuario creado en Firestore");
                    callback.onSuccess(usuario);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error creando usuario", e);
                    callback.onError("Error al crear perfil de usuario");
                });
    }

    // ========== ACTUALIZAR USUARIO ==========

    // Actualizar rol de usuario
    public void actualizarRol(String uid, String nuevoRol, UpdateCallback callback) {
        db.collection(COLLECTION_USERS)
                .document(uid)
                .update("type", nuevoRol)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Rol actualizado a: " + nuevoRol);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando rol", e);
                    callback.onError("Error al actualizar rol");
                });
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
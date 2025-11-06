package com.helzitom.proyecto_ayedd.services;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.helzitom.proyecto_ayedd.models.User;

import java.util.HashMap;
import java.util.Map;


//Servicio de autentificación
public class AuthService {
    private static final String TAG = "AuthService";
    private static final String COLLECTION_USERS = "users";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Constructor con parámetros (tu versión)
    public AuthService(FirebaseAuth auth, FirebaseFirestore firestore) {
        this.mAuth = auth;
        this.db = firestore;
    }

    // Constructor sin parámetros (para la versión con singleton)
    public AuthService() {
        this.mAuth = FirebaseManager.getInstance().getAuth();
        this.db = FirebaseManager.getInstance().getFirestore();
    }

    // ========== INTERFACES ==========

    public interface AuthCallback {
        void onSuccess(String userId);
        void onError(String error);
    }

    public interface LoginCallback {
        void onSuccess(String uid, String email, String tipo);
        void onError(String error);
    }

    public interface AvailabilityCallback {
        void onResult(boolean isAvailable);
    }

    public interface PasswordResetCallback {
        void onSuccess();
        void onError(String error);
    }

    // ========== VERIFICACIONES ==========

    // Verificar disponibilidad de username
    public void checkUsernameAvailability(String username, AvailabilityCallback callback) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        callback.onResult(snapshot.isEmpty());
                    } else {
                        Exception e = task.getException();
                        Log.e(TAG, "Error verificando username", e);
                        callback.onResult(true);
                    }
                });
    }

    // Verificar si hay sesión activa
    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    // ========== REGISTRO ==========

    // Registrar usuario completo (con User model)
    public void registerUser(User user, String password, AuthCallback callback) {
        Log.d(TAG, "📝 Registrando usuario: " + user.getEmail());

        // 1. Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        user.setUserId(userId);

                        // 2. Enviar email de verificación
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(verifyTask -> {
                                    // Continuar aunque falle el envío
                                    saveUserToFirestore(user, callback);
                                });
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "❌ Error en registro: " + error);
                        callback.onError(getErrorMessage(task.getException()));
                    }
                });
    }

    // Guardar datos del usuario en Firestore
    private void saveUserToFirestore(User user, AuthCallback callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUserId());
        userData.put("uid", user.getUserId()); // Ambos por compatibilidad
        userData.put("name", user.getName());
        userData.put("lastname", user.getLastname());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("type", user.getType());
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("isVerified", false);

        db.collection(COLLECTION_USERS)
                .document(user.getUserId())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Usuario guardado en Firestore");
                    callback.onSuccess(user.getUserId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al guardar datos", e);
                    callback.onError("Error al guardar datos: " + e.getMessage());
                });
    }

    // ========== LOGIN ==========

    // Login con email y contraseña (retorna tipo de usuario)
    public void loginUsuario(String email, String password, LoginCallback callback) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            callback.onError("Email y contraseña son obligatorios");
            return;
        }

        Log.d(TAG, "Iniciando sesión: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();

                        // Obtener tipo de usuario desde Firestore
                        obtenerTipoUsuario(uid, new UserService.TipoUsuarioCallback() {
                            @Override
                            public void onSuccess(String tipo) {
                                Log.d(TAG, "Login exitoso - Tipo: " + tipo);
                                callback.onSuccess(uid, email, tipo);
                            }

                            @Override
                            public void onError(String error) {
                                callback.onError(error);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error en inicio de sesión", e);
                    callback.onError(getErrorMessage(e));
                });
    }

    // Obtener tipo del usuario actual
    public void obtenerTipoUsuarioActual(UserService.TipoUsuarioCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            obtenerTipoUsuario(user.getUid(), callback);
        } else {
            callback.onError("No hay sesión activa");
        }
    }

    // Obtener tipo de usuario por UID
    private void obtenerTipoUsuario(String uid, UserService.TipoUsuarioCallback callback) {
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


    // ========== UTILIDADES ==========

    // Obtener usuario actual
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // Obtener UID del usuario actual
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Obtener email del usuario actual
    public String getCurrentUserEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    // Cerrar sesión
    public void logout() {
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "👋 Cerrando sesión");
            mAuth.signOut();
        }
    }

    // Convertir errores de Firebase a mensajes legibles
    private String getErrorMessage(Exception e) {
        if (e == null) return "Error desconocido";

        String message = e.getMessage();

        if (message.contains("email address is already in use")) {
            return "Este email ya está registrado";
        } else if (message.contains("password is invalid") || message.contains("INVALID_LOGIN_CREDENTIALS")) {
            return "Contraseña incorrecta";
        } else if (message.contains("no user record") || message.contains("user not found")) {
            return "Usuario no encontrado";
        } else if (message.contains("email address is badly formatted")) {
            return "Email inválido";
        } else if (message.contains("network error")) {
            return "Error de conexión. Verifica tu internet";
        } else if (message.contains("weak password")) {
            return "La contraseña debe tener al menos 6 caracteres";
        } else {
            return "Error: " + message;
        }
    }
}
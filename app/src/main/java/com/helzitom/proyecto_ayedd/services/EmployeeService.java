package com.helzitom.proyecto_ayedd.services;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.helzitom.proyecto_ayedd.models.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeService {
    private static final String TAG = "EmployeeService";
    private static final String COLLECTION_USERS = "users";
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public EmployeeService() {
        this.db = FirebaseManager.getInstance().getFirestore();
        this.auth = FirebaseManager.getInstance().getAuth();
    }

    // Obtener todos los empleados (delivery y receiver)
    public void obtenerTodosEmpleados(EmployeesListCallback callback) {
        Log.d(TAG, "🔍 Obteniendo todos los empleados");

        db.collection(COLLECTION_USERS)
                .whereIn("type", java.util.Arrays.asList("delivery", "receiver"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> employees = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User employee = document.toObject(User.class);

                        // 🔥 Asignar el ID del documento al campo userId
                        employee.setUserId(document.getId());

                        employees.add(employee);
                    }
                    Log.d(TAG, "✅ Empleados obtenidos: " + employees.size());
                    callback.onSuccess(employees);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error obteniendo empleados", e);
                    callback.onError(e.getMessage());
                });

    }

    // Crear empleado
    public void crearEmpleado(String email, String password, String name, String lastname,
                              String username, String type, CreateEmployeeCallback callback) {
        Log.d(TAG, "📝 Creando empleado: " + email);

        // Crear usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    // Crear documento en Firestore
                    Map<String, Object> employeeData = new HashMap<>();
                    employeeData.put("uid", uid);
                    employeeData.put("email", email);
                    employeeData.put("name", name);
                    employeeData.put("lastname", lastname);
                    employeeData.put("username", username);
                    employeeData.put("type", type);
                    employeeData.put("createdAt", new Date().getTime());
                    employeeData.put("isVerified", false);

                    db.collection(COLLECTION_USERS)
                            .document(uid)
                            .set(employeeData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ Empleado creado exitosamente");
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Error creando documento de empleado", e);
                                callback.onError("Error al guardar datos del empleado");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error creando usuario en Auth", e);
                    callback.onError(getErrorMessage(e));
                });
    }

    // Actualizar empleado
    public void actualizarEmpleado(String uid, String name, String lastname,
                                   String username, String type, UpdateCallback callback) {
        Log.d(TAG, "📝 Actualizando empleado: " + uid);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("lastname", lastname);
        updates.put("username", username);
        updates.put("type", type);

        db.collection(COLLECTION_USERS)
                .document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Empleado actualizado");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando empleado", e);
                    callback.onError(e.getMessage());
                });
    }

    // Eliminar empleado
    public void eliminarEmpleado(String uid, DeleteCallback callback) {
        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "❌ UID del empleado es nulo o vacío");
            callback.onError("UID inválido");
            return;
        }

        db.collection(COLLECTION_USERS)
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Empleado eliminado de Firestore");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error eliminando empleado", e);
                    callback.onError(e.getMessage());
                });
    }


    private String getErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message.contains("email address is already in use")) {
            return "Este email ya está registrado";
        } else if (message.contains("password")) {
            return "La contraseña debe tener al menos 6 caracteres";
        } else if (message.contains("email address is badly formatted")) {
            return "Email inválido";
        }
        return "Error: " + message;
    }

    public interface EmployeesListCallback {
        void onSuccess(List<User> employees);
        void onError(String error);
    }

    public interface CreateEmployeeCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }
}
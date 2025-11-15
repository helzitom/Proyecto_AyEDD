package com.helzitom.proyecto_ayedd.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.helzitom.proyecto_ayedd.models.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Servicio para el manejo de empleados en el sistema
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
    public void obtenerTodosEmpleados(final EmployeesListCallback callback) {
        Log.d(TAG, "🔍 Obteniendo todos los empleados");

        db.collection(COLLECTION_USERS)
                .whereIn("type", java.util.Arrays.asList("delivery", "receiver"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<User> employees = new ArrayList<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            User employee = document.toObject(User.class);

                            // Asignar el ID del documento al campo userId
                            employee.setUserId(document.getId());

                            employees.add(employee);
                        }

                        Log.d(TAG, "Empleados obtenidos: " + employees.size());
                        callback.onSuccess(employees);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error obteniendo empleados", e);
                        callback.onError(e.getMessage());
                    }
                });
    }


    // Método para crear empleado
    public void crearEmpleado(String email, String password, String name, String lastname,
                              String username, String type, final CreateEmployeeCallback callback) {

        Log.d(TAG, "📝 Creando empleado: " + email);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        String uid = authResult.getUser().getUid();

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
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "✅ Empleado creado exitosamente");
                                        callback.onSuccess();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Error creando documento de empleado", e);
                                        callback.onError("Error al guardar datos del empleado");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error creando usuario en Auth", e);
                        callback.onError(getErrorMessage(e));
                    }
                });
    }


    //Método para actualizar empleado
    public void actualizarEmpleado(String uid, String name, String lastname,
                                   String username, String type, UpdateCallback callback) {

        Log.d(TAG, "Actualizando empleado: " + uid);

        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("name", name);
        updates.put("lastname", lastname);
        updates.put("username", username);
        updates.put("type", type);

        db.collection(COLLECTION_USERS)
                .document(uid)
                .update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Empleado actualizado");
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error actualizando empleado", e);
                        callback.onError(e.getMessage());
                    }
                });
    }


    // Método para eliminar empleado
    public void eliminarEmpleado(String uid, DeleteCallback callback) {

        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "UID del empleado es nulo o vacío");
            callback.onError("UID inválido");
            return;
        }

        db.collection(COLLECTION_USERS)
                .document(uid)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Empleado eliminado de Firestore");
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error eliminando empleado", e);
                        callback.onError(e.getMessage());
                    }
                });
    }


    //Métrodos para obtener excepciones y convertirlas a español
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

    //Interfaces
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
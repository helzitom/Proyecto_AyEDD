package com.helzitom.proyecto_ayedd.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.helzitom.proyecto_ayedd.models.User;

import java.util.function.Consumer;

public class UserService {
    private static final String TAG = "UserService";
    private static final String COLLECTION_USERS = "usuarios";
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

    public void getUserById(String userId, @NonNull Consumer<User> onSuccess, @NonNull Consumer<Exception> onFailure) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            onSuccess.accept(user);
                        } else {
                            onFailure.accept(new Exception("Usuario no encontrado"));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onFailure.accept(e);
                    }
                });
    }


    /**
     * Asigna un pedido a un usuario si el código es válido.
     */
    public void asignarPedidoAlUsuario(String userId, String codigoPedido, @NonNull Runnable onSuccess, @NonNull Consumer<String> onError) {
        db.collection("pedidos")
                .whereEqualTo("codigoPedido", codigoPedido)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {

                        if (querySnapshot.isEmpty()) {
                            onError.accept("Código de pedido no válido");
                            return;
                        }

                        String documentId = querySnapshot.getDocuments().get(0).getId();
                        DocumentSnapshot pedidoDoc = querySnapshot.getDocuments().get(0);

                        String clienteIdActual = pedidoDoc.getString("clienteId");
                        if (clienteIdActual != null && !clienteIdActual.isEmpty()) {
                            onError.accept("⚠️ Este pedido ya está asignado a otro cliente");
                            return;
                        }

                        db.collection("pedidos").document(documentId)
                                .update("clienteId", userId, "fechaAsignacion", new java.util.Date())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        onSuccess.run();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("UserService", "Error al asignar pedido", e);
                                        onError.accept("Error al asignar pedido");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("UserService", "Error al consultar pedido", e);
                        onError.accept("Error al consultar pedido");
                    }
                });
    }

}
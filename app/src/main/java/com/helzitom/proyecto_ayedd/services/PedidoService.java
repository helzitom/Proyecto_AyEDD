package com.helzitom.proyecto_ayedd.services;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.helzitom.proyecto_ayedd.adapters.PedidosAdapter;
import com.helzitom.proyecto_ayedd.models.Pedido;
import com.helzitom.proyecto_ayedd.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class PedidoService {
    private static final String TAG = "PedidoService";
    private static final String COLLECTION_PEDIDOS = "pedidos";
    private FirebaseFirestore db;

    public List<Pedido> pedidoList;

    private PedidosAdapter adapter;
    private Context context;

    //Constructor del pedido
    public PedidoService(Context context, List<Pedido> pedidoList, PedidosAdapter adapter) {
        this.context = context;
        this.pedidoList = pedidoList;
        this.adapter = adapter;
        this.db = FirebaseFirestore.getInstance();
    }
    public PedidoService() {
        this.db = FirebaseManager.getInstance().getFirestore();
    }

    //Método para crear pedido
    public void crearPedido(final Pedido pedido, final PedidoCallback callback) {
        Log.d(TAG, "📝 Creando pedido");

        pedido.setEstado("pendiente");
        pedido.setFechaCreacion(new Date());
        pedido.setEnRuta(false);

        Map<String, Object> pedidoData = pedidoToMap(pedido);

        db.collection(COLLECTION_PEDIDOS)
                .add(pedidoData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String pedidoId = documentReference.getId();
                        pedido.setId(pedidoId);
                        Log.d(TAG, "Pedido creado: " + pedidoId);
                        callback.onSuccess(pedidoId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error creando pedido", e);
                        callback.onError(e.getMessage());
                    }
                });
    }


    // Método usado al iniciar la ruta

    public void iniciarRuta(String pedidoId, double latInicial, double lngInicial, final UpdateCallback callback) {
        Log.d(TAG, "🚀 Iniciando ruta del pedido: " + pedidoId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "en_camino");
        updates.put("enRuta", true);
        updates.put("fechaInicio", new Date());
        updates.put("latitudRepartidor", latInicial);
        updates.put("longitudRepartidor", lngInicial);
        updates.put("ultimaActualizacionUbicacion", new Date());

        db.collection(COLLECTION_PEDIDOS).document(pedidoId)
                .update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Ruta iniciada");
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error iniciando ruta", e);
                        callback.onError(e.getMessage());
                    }
                });
    }


    // Método para actualizar la ubicación del repartidor

    public void actualizarUbicacionRepartidor(String pedidoId, double lat, double lng, final UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("latitudRepartidor", lat);
        updates.put("longitudRepartidor", lng);
        updates.put("ultimaActualizacionUbicacion", new Date());

        db.collection(COLLECTION_PEDIDOS).document(pedidoId)
                .update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error actualizando ubicación", e);
                        if (callback != null) {
                            callback.onError(e.getMessage());
                        }
                    }
                });
    }


    // Método para cambiar el estado del pedido a entregado

    public void marcarComoEntregado(String pedidoId, final UpdateCallback callback) {
        Log.d(TAG, "✅ Marcando pedido como entregado: " + pedidoId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "entregado");
        updates.put("enRuta", false);
        updates.put("fechaEntrega", new Date());

        db.collection(COLLECTION_PEDIDOS).document(pedidoId)
                .update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Pedido marcado como entregado");
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error marcando como entregado", e);
                        callback.onError(e.getMessage());
                    }
                });
    }


    // Escuchar el pedido en tiempo real

    public ListenerRegistration escucharPedido(String pedidoId, final PedidoRealtimeCallback callback) {
        Log.d(TAG, "👂 Escuchando pedido en tiempo real: " + pedidoId);

        return db.collection(COLLECTION_PEDIDOS).document(pedidoId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                        if (error != null) {
                            Log.e(TAG, "Error escuchando pedido", error);
                            callback.onError(error.getMessage());
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Pedido pedido = snapshot.toObject(Pedido.class);
                            if (pedido != null) {
                                pedido.setId(snapshot.getId());
                                callback.onPedidoChanged(pedido);
                            }
                        }
                    }
                });
    }


    // Método para obtenrer todos los pedidos

    public void obtenerTodosPedidos(final PedidosListCallback callback) {
        Log.d(TAG, "🔍 Obteniendo todos los pedidos");

        db.collection("pedidos")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        List<Pedido> pedidos = new ArrayList<>();

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            // Datos del pedido
                            Map<String, Object> data = document.getData();
                            Log.d(TAG, "====================================");
                            Log.d(TAG, "📄 ID Documento: " + document.getId());
                            Log.d(TAG, "📋 TODOS los campos: " + data);

                            // Verificar específicamente el código
                            Object codigoValue = document.get("codigoPedido");
                            Log.d(TAG, "🔍 Campo 'codigoPedido' existe: " + (codigoValue != null));
                            Log.d(TAG, "🔍 Valor 'codigoPedido': " + codigoValue);

                            // Mapear a objeto
                            Pedido pedido = document.toObject(Pedido.class);
                            pedido.setId(document.getId());

                            Log.d(TAG, "✅ Después de mapear:");
                            Log.d(TAG, "   ID: " + pedido.getId());
                            Log.d(TAG, "   CodigoPedido: " + pedido.getcodigoPedido());
                            Log.d(TAG, "   Cliente: " + pedido.getClienteNombre());
                            Log.d(TAG, "====================================");

                            pedidos.add(pedido);
                        }

                        callback.onSuccess(pedidos);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "❌ Error: " + e.getMessage());
                        callback.onError(e.getMessage());
                    }
                });
    }


    //Método para obtener pedidos segun el repartidor asignado
    public void obtenerPedidosPorRepartidor(String repartidorId, String estado, final PedidosListCallback callback) {
        db.collection(COLLECTION_PEDIDOS)
                .whereEqualTo("repartidorId", repartidorId)
                .whereEqualTo("estado", estado)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Pedido> pedidos = new ArrayList<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Pedido pedido = document.toObject(Pedido.class);
                            pedido.setId(document.getId());
                            pedidos.add(pedido);
                        }

                        callback.onSuccess(pedidos);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }


    //Método para escuchar todos los pedidos en tiempo real
    public void escucharPedidosTiempoReal() {
        db.collection("pedidos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("PedidoService", "Error al escuchar pedidos", e);
                            return;
                        }

                        if (snapshots != null) {
                            pedidoList.clear();
                            for (QueryDocumentSnapshot doc : snapshots) {
                                Pedido pedido = doc.toObject(Pedido.class);
                                pedidoList.add(pedido);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }


    // ========== UTILIDADES ==========

    private Map<String, Object> pedidoToMap(Pedido pedido) {
        Map<String, Object> map = new HashMap<>();
        map.put("clienteId", pedido.getClienteId());
        map.put("clienteNombre", pedido.getClienteNombre());
        map.put("clienteTelefono", pedido.getClienteTelefono());
        map.put("clienteDireccion", pedido.getClienteDireccion());
        map.put("estado", pedido.getEstado());
        map.put("latitudDestino", pedido.getLatitudDestino());
        map.put("longitudDestino", pedido.getLongitudDestino());
        map.put("direccionDestino", pedido.getDireccionDestino());
        map.put("latitudOrigen", pedido.getLatitudOrigen());
        map.put("longitudOrigen", pedido.getLongitudOrigen());
        map.put("direccionOrigen", pedido.getDireccionOrigen());
        map.put("items", pedido.getItems());
        map.put("total", pedido.getTotal());
        map.put("subtotal", pedido.getSubtotal());
        map.put("costoDelivery", pedido.getCostoDelivery());
        map.put("fechaCreacion", pedido.getFechaCreacion());
        map.put("notas", pedido.getNotas());
        map.put("enRuta", pedido.isEnRuta());
        map.put("repartidorId", pedido.getRepartidorId());
        map.put("repartidorNombre", pedido.getRepartidorNombre());
        String codigo = generarCodigoVerificacion();
        map.put("codigoVerificacion", codigo);
        String idPedido = Utils.generarPedidoIdCorto(6);
        map.put("codigoPedido", idPedido);
        return map;

    }



    // ========== INTERFACES ==========

    public interface PedidoCallback {
        void onSuccess(String pedidoId);

        void onError(String error);
    }

    public interface UpdateCallback {
        void onSuccess();

        void onError(String error);
    }

    //Callbacks para ser ejecutados luego de que se completen los procesos
    public interface PedidosListCallback {
        void onSuccess(List<Pedido> pedidos);
        void onError(String error);
    }

    public interface PedidoRealtimeCallback {
        void onPedidoChanged(Pedido pedido);

        void onError(String error);
    }

    //Método para
    private String generarCodigoVerificacion() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int index = (int) (Math.random() * caracteres.length());
            codigo.append(caracteres.charAt(index));
        }
        return codigo.toString();
    }
}
package com.helzitom.proyecto_ayedd.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.ListenerRegistration;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.models.Pedido;
import com.helzitom.proyecto_ayedd.services.PedidoService;

public class TrackOrderActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "TrackOrderActivity";
    public static final String EXTRA_PEDIDO_ID = "pedido_id";

    private GoogleMap mMap;
    private PedidoService pedidoService;
    private ListenerRegistration pedidoListener;
    private String pedidoId;

    private Marker markerOrigen;
    private Marker markerDestino;
    private Marker markerRepartidor;

    // Vistas
    private TextView tvEstado, tvRepartidor, tvDireccion, tvcodigoVerificacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_order);

        pedidoId = getIntent().getStringExtra(EXTRA_PEDIDO_ID);
        if (pedidoId == null) {
            Toast.makeText(this, "Error: ID de pedido no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pedidoService = new PedidoService();

        initViews();
        setupMap();
    }

    private void initViews() {
        tvEstado = findViewById(R.id.tv_track_estado);
        tvRepartidor = findViewById(R.id.tv_track_repartidor);
        tvDireccion = findViewById(R.id.tv_track_direccion);
        tvcodigoVerificacion = findViewById(R.id.tv_codigo_verificacion);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_track_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Escuchar cambios del pedido en tiempo real
        startListeningToPedido();
    }

    private void startListeningToPedido() {
        Log.d(TAG, "👂 Iniciando escucha en tiempo real del pedido: " + pedidoId);

        pedidoListener = pedidoService.escucharPedido(pedidoId, new PedidoService.PedidoRealtimeCallback() {
            @Override
            public void onPedidoChanged(Pedido pedido) {
                Log.d(TAG, "📍 Pedido actualizado - Estado: " + pedido.getEstado());
                Log.d(TAG, "📍 Lat Repartidor: " + pedido.getLatitudRepartidor() + ", Lng: " + pedido.getLongitudRepartidor());
                updateUI(pedido);
                updateMap(pedido);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error: " + error);
                Toast.makeText(TrackOrderActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Pedido pedido) {
        // Actualizar información
        tvEstado.setText(getEstadoText(pedido.getEstado()));
        tvDireccion.setText("📍 " + pedido.getDireccionDestino());

        if (pedido.getRepartidorNombre() != null) {
            tvRepartidor.setText("🚚 " + pedido.getRepartidorNombre());
        } else {
            tvRepartidor.setText("🚚 Sin asignar");
        }

        tvcodigoVerificacion.setText("💨 Codigo de Verificación: " + pedido.getCodigoVerificacion());
    }

    private void updateMap(Pedido pedido) {
        if (mMap == null) return;

        mMap.clear();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        // Marcador de origen (tienda)
        LatLng origen = new LatLng(pedido.getLatitudOrigen(), pedido.getLongitudOrigen());
        markerOrigen = mMap.addMarker(new MarkerOptions()
                .position(origen)
                .title("Origen - " + pedido.getDireccionOrigen())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        boundsBuilder.include(origen);

        // Marcador de destino
        LatLng destino = new LatLng(pedido.getLatitudDestino(), pedido.getLongitudDestino());
        markerDestino = mMap.addMarker(new MarkerOptions()
                .position(destino)
                .title("Destino - " + pedido.getDireccionDestino())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        boundsBuilder.include(destino);

        // CORREGIDO: Verificar coordenadas del repartidor independientemente del estado
        // Las coordenadas válidas deben ser diferentes de 0
        double latRepartidor = pedido.getLatitudRepartidor();
        double lngRepartidor = pedido.getLongitudRepartidor();

        Log.d(TAG, "🔍 Verificando repartidor - Lat: " + latRepartidor + ", Lng: " + lngRepartidor);

        if (latRepartidor != 0 && lngRepartidor != 0) {
            LatLng posicionRepartidor = new LatLng(latRepartidor, lngRepartidor);

            // Crear o actualizar marcador del repartidor
            markerRepartidor = mMap.addMarker(new MarkerOptions()
                    .position(posicionRepartidor)
                    .title("Repartidor - " + (pedido.getRepartidorNombre() != null ? pedido.getRepartidorNombre() : "En camino"))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            boundsBuilder.include(posicionRepartidor);

            Log.d(TAG, "✅ Marcador del repartidor añadido en: " + posicionRepartidor);

            // Dibujar línea de ruta desde repartidor hasta destino
            mMap.addPolyline(new PolylineOptions()
                    .add(posicionRepartidor, destino)
                    .width(8)
                    .color(0xFF4CAF50)
                    .geodesic(true));
        } else {
            Log.w(TAG, "⚠️ Coordenadas del repartidor inválidas o no disponibles");
        }

        // Ajustar cámara para mostrar todos los marcadores
        try {
            LatLngBounds bounds = boundsBuilder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        } catch (Exception e) {
            Log.e(TAG, "Error ajustando cámara", e);
            // Fallback: centrar en el destino
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destino, 14));
        }
    }

    private String getEstadoText(String estado) {
        switch (estado) {
            case "pendiente": return "⏳ Pendiente";
            case "asignado": return "✅ Asignado";
            case "en_camino": return "🚚 En Camino";
            case "entregado": return "✅ Entregado";
            case "cancelado": return "❌ Cancelado";
            default: return estado;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener escucha
        if (pedidoListener != null) {
            pedidoListener.remove();
        }
    }
}
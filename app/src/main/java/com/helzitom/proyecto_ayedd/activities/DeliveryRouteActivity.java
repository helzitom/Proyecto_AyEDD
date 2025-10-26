package com.helzitom.proyecto_ayedd.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.models.Pedido;
import com.helzitom.proyecto_ayedd.services.PedidoService;

public class DeliveryRouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "DeliveryRouteActivity";
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    public static final String EXTRA_PEDIDO = "pedido";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private PedidoService pedidoService;
    private Pedido pedido;

    private Marker markerDestino;
    private Marker markerRepartidor;
    private boolean rutaIniciada = false;

    // Vistas
    private TextView tvClienteNombre, tvDireccionDestino, tvEstadoRuta;
    private Button btnIniciarRuta, btnMarcarEntregado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_route);

        pedido = (Pedido) getIntent().getSerializableExtra(EXTRA_PEDIDO);
        if (pedido == null) {
            Toast.makeText(this, "Error: Pedido no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pedidoService = new PedidoService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupMap();
        setupLocationCallback();
        setupListeners();
        updateUI();
    }

    private void initViews() {
        tvClienteNombre = findViewById(R.id.tv_delivery_cliente_nombre);
        tvDireccionDestino = findViewById(R.id.tv_delivery_direccion);
        tvEstadoRuta = findViewById(R.id.tv_delivery_estado_ruta);
        btnIniciarRuta = findViewById(R.id.btn_iniciar_ruta);
        btnMarcarEntregado = findViewById(R.id.btn_marcar_entregado);

        btnMarcarEntregado.setEnabled(false);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_delivery_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Solicitar permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            enableMyLocation();
        }

        // Mostrar destino
        LatLng destino = new LatLng(pedido.getLatitudDestino(), pedido.getLongitudDestino());
        markerDestino = mMap.addMarker(new MarkerOptions()
                .position(destino)
                .title("Destino")
                .snippet(pedido.getDireccionDestino())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 15));
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // Obtener ubicación actual
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            markerRepartidor = mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("Tu ubicación")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                            // Ajustar cámara para mostrar ambos puntos
                            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                            boundsBuilder.include(currentLocation);
                            boundsBuilder.include(new LatLng(pedido.getLatitudDestino(), pedido.getLongitudDestino()));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
                        }
                    });
        }
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null || !rutaIniciada) {
                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                    // Actualizar marcador en el mapa
                    if (markerRepartidor != null) {
                        markerRepartidor.setPosition(currentPosition);
                    }

                    // Actualizar en Firebase
                    pedidoService.actualizarUbicacionRepartidor(
                            pedido.getId(),
                            location.getLatitude(),
                            location.getLongitude(),
                            null // Callback opcional
                    );

                    // Dibujar línea de ruta
                    mMap.clear();
                    LatLng destino = new LatLng(pedido.getLatitudDestino(), pedido.getLongitudDestino());

                    markerDestino = mMap.addMarker(new MarkerOptions()
                            .position(destino)
                            .title("Destino")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    markerRepartidor = mMap.addMarker(new MarkerOptions()
                            .position(currentPosition)
                            .title("Tu ubicación")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    mMap.addPolyline(new PolylineOptions()
                            .add(currentPosition, destino)
                            .width(5)
                            .color(0xFF4CAF50)
                            .geodesic(true));
                }
            }
        };
    }

    private void setupListeners() {
        btnIniciarRuta.setOnClickListener(v -> iniciarRuta());
        btnMarcarEntregado.setOnClickListener(v -> marcarComoEntregado());
    }

    private void updateUI() {
        tvClienteNombre.setText("Cliente: " + pedido.getClienteNombre());
        tvDireccionDestino.setText("📍 " + pedido.getDireccionDestino());
        tvEstadoRuta.setText("Estado: " + pedido.getEstado());
    }

    private void iniciarRuta() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Se requieren permisos de ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Iniciar ruta en Firebase
                        pedidoService.iniciarRuta(pedido.getId(),
                                location.getLatitude(),
                                location.getLongitude(),
                                new PedidoService.UpdateCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "✅ Ruta iniciada");
                                        rutaIniciada = true;
                                        btnIniciarRuta.setEnabled(false);
                                        btnIniciarRuta.setText("Ruta en Progreso");
                                        btnMarcarEntregado.setEnabled(true);
                                        tvEstadoRuta.setText("Estado: En Camino 🚚");

                                        // Iniciar actualización de ubicación en tiempo real
                                        startLocationUpdates();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Toast.makeText(DeliveryRouteActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(DeliveryRouteActivity.this, "No se pudo obtener tu ubicación", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Actualizar cada 5 segundos
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void marcarComoEntregado() {
        btnMarcarEntregado.setEnabled(false);
        btnMarcarEntregado.setText("Marcando...");

        pedidoService.marcarComoEntregado(pedido.getId(), new PedidoService.UpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(DeliveryRouteActivity.this, "Pedido entregado ✅", Toast.LENGTH_SHORT).show();
                stopLocationUpdates();
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DeliveryRouteActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                btnMarcarEntregado.setEnabled(true);
                btnMarcarEntregado.setText("Marcar como Entregado");
            }
        });
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }
}
package com.helzitom.proyecto_ayedd.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.models.ItemPedido;
import com.helzitom.proyecto_ayedd.models.Pedido;
import com.helzitom.proyecto_ayedd.services.PedidoService;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateOrderActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "CreateOrderActivity";
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    // Ubicación por defecto (Lima, Perú)
    private static final LatLng DEFAULT_LOCATION = new LatLng(-12.0464, -77.0428);
    private static final LatLng TIENDA_LOCATION = new LatLng(-12.0464, -77.0428);

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PedidoService pedidoService;
    private FirebaseFirestore db;

    private LatLng selectedLocation;

    // Vistas
    private TextInputEditText etClienteNombre, etClienteTelefono, etClienteDireccion;
    private TextInputEditText etNotas, etTotal;
    private Button btnSelectLocation, btnCreateOrder;
    private android.widget.Spinner spinnerRepartidor;

    // Repartidores
    private List<String> listaRepartidores = new ArrayList<>();
    private List<String> listaRepartidorUID = new ArrayList<>();
    private ArrayAdapter<String> adapterRepartidor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        db = FirebaseFirestore.getInstance();
        pedidoService = new PedidoService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupMap();
        setupListeners();
        cargarRepartidoresDesdeFirebase();
    }

    private void initViews() {
        etClienteNombre = findViewById(R.id.et_cliente_nombre);
        etClienteTelefono = findViewById(R.id.et_cliente_telefono);
        etClienteDireccion = findViewById(R.id.et_cliente_direccion);
        etNotas = findViewById(R.id.et_notas);
        etTotal = findViewById(R.id.et_total);
        btnSelectLocation = findViewById(R.id.btn_select_location);
        btnCreateOrder = findViewById(R.id.btn_create_order);
        spinnerRepartidor = findViewById(R.id.spinner_repartidor);

        // Configurar adaptador del spinner
        adapterRepartidor = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaRepartidores);
        adapterRepartidor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepartidor.setAdapter(adapterRepartidor);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            enableMyLocation();
        }

        // Cámara inicial
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 12));

        // Detectar clic en mapa
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación de entrega"));
            getAddressFromLocation(latLng);
        });
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    });
        }
    }

    private void getAddressFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                etClienteDireccion.setText(fullAddress);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error obteniendo dirección", e);
        }
    }

    private void setupListeners() {
        btnSelectLocation.setOnClickListener(v ->
                Toast.makeText(this, "📍 Toca el mapa para seleccionar la ubicación de entrega", Toast.LENGTH_LONG).show()
        );

        btnCreateOrder.setOnClickListener(v -> createOrder());
    }

    private void cargarRepartidoresDesdeFirebase() {
        db.collection("users")
                .whereEqualTo("type", "delivery")
                .get()
                .addOnSuccessListener(query -> {
                    listaRepartidores.clear();
                    listaRepartidorUID.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        String nombre = doc.getString("name");
                        String uid = doc.getId();
                        if (nombre != null) {
                            listaRepartidores.add(nombre);
                            listaRepartidorUID.add(uid);
                        }
                    }
                    adapterRepartidor.notifyDataSetChanged();

                    // Mostrar mensaje si no hay repartidores
                    if (listaRepartidores.isEmpty()) {
                        Toast.makeText(this, "No hay repartidores disponibles", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar repartidores: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar repartidores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createOrder() {
        String nombre = etClienteNombre.getText().toString().trim();
        String telefono = etClienteTelefono.getText().toString().trim();
        String direccion = etClienteDireccion.getText().toString().trim();
        String totalStr = etTotal.getText().toString().trim();
        String notas = etNotas.getText().toString().trim();

        // Validaciones
        if (nombre.isEmpty() || telefono.isEmpty() || direccion.isEmpty() || totalStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLocation == null) {
            Toast.makeText(this, "Selecciona una ubicación en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (listaRepartidores.isEmpty()) {
            Toast.makeText(this, "No hay repartidores disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato de teléfono
        if (!telefono.matches("\\d{9,15}")) {
            Toast.makeText(this, "Ingresa un número de teléfono válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato de total
        double total;
        try {
            total = Double.parseDouble(totalStr);
            if (total <= 0) {
                Toast.makeText(this, "El total debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingresa un total válido", Toast.LENGTH_SHORT).show();
            return;
        }

        int indiceSeleccionado = spinnerRepartidor.getSelectedItemPosition();
        String repartidorNombre = listaRepartidores.get(indiceSeleccionado);
        String repartidorUID = listaRepartidorUID.get(indiceSeleccionado);

        double costoDelivery = 5.50;
        double subtotal = total - costoDelivery;

        // Validar que el subtotal no sea negativo
        if (subtotal < 0) {
            Toast.makeText(this, "El total debe ser mayor al costo de delivery (S/ " + costoDelivery + ")", Toast.LENGTH_LONG).show();
            return;
        }

        // Crear objeto Pedido
        Pedido pedido = new Pedido();
        pedido.setClienteNombre(nombre);
        pedido.setClienteTelefono(telefono);
        pedido.setClienteDireccion(direccion);
        pedido.setDireccionDestino(direccion);
        pedido.setLatitudDestino(selectedLocation.latitude);
        pedido.setLongitudDestino(selectedLocation.longitude);
        pedido.setLatitudOrigen(TIENDA_LOCATION.latitude);
        pedido.setLongitudOrigen(TIENDA_LOCATION.longitude);
        pedido.setDireccionOrigen("Tienda Principal");
        pedido.setSubtotal(subtotal);
        pedido.setCostoDelivery(costoDelivery);
        pedido.setTotal(total);
        pedido.setNotas(notas);
        pedido.setEstado("asignado");
        pedido.setRepartidorNombre(repartidorNombre);
        pedido.setRepartidorId(repartidorUID);

        // Crear items del pedido
        List<ItemPedido> items = new ArrayList<>();
        items.add(new ItemPedido("Producto", 1, subtotal, ""));
        pedido.setItems(items);

        // Deshabilitar botón para evitar múltiples clics
        btnCreateOrder.setEnabled(false);
        btnCreateOrder.setText("Creando...");

        // Crear pedido en Firebase
        pedidoService.crearPedido(pedido, new PedidoService.PedidoCallback() {
            @Override
            public void onSuccess(String pedidoId) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateOrderActivity.this, "✅ Pedido creado exitosamente", Toast.LENGTH_SHORT).show();

                    // 🔹 IMPORTANTE: Establecer resultado de éxito antes de cerrar
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error al crear pedido: " + error);
                    Toast.makeText(CreateOrderActivity.this, "❌ Error al crear pedido: " + error, Toast.LENGTH_LONG).show();

                    // Rehabilitar botón
                    btnCreateOrder.setEnabled(true);
                    btnCreateOrder.setText("Crear Pedido");

                    // 🔹 Establecer resultado de error
                    setResult(RESULT_CANCELED);
                });
            }
        });
    }

    // 🔹 Manejar el botón de retroceso
    @Override
    public void onBackPressed() {
        // Establecer resultado cancelado cuando el usuario presiona back
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Se necesitan permisos de ubicación para seleccionar la ubicación de entrega", Toast.LENGTH_LONG).show();
            }
        }
    }
}


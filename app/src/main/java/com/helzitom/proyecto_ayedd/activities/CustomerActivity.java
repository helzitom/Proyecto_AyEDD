package com.helzitom.proyecto_ayedd.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.adapters.CustomerPedidosAdapter;
import com.helzitom.proyecto_ayedd.models.Pedido;
import com.helzitom.proyecto_ayedd.models.User;
import com.helzitom.proyecto_ayedd.services.AuthService;
import com.helzitom.proyecto_ayedd.services.PedidoService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//Clase principal de la actividad del cliente
public class CustomerActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fabLogout;
    private RecyclerView rvPedidos;
    private CustomerPedidosAdapter adapter;
    private List<Pedido> pedidoList;
    private PedidoService pedidoService;
    private AuthService authService;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tv_customer_name;
    private Button btnNewOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        // 🔹 Inicialización de servicios y Firebase
        pedidoService = new PedidoService();
        authService = new AuthService();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 🔹 Referencias UI
        toolbar = findViewById(R.id.toolbar);
        fabLogout = findViewById(R.id.fab_logout);
        rvPedidos = findViewById(R.id.rv_customer_orders);
        tv_customer_name = findViewById(R.id.tv_customer_name);
        btnNewOrder = findViewById(R.id.btn_new_order);

        btnNewOrder.setOnClickListener(v -> mostrarDialogoNuevoPedido());

        // 🔹 Configurar Toolbar
        setSupportActionBar(toolbar);

        // 🔹 Ajuste de márgenes por notch
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Inicializar lista y RecyclerView
        pedidoList = new ArrayList<>();
        setupRecyclerView();

        // 🔹 Cargar datos del usuario (incluye cambio de color)
        cargarDatosUsuario();

        // 🔹 Escuchar pedidos en tiempo real
        escucharPedidosEnTiempoReal();

        // 🔹 Botón logout
        fabLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(CustomerActivity.this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(CustomerActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // ========================
    // 🔹 CARGAR DATOS DEL USUARIO
    // ========================
    private void cargarDatosUsuario() {
        String userId = authService.getCurrentUserId();

        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            updateUI(user);
                            // Cambiar color según el tipo de usuario desde Firebase
                            cambiarColorToolbar(user.getType());
                        }
                    } else {
                        Log.e("CustomerActivity", "Usuario no encontrado en Firestore");
                        Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CustomerActivity", "Error al cargar usuario", e);
                    Toast.makeText(this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
                });
    }

    // ========================
    // 🔹 ACTUALIZAR UI CON DATOS DEL USUARIO
    // ========================
    private void updateUI(User user) {
        if (tv_customer_name != null && user != null) {
            String nombre = user.getName() != null ? user.getName() : "Usuario";
            tv_customer_name.setText("👤 " + nombre);
        }
    }

    // ========================
    // 🔹 CONFIGURAR RECYCLER
    // ========================
    private void setupRecyclerView() {
        adapter = new CustomerPedidosAdapter(pedidoList, this, this::onVerEnTiempoReal);
        rvPedidos.setLayoutManager(new LinearLayoutManager(this));
        rvPedidos.setAdapter(adapter);
    }

    // ========================
    // 🔹 PEDIDOS EN TIEMPO REAL
    // ========================
    private void escucharPedidosEnTiempoReal() {
        String userId = authService.getCurrentUserId();
        db.collection("pedidos")
                .whereEqualTo("clienteId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("CustomerActivity", "Error escuchando pedidos", error);
                        return;
                    }
                    if (value != null) {
                        pedidoList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Pedido pedido = doc.toObject(Pedido.class);
                            pedido.setId(doc.getId());
                            pedidoList.add(pedido);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void onVerEnTiempoReal(Pedido pedido) {
        Intent intent = new Intent(this, TrackOrderActivity.class);
        intent.putExtra(TrackOrderActivity.EXTRA_PEDIDO_ID, pedido.getId());
        startActivity(intent);
    }

    // ========================
    // 🔹 CAMBIAR COLOR DEL TOOLBAR Y BOTÓN
    // ========================
    private void cambiarColorToolbar(String tipoUsuario) {
        if (tipoUsuario == null) {
            // Color por defecto si no hay tipo de usuario
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.main));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.mainDark));
            btnNewOrder.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.main)));
            return;
        }

        int colorResId;
        int statusBarColorResId;

        switch (tipoUsuario.toLowerCase()) {
            case "admin":
                colorResId = R.color.admin_color;
                statusBarColorResId = R.color.admin_color;
                break;
            case "customer":
            case "cliente":
                colorResId = R.color.customer_color;
                statusBarColorResId = R.color.customer_color;
                break;
            case "delivery":
                colorResId = R.color.delivery_color;
                statusBarColorResId = R.color.delivery_color;
                break;
            case "receiver":
                colorResId = R.color.receiver_color;
                statusBarColorResId = R.color.receiver_color;
                break;
            default:
                colorResId = R.color.main;
                statusBarColorResId = R.color.mainDark;
                break;
        }

        // Cambiar color del Toolbar y Status Bar
        toolbar.setBackgroundColor(ContextCompat.getColor(this, colorResId));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColorResId));

        // Cambiar color del botón "Nuevo Pedido"
        btnNewOrder.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, colorResId)));
    }

    private void mostrarDialogoNuevoPedido() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ingresar ID del pedido");

        final EditText input = new EditText(this);
        input.setHint("Ej: AbC123XYZ");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String codigoPedido = input.getText().toString().trim();
            if (codigoPedido.isEmpty()) {
                Toast.makeText(this, "⚠️ Ingresa un ID válido", Toast.LENGTH_SHORT).show();
            } else {
                asignarPedidoAlUsuario(codigoPedido);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void asignarPedidoAlUsuario(String codigoPedido) {
        String userId = authService.getCurrentUserId();

        // Buscar el pedido por el campo codigoPedido
        db.collection("pedidos")
                .whereEqualTo("codigoPedido", codigoPedido)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Obtener el ID del documento encontrado
                        String documentId = querySnapshot.getDocuments().get(0).getId();

                        // Verificar si ya tiene cliente asignado
                        String clienteIdActual = querySnapshot.getDocuments().get(0).getString("clienteId");

                        if (clienteIdActual != null && !clienteIdActual.isEmpty()) {
                            Toast.makeText(this, "⚠️ Este pedido ya está asignado a otro cliente", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Asignar el pedido al usuario
                        db.collection("pedidos").document(documentId)
                                .update("clienteId", userId, "fechaAsignacion", new Date())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "✅ Pedido asignado correctamente", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("CustomerActivity", "Error al asignar pedido", e);
                                    Toast.makeText(this, "❌ Error al asignar pedido", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "❌ Código de pedido no válido", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CustomerActivity", "Error al consultar pedido", e);
                    Toast.makeText(this, "❌ Error al consultar pedido", Toast.LENGTH_SHORT).show();
                });
    }
}
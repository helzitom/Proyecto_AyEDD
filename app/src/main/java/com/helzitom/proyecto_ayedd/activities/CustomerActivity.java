package com.helzitom.proyecto_ayedd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import com.helzitom.proyecto_ayedd.services.AuthService;
import com.helzitom.proyecto_ayedd.services.PedidoService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        Button btnNewOrder = findViewById(R.id.btn_new_order);
        btnNewOrder.setOnClickListener(v -> mostrarDialogoNuevoPedido());

        // 🔹 Configurar Toolbar
        setSupportActionBar(toolbar);
        String tipoUsuario = getIntent().getStringExtra("tipo_usuario");
        cambiarColorToolbar(tipoUsuario);

        // 🔹 Ajuste de márgenes por notch
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Inicializar lista y RecyclerView
        pedidoList = new ArrayList<>();
        setupRecyclerView();

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

    // ========================
    // 🔹 MOSTRAR PEDIDO EN TIEMPO REAL
    // ========================
    private void onVerEnTiempoReal(Pedido pedido) {
        Intent intent = new Intent(this, TrackOrderActivity.class);
        intent.putExtra(TrackOrderActivity.EXTRA_PEDIDO_ID, pedido.getId());
        startActivity(intent);
    }

    // ========================
    // 🔹 CAMBIAR COLOR TOOLBAR
    // ========================
    private void cambiarColorToolbar(String tipoUsuario) {
        if (tipoUsuario == null) return;
        int colorResId;
        switch (tipoUsuario.toLowerCase()) {
            case "admin": colorResId = R.color.admin_color; break;
            case "cliente": colorResId = R.color.customer_color; break;
            case "delivery": colorResId = R.color.delivery_color; break;
            case "receiver": colorResId = R.color.receiver_color; break;
            default: colorResId = R.color.main; break;
        }
        toolbar.setBackgroundColor(ContextCompat.getColor(this, colorResId));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, colorResId));
    }

    // ========================
    // 🔹 NUEVO PEDIDO POR ID
    // ========================
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
        db.collection("pedidos").document(codigoPedido)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("pedidos").document(codigoPedido)
                                .update("clienteId", userId, "fechaAsignacion", new Date())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Pedido asignado correctamente ✔️", Toast.LENGTH_SHORT).show();
                                    // ✅ Se recargan los pedidos automáticamente por el listener en tiempo real
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error al asignar pedido ❌", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "ID de pedido no válido ❌", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al consultar pedido ❌", Toast.LENGTH_SHORT).show());
    }
}

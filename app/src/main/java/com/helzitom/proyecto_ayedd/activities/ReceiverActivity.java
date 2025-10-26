package com.helzitom.proyecto_ayedd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.adapters.PedidosAdapter;
import com.helzitom.proyecto_ayedd.models.Pedido;
import com.helzitom.proyecto_ayedd.services.PedidoService;

import java.util.ArrayList;
import java.util.List;

public class ReceiverActivity extends AppCompatActivity {

    Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FloatingActionButton fabLogout, fabAddOrder;

    private List<Pedido> pedidoList;
    private PedidosAdapter adapter;
    private PedidoService pedidosService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receiver);

        // 🔹 Inicializar lista y adapter
        pedidoList = new ArrayList<>();
        adapter = new PedidosAdapter(pedidoList, this);

        // 🔹 Inicializar RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rv_receiver_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // ✅ LayoutManager agregado
        recyclerView.setAdapter(adapter);

        // 🔹 Inicializar PedidoService
        pedidosService = new PedidoService(this, pedidoList, adapter);

        // 🔹 Inicializar FirebaseAuth y Toolbar
        mAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String tipoUsuario = getIntent().getStringExtra("tipo_usuario");
        cambiarColorToolbar(tipoUsuario);

        // 🔹 Inicializar botones flotantes
        fabLogout = findViewById(R.id.fab_logout);
        fabAddOrder = findViewById(R.id.fab_add_order);

        // 🔹 Botón para crear pedidos
        fabAddOrder.setOnClickListener(v -> {
            Intent intent = new Intent(ReceiverActivity.this, CreateOrderActivity.class);
            startActivity(intent);
            pedidosService.escucharPedidosTiempoReal();
        });

        // 🔹 Botón para cerrar sesión
        fabLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ReceiverActivity.this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ReceiverActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 🔹 Cargar pedidos al iniciar la Activity
        cargarPedidos();
        pedidosService.escucharPedidosTiempoReal();
    }

    // 🔹 Método para cargar pedidos
    private void cargarPedidos() {
        pedidosService.obtenerTodosPedidos(new PedidoService.PedidosListCallback() {
            @Override
            public void onSuccess(List<Pedido> pedidos) {
                pedidoList.clear();
                pedidoList.addAll(pedidos);
                adapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(), "Pedidos cargados: " + pedidos.size(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getApplicationContext(), "Error al obtener pedidos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Cambiar color de la toolbar según tipo de usuario
    private void cambiarColorToolbar(String tipoUsuario) {
        if (tipoUsuario == null) return;
        int colorResId;
        switch (tipoUsuario.toLowerCase()) {
            case "admin": colorResId = R.color.admin_color; break;
            case "cliente": colorResId = R.color.customer_color; break;
            case "delivery": colorResId = R.color.delivery_color; break;
            case "receptor": colorResId = R.color.receiver_color; break;
            default: colorResId = R.color.main; break;
        }
        toolbar.setBackgroundColor(ContextCompat.getColor(this, colorResId));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, colorResId));
    }
}

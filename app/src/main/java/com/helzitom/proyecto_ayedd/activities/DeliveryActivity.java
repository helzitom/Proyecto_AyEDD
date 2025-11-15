package com.helzitom.proyecto_ayedd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.adapters.DeliveryPedidosAdapter;
import com.helzitom.proyecto_ayedd.models.Pedido;
import com.helzitom.proyecto_ayedd.services.AuthService;
import com.helzitom.proyecto_ayedd.services.FirebaseManager;
import com.helzitom.proyecto_ayedd.services.PedidoService;

import java.util.ArrayList;
import java.util.List;

public class DeliveryActivity extends AppCompatActivity {
    private static final String TAG = "DeliveryActivity";

    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvPedidos;
    private LinearLayout layoutEmpty;
    private TextView tvPedidosHoy;
    private FloatingActionButton fabLogout;

    private DeliveryPedidosAdapter adapter;
    private PedidoService pedidoService;
    private AuthService authService;
    private boolean isAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        FirebaseManager.getInstance().initialize(this);
        pedidoService = new PedidoService();
        authService = new AuthService();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        loadPedidos();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvPedidos = findViewById(R.id.rv_delivery_orders);
        layoutEmpty = findViewById(R.id.layout_empty_delivery);
        tvPedidosHoy = findViewById(R.id.tv_pedidos_hoy);
        fabLogout = findViewById(R.id.fab_logout);

        fabLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
            logout();
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupRecyclerView() {
        adapter = new DeliveryPedidosAdapter(new ArrayList<>(), this, new DeliveryPedidosAdapter.OnPedidoClickListener() {
            @Override
            public void onVerMapaClick(Pedido pedido) {
                // Implementar si necesitas esta funcionalidad
                Toast.makeText(DeliveryActivity.this, "Ver mapa", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onIniciarClick(String pedidoId) {
                // Buscar el pedido en la lista del adapter
                Pedido pedido = adapter.getPedidoById(pedidoId);
                if (pedido != null) {
                    onIniciarRuta(pedido);
                }
            }
        });
        rvPedidos.setLayoutManager(new LinearLayoutManager(this));
        rvPedidos.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        // Configurar colores del refresh
        swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        // Listener para cuando el usuario desliza
        swipeRefresh.setOnRefreshListener(() -> {
            Log.d(TAG, "🔄 Refrescando lista de pedidos...");
            loadPedidos();
        });
    }


    private void loadPedidos() {
        String repartidorId = authService.getCurrentUserId();

        if (!isAvailable) {
            // Si no está disponible, solo mostrar pedidos asignados
            loadMisPedidosAsignados(repartidorId);
            return;
        }

        // Cargar pedidos disponibles (pendientes o asignados a este repartidor)
        pedidoService.obtenerTodosPedidos(new PedidoService.PedidosListCallback() {
            @Override
            public void onSuccess(List<Pedido> pedidos) {
                Log.d(TAG, "✅ Pedidos cargados: " + pedidos.size());

                List<Pedido> pedidosDisponibles = new ArrayList<>();

                for (Pedido p : pedidos) {
                    String estado = p.getEstado() != null ? p.getEstado().toLowerCase() : "";

                    // Filtrar:
                    // 1️⃣ pedidos pendientes (aún no tomados)
                    // 2️⃣ pedidos asignados al repartidor actual
                    // 3️⃣ pedidos en camino del mismo repartidor
                    if ("pendiente".equals(estado)
                            || ("asignado".equals(estado) && repartidorId.equals(p.getRepartidorId()))
                            || ("en_camino".equals(estado) && repartidorId.equals(p.getRepartidorId()))
                            || ("en camino".equals(estado) && repartidorId.equals(p.getRepartidorId()))) {

                        pedidosDisponibles.add(p);
                    }
                }

                updateUI(pedidosDisponibles);

                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
                Toast.makeText(DeliveryActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                showEmpty(true);
                swipeRefresh.setRefreshing(false);
            }
        });
    }


    private void loadMisPedidosAsignados(String repartidorId) {
        pedidoService.obtenerPedidosPorRepartidor(repartidorId, "asignado",
                new PedidoService.PedidosListCallback() {
                    @Override
                    public void onSuccess(List<Pedido> pedidos) {
                        Log.d(TAG, "Mis pedidos asignados: " + pedidos.size());
                        updateUI(pedidos);
                        swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error: " + error);
                        showEmpty(true);
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private void updateUI(List<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            showEmpty(true);
        } else {
            showEmpty(false);
            adapter.updateList(pedidos);
        }

        // Actualizar estadísticas
        tvPedidosHoy.setText(String.valueOf(pedidos.size()));
        // TODO: Calcular ganancias reales
    }

    private void showEmpty(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvPedidos.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void onIniciarRuta(Pedido pedido) {
        Intent intent = new Intent(this, DeliveryRouteActivity.class);
        intent.putExtra(DeliveryRouteActivity.EXTRA_PEDIDO, pedido);
        startActivity(intent);
    }

    private void logout() {
        authService.logout();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar pedidos al volver a la activity
        loadPedidos();
    }
}
package com.helzitom.proyecto_ayedd.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.adapters.PedidosAdapter;
import com.helzitom.proyecto_ayedd.models.Pedido;
import com.helzitom.proyecto_ayedd.services.PedidoService;

import java.util.ArrayList;
import java.util.List;

//Clase de el fragmento de todos los pedidos
public class PedidosFragment extends Fragment {
    private static final String TAG = "PedidosFragment";

    private RecyclerView rvPedidos;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private Chip chipAll, chipPending, chipInProgress, chipDelivered;

    private PedidosAdapter adapter;
    private PedidoService pedidoService;
    private List<Pedido> allPedidos;
    private String currentFilter = "all";

    //Método para crear la vista
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_fragment_pedidos_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupFilters();
        loadPedidos();
    }

    //Método para iniciar la vista con sus componentes
    private void initViews(View view) {
        rvPedidos = view.findViewById(R.id.rv_pedidos);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progress_bar);
        chipAll = view.findViewById(R.id.chip_all);
        chipPending = view.findViewById(R.id.chip_pending);
        chipInProgress = view.findViewById(R.id.chip_in_progress);
        chipDelivered = view.findViewById(R.id.chip_delivered);

        pedidoService = new PedidoService();
        allPedidos = new ArrayList<>();
    }
    //Establecer el Recycler para optimizar la app
    private void setupRecyclerView() {
        adapter = new PedidosAdapter(new ArrayList<>(), requireContext());
        rvPedidos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPedidos.setAdapter(adapter);
    }

    //Establece los filtros, según su estado, con el método filtrar pedidos
    private void setupFilters() {
        chipAll.setOnClickListener(v -> filterPedidos("all"));
        chipPending.setOnClickListener(v -> filterPedidos("pending"));
        chipInProgress.setOnClickListener(v -> filterPedidos("in_progress"));
        chipDelivered.setOnClickListener(v -> filterPedidos("delivered"));
    }

    //Método que carga los pedidos, con ayuda de su servicio (PedidoService)
    private void loadPedidos() {
        showLoading(true);

        pedidoService.obtenerTodosPedidos(new PedidoService.PedidosListCallback() {
            @Override
            public void onSuccess(List<Pedido> pedidos) {
                Log.d(TAG, "✅ Pedidos cargados: " + pedidos.size());
                allPedidos = pedidos;
                filterPedidos(currentFilter);
                showLoading(false);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error: " + error);
                showEmpty(true);
                showLoading(false);
            }
        });
    }

    //Método para filtrar los pedidos segun su estado
    private void filterPedidos(String filter) {
        currentFilter = filter;
        List<Pedido> filteredList = new ArrayList<>();

        for (Pedido pedido : allPedidos) {
            switch (filter) {
                case "all":
                    filteredList.add(pedido);
                    break;
                case "pending":
                    if ("pendiente".equals(pedido.getEstado())) {
                        filteredList.add(pedido);
                    }
                    break;
                case "in_progress":
                    if ("en_camino".equals(pedido.getEstado()) || "asignado".equals(pedido.getEstado())) {
                        filteredList.add(pedido);
                    }
                    break;
                case "delivered":
                    if ("entregado".equals(pedido.getEstado())) {
                        filteredList.add(pedido);
                    }
                    break;
            }
        }

        adapter.updateList(filteredList);
        showEmpty(filteredList.isEmpty());
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvPedidos.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvPedidos.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
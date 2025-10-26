package com.helzitom.proyecto_ayedd.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.adapters.PedidoItemsAdapter;
import com.helzitom.proyecto_ayedd.models.Pedido;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PedidoDetailsDialog extends DialogFragment {

    private static final String ARG_PEDIDO = "pedido";
    private Pedido pedido;

    private TextView tvPedidoId, tvClienteNombre, tvClienteTelefono, tvClienteEmail;
    private TextView tvDireccion, tvSubtotal, tvDelivery, tvTotal;
    private TextView tvFechaCreacion, tvRepartidor;
    private Chip chipEstado;
    private RecyclerView rvItems;
    private Button btnClose, btnVerMapa;

    public static PedidoDetailsDialog newInstance(Pedido pedido) {
        PedidoDetailsDialog dialog = new PedidoDetailsDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PEDIDO, pedido);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            pedido = (Pedido) getArguments().getSerializable(ARG_PEDIDO);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_pedido_details, null);

        initViews(view);
        fillPedidoData();
        setupListeners();

        builder.setView(view);
        return builder.create();
    }

    private void initViews(View view) {
        tvPedidoId = view.findViewById(R.id.tv_dialog_pedido_id);
        chipEstado = view.findViewById(R.id.chip_dialog_estado);
        tvClienteNombre = view.findViewById(R.id.tv_dialog_cliente_nombre);
        tvClienteTelefono = view.findViewById(R.id.tv_dialog_cliente_telefono);
        tvClienteEmail = view.findViewById(R.id.tv_dialog_cliente_email);
        tvDireccion = view.findViewById(R.id.tv_dialog_direccion);
        rvItems = view.findViewById(R.id.rv_dialog_items);
        tvSubtotal = view.findViewById(R.id.tv_dialog_subtotal);
        tvDelivery = view.findViewById(R.id.tv_dialog_delivery);
        tvTotal = view.findViewById(R.id.tv_dialog_total);
        tvFechaCreacion = view.findViewById(R.id.tv_dialog_fecha_creacion);
        tvRepartidor = view.findViewById(R.id.tv_dialog_repartidor);
        btnClose = view.findViewById(R.id.btn_dialog_close);
        btnVerMapa = view.findViewById(R.id.btn_dialog_ver_mapa);
    }

    private void fillPedidoData() {
        if (pedido == null) return;

        // Header
        tvPedidoId.setText("Pedido #" + pedido.getId().substring(0, 8));
        chipEstado.setText(getEstadoText(pedido.getEstado()));

        // Cliente
        tvClienteNombre.setText("👤 " + (pedido.getClienteNombre() != null ? pedido.getClienteNombre() : "Sin nombre"));
        tvClienteTelefono.setText("📞 " + (pedido.getClienteTelefono() != null ? pedido.getClienteTelefono() : "Sin teléfono"));
        tvClienteEmail.setText("📧 Sin email"); // Agregar si tienes este campo

        // Dirección
        tvDireccion.setText("📍 " + (pedido.getDireccionDestino() != null ? pedido.getDireccionDestino() : "Sin dirección"));

        // Items
        if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
            PedidoItemsAdapter adapter = new PedidoItemsAdapter(pedido.getItems());
            rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvItems.setAdapter(adapter);
        }

        // Resumen
        tvSubtotal.setText("S/ " + String.format(Locale.getDefault(), "%.2f", pedido.getSubtotal()));
        tvDelivery.setText("S/ " + String.format(Locale.getDefault(), "%.2f", pedido.getCostoDelivery()));
        tvTotal.setText("S/ " + String.format(Locale.getDefault(), "%.2f", pedido.getTotal()));

        // Fecha
        if (pedido.getFechaCreacion() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvFechaCreacion.setText("🕒 Creado: " + sdf.format(pedido.getFechaCreacion()));
        }

        // Repartidor
        if (pedido.getRepartidorNombre() != null) {
            tvRepartidor.setText("🚚 Repartidor: " + pedido.getRepartidorNombre());
        } else {
            tvRepartidor.setText("🚚 Repartidor: No asignado");
        }
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
        btnVerMapa.setOnClickListener(v -> {
            // TODO: Implementar vista de mapa
            android.widget.Toast.makeText(requireContext(), "Mapa próximamente", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private String getEstadoText(String estado) {
        switch (estado) {
            case "pendiente": return "Pendiente";
            case "asignado": return "Asignado";
            case "en_camino": return "En Camino";
            case "entregado": return "Entregado";
            case "cancelado": return "Cancelado";
            default: return estado;
        }
    }
}
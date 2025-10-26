package com.helzitom.proyecto_ayedd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.dialogs.PedidoDetailsDialog;
import com.helzitom.proyecto_ayedd.models.Pedido;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    private List<Pedido> pedidos;
    private Context context;

    public PedidosAdapter(List<Pedido> pedidos, Context context) {
        this.pedidos = pedidos;
        this.context = context;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        holder.bind(pedido);
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public void updateList(List<Pedido> newPedidos) {
        this.pedidos = newPedidos;
        notifyDataSetChanged();
    }

    class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvPedidoId, tvCliente, tvDireccion, tvFecha, tvTotal;
        Chip chipEstado;
        Button btnVerDetalles, btnVerMapa;

        PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPedidoId = itemView.findViewById(R.id.tv_pedido_id);
            tvCliente = itemView.findViewById(R.id.tv_cliente);
            tvDireccion = itemView.findViewById(R.id.tv_direccion);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvTotal = itemView.findViewById(R.id.tv_total);
            chipEstado = itemView.findViewById(R.id.chip_estado);
            btnVerDetalles = itemView.findViewById(R.id.btn_ver_detalles);
            btnVerMapa = itemView.findViewById(R.id.btn_ver_mapa);
        }

        void bind(Pedido pedido) {
            // Mostrar código de 6 dígitos
            String codigo = pedido.getcodigoPedido();
            if (codigo != null && !codigo.isEmpty()) {
                tvPedidoId.setText("Pedido #" + codigo);
            } else {
                tvPedidoId.setText("Pedido #------");
            }

            tvCliente.setText("Cliente: " + (pedido.getClienteNombre() != null ? pedido.getClienteNombre() : "Sin nombre"));
            tvDireccion.setText("📍 " + (pedido.getDireccionDestino() != null ? pedido.getDireccionDestino() : "Sin dirección"));

            if (pedido.getFechaCreacion() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvFecha.setText("🕒 " + sdf.format(pedido.getFechaCreacion()));
            } else {
                tvFecha.setText("🕒 Sin fecha");
            }

            tvTotal.setText("S/ " + String.format(Locale.getDefault(), "%.2f", pedido.getTotal()));

            String estado = pedido.getEstado();
            chipEstado.setText(getEstadoText(estado));
            chipEstado.setChipBackgroundColorResource(getEstadoColor(estado));

            btnVerDetalles.setOnClickListener(v -> {
                PedidoDetailsDialog dialog = PedidoDetailsDialog.newInstance(pedido);
                dialog.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "PedidoDetails");
            });

            btnVerMapa.setOnClickListener(v -> {
                android.widget.Toast.makeText(context, "Mapa próximamente", android.widget.Toast.LENGTH_SHORT).show();
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

        private int getEstadoColor(String estado) {
            switch (estado) {
                case "pendiente": return android.R.color.holo_orange_light;
                case "asignado": return android.R.color.holo_blue_light;
                case "en_camino": return android.R.color.holo_purple;
                case "entregado": return android.R.color.holo_green_light;
                case "cancelado": return android.R.color.holo_red_light;
                default: return android.R.color.darker_gray;
            }
        }
    }
}
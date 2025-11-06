package com.helzitom.proyecto_ayedd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.models.Pedido;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerPedidosAdapter extends RecyclerView.Adapter<CustomerPedidosAdapter.ViewHolder> {

    private List<Pedido> listaPedidos;
    private final Context context;
    private final OnPedidoClickListener listener;

    // 🔹 Constructor
    public CustomerPedidosAdapter(List<Pedido> listaPedidos, Context context, OnPedidoClickListener listener) {
        this.listaPedidos = listaPedidos != null ? listaPedidos : new ArrayList<>();
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pedido_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pedido pedido = listaPedidos.get(position);

        // 🔹 Mostrar código del pedido
        String codigoPedido = pedido.getcodigoPedido() != null ? pedido.getcodigoPedido() : "Sin código";
        holder.tvPedidoId.setText("📦 Pedido #" + codigoPedido);

        // 🔹 Dirección
        String direccion = pedido.getDireccionDestino() != null ? pedido.getDireccionDestino() : "Sin dirección";
        holder.tvDireccion.setText("📍 " + direccion);

        // 🔹 Fecha formateada
        if (pedido.getFechaAsignacion() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvFecha.setText("🕒 " + sdf.format(pedido.getFechaAsignacion()));
        } else {
            holder.tvFecha.setText("🕒 Fecha desconocida");
        }

        // 🔹 Total
        double total = pedido.getTotal();
        holder.tvTotal.setText(String.format(Locale.getDefault(), "💰 S/ %.2f", total));

        // 🔹 Estado con colores
        String estado = pedido.getEstado() != null ? pedido.getEstado() : "desconocido";
        holder.chipEstado.setText(formatearEstado(estado));

        // Cambiar color según estado
        int colorFondo = getColorEstado(estado);
        holder.chipEstado.setBackgroundColor(ContextCompat.getColor(context, colorFondo));
        holder.chipEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white));

        // 🔹 LÓGICA DEL BOTÓN: Solo habilitar para "en_camino" y "asignado"
        String estadoNormalizado = estado.toLowerCase().replace(" ", "_");
        boolean puedeVerEnTiempoReal = estadoNormalizado.equals("en_camino") || estadoNormalizado.equals("asignado");

        holder.btnVerSeguimiento.setEnabled(puedeVerEnTiempoReal);
        holder.btnVerSeguimiento.setAlpha(puedeVerEnTiempoReal ? 1.0f : 0.5f);

        if (puedeVerEnTiempoReal) {
            holder.btnVerSeguimiento.setText("🗺️ Ver en Tiempo Real");
            holder.btnVerSeguimiento.setOnClickListener(v -> {
                if (listener != null) listener.onVerEnTiempoRealClick(pedido);
            });
        } else {
            // Cambiar texto según el estado
            switch (estadoNormalizado) {
                case "pendiente":
                    holder.btnVerSeguimiento.setText("⏳ Esperando asignación");
                    break;
                case "entregado":
                    holder.btnVerSeguimiento.setText("✅ Pedido entregado");
                    break;
                case "cancelado":
                    holder.btnVerSeguimiento.setText("❌ Pedido cancelado");
                    break;
                default:
                    holder.btnVerSeguimiento.setText("🗺️ No disponible");
                    break;
            }
            holder.btnVerSeguimiento.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return listaPedidos.size();
    }

    // 🔄 Método para actualizar la lista
    public void updateList(List<Pedido> nuevaLista) {
        this.listaPedidos = nuevaLista != null ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    // 🎨 Formatear estado
    private String formatearEstado(String estado) {
        switch (estado.toLowerCase().replace(" ", "_")) {
            case "pendiente":
                return "⏳ PENDIENTE";
            case "asignado":
                return "✅ ASIGNADO";
            case "en_camino":
                return "🚚 EN CAMINO";
            case "entregado":
                return "✅ ENTREGADO";
            case "cancelado":
                return "❌ CANCELADO";
            default:
                return "❓ " + estado.toUpperCase();
        }
    }

    // 🎨 Obtener color según estado
    private int getColorEstado(String estado) {
        switch (estado.toLowerCase().replace(" ", "_")) {
            case "pendiente":
                return R.color.estado_pendiente; // Naranja
            case "asignado":
                return R.color.customer_color; // Azul
            case "en_camino":
                return R.color.estado_en_camino; // Azul/Verde
            case "entregado":
                return R.color.estado_entregado; // Verde
            case "cancelado":
                return R.color.estado_cancelado; // Rojo
            default:
                return R.color.estado_default; // Gris
        }
    }

    // Interfaz para manejar eventos
    public interface OnPedidoClickListener {
        void onVerEnTiempoRealClick(Pedido pedido);
    }

    // ViewHolder interno
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPedidoId, tvDireccion, tvFecha, tvTotal, chipEstado;
        Button btnVerSeguimiento;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPedidoId = itemView.findViewById(R.id.tv_customer_pedido_id);
            tvDireccion = itemView.findViewById(R.id.tv_customer_pedido_direccion);
            tvFecha = itemView.findViewById(R.id.tv_customer_pedido_fecha);
            tvTotal = itemView.findViewById(R.id.tv_customer_pedido_total);
            chipEstado = itemView.findViewById(R.id.chip_customer_estado);
            btnVerSeguimiento = itemView.findViewById(R.id.btn_customer_track);
        }
    }
}
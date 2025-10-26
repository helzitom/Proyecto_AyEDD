package com.helzitom.proyecto_ayedd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.models.Pedido;

import java.util.ArrayList;
import java.util.List;

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

        // 🔹 Mostrar datos del pedido
        holder.tvPedidoId.setText("Pedido #" + (pedido.getId() != null ? pedido.getId() : "Desconocido"));
        holder.tvDireccion.setText("📍 " + (pedido.getDireccionDestino() != null ? pedido.getDireccionDestino() : "Sin dirección"));
        holder.tvFecha.setText(pedido.getFechaAsignacion() != null ? pedido.getFechaAsignacion().toString() : "Fecha desconocida");
        holder.tvTotal.setText("S/ " + pedido.getTotal());

        // 🔹 Estado
        holder.chipEstado.setText(pedido.getEstado() != null ? pedido.getEstado().toUpperCase() : "DESCONOCIDO");

        // 🔹 Botón: Ver seguimiento en tiempo real
        holder.btnVerSeguimiento.setOnClickListener(v -> {
            if (listener != null) listener.onVerEnTiempoRealClick(pedido);
        });
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

    // 🔹 Interfaz para manejar eventos
    public interface OnPedidoClickListener {
        void onVerEnTiempoRealClick(Pedido pedido);
    }

    // 🧱 ViewHolder interno
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

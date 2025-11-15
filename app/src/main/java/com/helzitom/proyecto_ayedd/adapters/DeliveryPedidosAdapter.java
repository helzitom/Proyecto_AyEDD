package com.helzitom.proyecto_ayedd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.models.Pedido;

import java.util.ArrayList;
import java.util.List;

public class DeliveryPedidosAdapter extends RecyclerView.Adapter<DeliveryPedidosAdapter.ViewHolder> {

    private List<Pedido> listaPedidos;
    private final Context context;
    private final OnPedidoClickListener listener;

    public DeliveryPedidosAdapter(List<Pedido> listaPedidos, Context context, OnPedidoClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.listaPedidos = listaPedidos != null ? listaPedidos : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✅ Debe coincidir con tu XML real
        View view = LayoutInflater.from(context).inflate(R.layout.item_pedido_delivery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pedido pedido = listaPedidos.get(position);

        // Evitar NullPointerException si algún campo viene nulo
        holder.tvCliente.setText("👤 Cliente: " + safe(pedido.getClienteNombre()));
        holder.tvDireccion.setText("📍 " + safe(pedido.getDireccionDestino()));
        holder.tvTelefono.setText("📞 " + safe(pedido.getClienteTelefono()));
        holder.tvTotal.setText("💰 S/ " + pedido.getTotal());

        // Botones funcionales sin lambda
        holder.btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && pedido.getId() != null) {
                    listener.onIniciarClick(pedido.getId());
                } else {
                    Toast.makeText(context, "⚠️ Pedido sin ID válido", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return listaPedidos.size();
    }

    // 🔄 Actualizar lista
    public void updateList(List<Pedido> nuevaLista) {
        this.listaPedidos = nuevaLista != null ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    // 🎯 Interfaz para eventos
    // 🎯 Interfaz para eventos
    public interface OnPedidoClickListener {
        void onVerMapaClick(Pedido pedido);

        void onIniciarClick(String pedidoId);
    }


    // 🧱 ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCliente, tvDireccion, tvTelefono, tvTotal;
        Button  btnIniciar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCliente = itemView.findViewById(R.id.tv_delivery_pedido_cliente);
            tvDireccion = itemView.findViewById(R.id.tv_delivery_pedido_direccion);
            tvTelefono = itemView.findViewById(R.id.tv_delivery_pedido_telefono);
            tvTotal = itemView.findViewById(R.id.tv_delivery_pedido_total);
            btnIniciar = itemView.findViewById(R.id.btn_delivery_iniciar);
        }
    }

    // 🔹 Método auxiliar para evitar texto nulo
    private String safe(String text) {
        return text != null ? text : "—";
    }
    public Pedido getPedidoById(String pedidoId) {
        for (Pedido p : listaPedidos) {
            if (p.getId() != null && p.getId().equals(pedidoId)) {
                return p;
            }
        }
        return null;
    }
}

package com.helzitom.proyecto_ayedd.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.models.ItemPedido;

import java.util.List;
import java.util.Locale;

public class PedidoItemsAdapter extends RecyclerView.Adapter<PedidoItemsAdapter.ItemViewHolder> {

    private List<ItemPedido> items;

    public PedidoItemsAdapter(List<ItemPedido> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemPedido item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvCantidad, tvNombre, tvPrecio;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCantidad = itemView.findViewById(R.id.tv_item_cantidad);
            tvNombre = itemView.findViewById(R.id.tv_item_nombre);
            tvPrecio = itemView.findViewById(R.id.tv_item_precio);
        }

        void bind(ItemPedido item) {
            tvCantidad.setText(item.getCantidad() + "x");
            tvNombre.setText(item.getNombre());
            tvPrecio.setText("S/ " + String.format(Locale.getDefault(), "%.2f", item.getSubtotal()));
        }
    }
}
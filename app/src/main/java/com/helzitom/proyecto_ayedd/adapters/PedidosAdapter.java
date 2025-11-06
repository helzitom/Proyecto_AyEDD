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

    /**
     * Adaptador para mostrar la lista de pedidos dentro de un RecyclerView.
     * Este adaptador maneja la vinculación de los datos del modelo
     * con la interfaz visual definida en el layout item_pedido.xml.
     */
public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    private List<Pedido> pedidos;
    private Context context;

    /**
     * Constructor del adaptador.
     */
    public PedidosAdapter(List<Pedido> pedidos, Context context) {
        this.pedidos = pedidos;
        this.context = context;
    }

    /**
     * Crea un nuevo ViewHolder cuando el RecyclerView lo necesita.
     */
    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    /**
     * Asigna los datos del pedido correspondiente a la posición al ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        holder.bind(pedido);
    }

    /**
     * Retorna la cantidad total de elementos (pedidos) en la lista.
     */
    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    /**
     * Actualiza la lista de pedidos mostrada en el RecyclerView.
     */
    public void updateList(List<Pedido> newPedidos) {
        this.pedidos = newPedidos;
        notifyDataSetChanged();
    }

    /**
     * Clase interna que representa el ViewHolder para cada pedido individual.
     * Contiene las referencias a los elementos visuales de cada ítem y
     * define la lógica para mostrar la información de un Pedido.
     */
    class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvPedidoId, tvCliente, tvDireccion, tvFecha, tvTotal;
        Chip chipEstado;
        Button btnVerDetalles, btnVerMapa;

        /**
         * Constructor del ViewHolder.
         * Inicializa las vistas del layout item_pedido.xml.
         */
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

        /**
         * Asigna los datos de un objeto a los elementos de la interfaz.
         * Configura los textos, colores y acciones de los botones.
         */
        void bind(Pedido pedido) {
            // Mostrar código del pedido (6 dígitos o marcador)
            String codigo = pedido.getcodigoPedido();
            if (codigo != null && !codigo.isEmpty()) {
                tvPedidoId.setText("Pedido #" + codigo);
            } else {
                tvPedidoId.setText("Pedido #------");
            }

            // Mostrar nombre del cliente o texto por defecto
            tvCliente.setText("Cliente: " + (pedido.getClienteNombre() != null
                    ? pedido.getClienteNombre() : "Sin nombre"));

            // Mostrar dirección del pedido
            tvDireccion.setText("📍 " + (pedido.getDireccionDestino() != null
                    ? pedido.getDireccionDestino() : "Sin dirección"));

            // Mostrar fecha de creación formateada o texto por defecto
            if (pedido.getFechaCreacion() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvFecha.setText("🕒 " + sdf.format(pedido.getFechaCreacion()));
            } else {
                tvFecha.setText("🕒 Sin fecha");
            }

            // Mostrar total del pedido con dos decimales
            tvTotal.setText("S/ " + String.format(Locale.getDefault(), "%.2f", pedido.getTotal()));

            // Mostrar estado del pedido con texto y color
            String estado = pedido.getEstado();
            chipEstado.setText(getEstadoText(estado));
            chipEstado.setChipBackgroundColorResource(getEstadoColor(estado));

            // Botón para ver detalles del pedido
            btnVerDetalles.setOnClickListener(v -> {
                PedidoDetailsDialog dialog = PedidoDetailsDialog.newInstance(pedido);
                dialog.show(((androidx.fragment.app.FragmentActivity) context)
                        .getSupportFragmentManager(), "PedidoDetails");
            });

            // Botón para ver el mapa (en desarrollo)
            btnVerMapa.setOnClickListener(v -> {
                android.widget.Toast.makeText(context, "Mapa próximamente", android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        /**
         * Devuelve el texto legible correspondiente al estado del pedido.
         */
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

        /**
         * Devuelve el color correspondiente a cada estado del pedido.
         */
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

package com.helzitom.proyecto_ayedd.models;

import com.google.firebase.firestore.GeoPoint;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

//Clase de pedido
public class Pedido implements Serializable {

    private String id;

    private String codigoPedido;

    private String codigoVerificacion;

    // Datos del cliente
    private String clienteId;
    private String clienteNombre;
    private String clienteTelefono;
    private String clienteDireccion;

    // Datos del repartidor
    private String repartidorId;
    private String repartidorNombre;

    // Estado del pedido
    // Valores posibles: "pendiente", "asignado", "en_camino", "entregado", "cancelado"
    private String estado;

    // Coordenadas de destino (entrega)
    private double latitudDestino;
    private double longitudDestino;
    private String direccionDestino;

    // Coordenadas actuales del repartidor
    private double latitudRepartidor;
    private double longitudRepartidor;
    private Date ultimaActualizacionUbicacion;

    // Coordenadas de origen (tienda)
    private double latitudOrigen;
    private double longitudOrigen;
    private String direccionOrigen;

    // Datos económicos
    private List<ItemPedido> items;
    private double total;
    private double subtotal;
    private double costoDelivery;

    // Fechas
    private Date fechaCreacion;
    private Date fechaAsignacion;
    private Date fechaInicio;   // Cuando inicia la ruta
    private Date fechaEntrega;

    // Otros
    private String notas;
    private boolean enRuta;

    // Constructor vacío requerido por Firestore
    public Pedido() {}

    // GETTERS Y SETTERS
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getcodigoPedido() {
        return codigoPedido;
    }


    public void setCodigoPedido(String codigoPedido) {
        this.codigoPedido = codigoPedido;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }


    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }

    public String getClienteDireccion() { return clienteDireccion; }
    public void setClienteDireccion(String clienteDireccion) { this.clienteDireccion = clienteDireccion; }

    public String getRepartidorId() { return repartidorId; }
    public void setRepartidorId(String repartidorId) { this.repartidorId = repartidorId; }

    public String getRepartidorNombre() { return repartidorNombre; }
    public void setRepartidorNombre(String repartidorNombre) { this.repartidorNombre = repartidorNombre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getLatitudDestino() { return latitudDestino; }
    public void setLatitudDestino(double latitudDestino) { this.latitudDestino = latitudDestino; }

    public double getLongitudDestino() { return longitudDestino; }
    public void setLongitudDestino(double longitudDestino) { this.longitudDestino = longitudDestino; }

    public String getDireccionDestino() { return direccionDestino; }
    public void setDireccionDestino(String direccionDestino) { this.direccionDestino = direccionDestino; }

    public double getLatitudRepartidor() { return latitudRepartidor; }
    public void setLatitudRepartidor(double latitudRepartidor) { this.latitudRepartidor = latitudRepartidor; }

    public double getLongitudRepartidor() { return longitudRepartidor; }
    public void setLongitudRepartidor(double longitudRepartidor) { this.longitudRepartidor = longitudRepartidor; }

    public Date getUltimaActualizacionUbicacion() { return ultimaActualizacionUbicacion; }
    public void setUltimaActualizacionUbicacion(Date ultimaActualizacionUbicacion) {
        this.ultimaActualizacionUbicacion = ultimaActualizacionUbicacion;
    }

    public double getLatitudOrigen() { return latitudOrigen; }
    public void setLatitudOrigen(double latitudOrigen) { this.latitudOrigen = latitudOrigen; }

    public double getLongitudOrigen() { return longitudOrigen; }
    public void setLongitudOrigen(double longitudOrigen) { this.longitudOrigen = longitudOrigen; }

    public String getDireccionOrigen() { return direccionOrigen; }
    public void setDireccionOrigen(String direccionOrigen) { this.direccionOrigen = direccionOrigen; }

    public List<ItemPedido> getItems() { return items; }
    public void setItems(List<ItemPedido> items) { this.items = items; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getCostoDelivery() { return costoDelivery; }
    public void setCostoDelivery(double costoDelivery) { this.costoDelivery = costoDelivery; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Date getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(Date fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(Date fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public boolean isEnRuta() { return enRuta; }
    public void setEnRuta(boolean enRuta) { this.enRuta = enRuta; }

    // ======================
    // 🔹 MÉTODOS DE UTILIDAD
    // ======================
    public GeoPoint getGeoPointDestino() {
        return new GeoPoint(latitudDestino, longitudDestino);
    }

    public GeoPoint getGeoPointOrigen() {
        return new GeoPoint(latitudOrigen, longitudOrigen);
    }

    public GeoPoint getGeoPointRepartidor() {
        return new GeoPoint(latitudRepartidor, longitudRepartidor);
    }

    // 🔹 Texto legible para la lista (Adapter)
    public String getResumenCliente() {
        return clienteNombre != null ? clienteNombre : "Cliente desconocido";
    }

    public String getResumenDireccion() {
        return direccionDestino != null ? direccionDestino : "Sin dirección";
    }

    public String getResumenTelefono() {
        return clienteTelefono != null ? clienteTelefono : "Sin teléfono";
    }

    public String getResumenTotal() {
        return String.format("S/ %.2f", total);
    }

    public String getResumenEstado() {
        if (estado == null) return "Sin estado";
        switch (estado) {
            case "pendiente": return "🕓 Pendiente";
            case "asignado": return "📦 Asignado";
            case "en_camino": return "🚚 En camino";
            case "entregado": return "✅ Entregado";
            case "cancelado": return "❌ Cancelado";
            default: return estado;
        }
    }
}

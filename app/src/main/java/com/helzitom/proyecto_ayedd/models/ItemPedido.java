package com.helzitom.proyecto_ayedd.models;

import java.io.Serializable;

public class ItemPedido implements Serializable {
    private String nombre;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private String imagen; // 🆕 o puedes llamarlo "descripcion" si prefieres

    public ItemPedido() {}

    public ItemPedido(String nombre, int cantidad, double precioUnitario, String imagen) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
        this.imagen = imagen;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    private void calcularSubtotal() {
        this.subtotal = this.cantidad * this.precioUnitario;
    }

    @Override
    public String toString() {
        return "ItemPedido{" +
                "nombre='" + nombre + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal +
                ", imagen='" + imagen + '\'' +
                '}';
    }
}

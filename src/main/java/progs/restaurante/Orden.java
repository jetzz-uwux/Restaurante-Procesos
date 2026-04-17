/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package progs.restaurante;

import java.util.ArrayList;
import progs.restaurante.Producto;
import progs.restaurante.Mesa;

/**
 *
 * @author camil
 */
public class Orden {

    private ArrayList<Producto> items;
    private String estado; // Pendiente, En preparación, Listo
    private Mesa mesa;
    private int idPedido;

    public Orden(int idPedido, Mesa mesa) {
        this.idPedido = idPedido;
        this.mesa = mesa;
        this.items = new ArrayList<>();
        this.estado = "Pendiente";
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void agregarItem(Producto p) {
        this.items.add(p);
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public int getNumeroMesa() {

        if (this.mesa != null) {
            return this.mesa.getNumero();
        }
        return 0;
    }

    public ArrayList<Producto> getItems() {
        return items;
    }

    public double getTotal() {
        double suma = 0;
        for (Producto p : items) {
            suma += p.getPrecio();
        }
        return suma;
    }
}

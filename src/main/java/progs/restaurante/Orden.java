/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package progs.restaurante;

import java.util.ArrayList;
import progs.restaurante.Productos.Platillo;

/**
 *
 * @author camil
 */
public class Orden {

    private ArrayList<Producto> items;
    private String estado; // Pendiente, En preparación, Listo
   private Mesa mesa;

    public Orden() {
        this.items = new ArrayList<>();
        this.estado = "Pendiente";
    }
    
    public Mesa getMesa(){
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

    public ArrayList<Producto> getItems() {
        return items;
    }
}

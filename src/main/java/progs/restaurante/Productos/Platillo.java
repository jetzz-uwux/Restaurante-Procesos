/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package progs.restaurante.Productos;

/**
 *
 * @author camil
 */
public class Platillo {
    private String nombre;
    private double precio;
    private int stock; // Materia prima

    public Platillo(String nombre, double precio, int stock) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    public String getNombre() { return nombre; }
    
    public int getStock() { return stock; }

    // Método que el Mesero llama para actualizar el almacén
    public void reducirStock(int cantidad) {
        this.stock -= cantidad;
    }
}

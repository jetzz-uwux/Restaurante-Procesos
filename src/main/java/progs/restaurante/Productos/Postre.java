package progs.restaurante.Productos;

import progs.restaurante.Producto;

public class Postre extends Producto {

    public Postre(String nombre, double precio, int stock) {
        super(nombre, precio, stock);
        this.categoria = "Postre";
    }

    public Postre(String nombre, double precio, boolean disponible) {
        super(nombre, precio, "Postre", disponible);
    }

    public Postre(int idProducto, String nombre, double precio, boolean disponible) {
        super(idProducto, nombre, precio, "Postre", disponible);
    }
}

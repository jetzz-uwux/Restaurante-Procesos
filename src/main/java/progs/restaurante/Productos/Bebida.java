package progs.restaurante.Productos;

import progs.restaurante.Producto;

public class Bebida extends Producto {

    public Bebida(String nombre, double precio, int stock) {
        super(nombre, precio, stock);
        this.categoria = "Bebida";
    }

    public Bebida(String nombre, double precio, boolean disponible) {
        super(nombre, precio, "Bebida", disponible);
    }

    public Bebida(int idProducto, String nombre, double precio, boolean disponible) {
        super(idProducto, nombre, precio, "Bebida", disponible);
    }
}

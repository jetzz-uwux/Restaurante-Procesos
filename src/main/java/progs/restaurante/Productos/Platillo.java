package progs.restaurante.Productos;

import progs.restaurante.Producto;

public class Platillo extends Producto {

    public Platillo(String nombre, double precio, int stock) {
        super(nombre, precio, stock);
        this.categoria = "Platillo";
    }

    public Platillo(String nombre, double precio, boolean disponible) {
        super(nombre, precio, "Platillo", disponible);
    }

    public Platillo(int idProducto, String nombre, double precio, boolean disponible) {
        super(idProducto, nombre, precio, "Platillo", disponible);
    }
}

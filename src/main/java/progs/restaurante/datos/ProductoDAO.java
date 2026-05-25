package progs.restaurante.datos;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import progs.restaurante.Producto;
import progs.restaurante.Productos.Platillo;
import progs.restaurante.Productos.Bebida;
import progs.restaurante.Productos.Postre;

public class ProductoDAO {

    public ObservableList<Producto> listarMenu() {
        ObservableList<Producto> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM productos";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idProducto = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                String categoria = rs.getString("categoria");
                boolean disponible = rs.getBoolean("disponible");

                if (categoria.equalsIgnoreCase("Platillo")) {
                    lista.add(new Platillo(idProducto, nombre, precio, disponible));
                } else if (categoria.equalsIgnoreCase("Bebida")) {
                    lista.add(new Bebida(idProducto, nombre, precio, disponible));
                } else if (categoria.equalsIgnoreCase("Postre")) {
                    lista.add(new Postre(idProducto, nombre, precio, disponible));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
    
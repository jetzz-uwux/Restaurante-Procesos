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

    public void guardarProducto(Producto p) {
        String sql = "INSERT INTO productos (nombre, precio, categoria, disponible) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setString(3, p.getCategoria());
            ps.setBoolean(4, p.isDisponible());
            ps.executeUpdate();

            // ← esto es lo nuevo: recuperar el id generado y asignarlo al objeto
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                p.setIdProducto(rs.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void actualizarProducto(Producto p) {
        String sql = "UPDATE productos SET nombre=?, precio=?, categoria=?, disponible=? WHERE id_producto=?";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setString(3, p.getCategoria());
            ps.setBoolean(4, p.isDisponible());
            ps.setInt(5, p.getIdProducto());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void eliminarProducto(int idProducto) {
        String sql = "DELETE FROM productos WHERE id_producto=?";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            int filas = ps.executeUpdate();
            System.out.println("Filas eliminadas: " + filas);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

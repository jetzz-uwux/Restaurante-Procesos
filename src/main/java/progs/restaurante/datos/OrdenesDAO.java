/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package progs.restaurante.datos;

import java.sql.*;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import progs.restaurante.Mesa;
import progs.restaurante.Orden;
import progs.restaurante.Producto;

public class OrdenesDAO {

    public void registrarOrden(Orden orden) {
        String sqlOrden = "INSERT INTO pedidos (numero_mesa, total, estado) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false); // Transacción para asegurar atomicidad

            try (PreparedStatement ps = con.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, orden.getMesa().getNumero());
                ps.setDouble(2, orden.getTotal());
                ps.setString(3, orden.getEstado());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int idPedido = rs.getInt(1);
                    guardarDetalles(con, idPedido, orden);
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Orden> listarPedidos() {
        ObservableList<Orden> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM pedidos WHERE estado != 'Pagado'";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_pedido");
                Mesa mesa = new Mesa(rs.getInt("numero_mesa"));
                Orden orden = new Orden(id, mesa);
                orden.setEstado(rs.getString("estado"));

                orden.setItems(obtenerItemsPedido(id));

                orden.setTotal(orden.getTotal());

                lista.add(orden);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private void guardarDetalles(Connection con, int idPedido, Orden orden) throws SQLException {
        String sqlDetalle = "INSERT INTO detalles_pedido (id_pedido, nombre_producto, precio) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
            for (Producto p : orden.getItems()) {
                ps.setInt(1, idPedido);
                ps.setString(2, p.getNombre());
                ps.setDouble(3, p.getPrecio());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void actualizarOrden(Orden orden) {
        String sql = "UPDATE pedidos SET total = ?, estado = ? WHERE id_pedido = ?";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, orden.getTotal());
            ps.setString(2, orden.getEstado());
            ps.setInt(3, orden.getIdPedido()); // Usamos el ID para saber cuál editar

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar la orden: " + e.getMessage());
        }
    }

    public void actualizarDetalles(Orden orden) {
        String sqlBorrar = "DELETE FROM detalles_pedido WHERE id_pedido = ?";
        String sqlInsertar = "INSERT INTO detalles_pedido (id_pedido, nombre_producto, precio) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false); // Transacción segura
            try {
                try (PreparedStatement psBorrar = con.prepareStatement(sqlBorrar)) {
                    psBorrar.setInt(1, orden.getIdPedido());
                    psBorrar.executeUpdate();
                }

                try (PreparedStatement psInsertar = con.prepareStatement(sqlInsertar)) {
                    for (Producto p : orden.getItems()) {
                        psInsertar.setInt(1, orden.getIdPedido());
                        psInsertar.setString(2, p.getNombre());
                        psInsertar.setDouble(3, p.getPrecio());
                        psInsertar.addBatch();
                    }
                    psInsertar.executeBatch();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void eliminarOrden(int idPedido) {
        String sqlDetalles = "DELETE FROM detalles_pedido WHERE id_pedido = ?";
        String sqlPedido = "DELETE FROM pedidos WHERE id_pedido = ?";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);

            try (PreparedStatement psDetalles = con.prepareStatement(sqlDetalles); PreparedStatement psPedido = con.prepareStatement(sqlPedido)) {

                psDetalles.setInt(1, idPedido);
                psDetalles.executeUpdate();

                psPedido.setInt(1, idPedido);
                psPedido.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Producto> obtenerItemsPedido(int idPedido) {
        ArrayList<Producto> items = new ArrayList<>();
        String sql = "SELECT * FROM detalles_pedido WHERE id_pedido = ?";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Producto p = new Producto(rs.getString("nombre_producto"), rs.getDouble("precio"), 1) {
                };
                items.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public Orden buscarPedidoActivoPorMesa(int numMesa) {
        String sql = "SELECT * FROM pedidos WHERE numero_mesa = ? AND estado NOT IN ('Pagado', 'Cerrado')";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, numMesa);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Orden orden = new Orden(rs.getInt("id_pedido"), new Mesa(numMesa));
                orden.setEstado(rs.getString("estado"));

                // Carga los productos asociados
                orden.setItems(obtenerItemsPedido(orden.getIdPedido()));

                // Calcula el total real basándonos en los items
                orden.setTotal(orden.getTotal());

                return orden;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

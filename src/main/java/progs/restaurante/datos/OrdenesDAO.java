package progs.restaurante.datos;

import java.sql.*;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import progs.restaurante.Mesa;
import progs.restaurante.Orden;
import progs.restaurante.Producto;
import progs.restaurante.Venta;

public class OrdenesDAO {

    public void registrarOrden(Orden orden) {
        String sqlOrden = "INSERT INTO pedidos (numero_mesa, total, estado) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);

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

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, orden.getTotal());
            ps.setString(2, orden.getEstado());
            ps.setInt(3, orden.getIdPedido());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar la orden: " + e.getMessage());
        }
    }

    public void eliminarOrden(int idPedido) {
        String sqlDetalles = "DELETE FROM detalles_pedido WHERE id_pedido = ?";
        String sqlPedido = "DELETE FROM pedidos WHERE id_pedido = ?";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);

            try (PreparedStatement psDetalles = con.prepareStatement(sqlDetalles);
                 PreparedStatement psPedido = con.prepareStatement(sqlPedido)) {

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

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Producto p = new Producto(rs.getString("nombre_producto"),
                                          rs.getDouble("precio"), 1) {};
                items.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Reporte de ventas
    public void registrarVenta(Orden orden) {
        String verificar = "SELECT COUNT(*) FROM ventas WHERE id_pedido = ?";
        String insertar = "INSERT INTO ventas (id_pedido, numero_mesa, total) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.getConexion()) {

            try (PreparedStatement ps = con.prepareStatement(verificar)) {
                ps.setInt(1, orden.getIdPedido());
                ResultSet rs = ps.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(insertar)) {
                ps.setInt(1, orden.getIdPedido());
                ps.setInt(2, orden.getMesa().getNumero());
                ps.setDouble(3, orden.getTotal());
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Reporte de ventas
    public ObservableList<Venta> listarVentas() {
        ObservableList<Venta> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM ventas ORDER BY fecha_hora DESC";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Venta v = new Venta(
                    rs.getInt("id_venta"),
                    rs.getInt("id_pedido"),
                    rs.getInt("numero_mesa"),
                    rs.getDouble("total"),
                    rs.getTimestamp("fecha_hora").toLocalDateTime()
                );
                lista.add(v);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
    
    // Reporte de ventas
    public void actualizarDetalles(Orden orden) {
    String sqlBorrar = "DELETE FROM detalles_pedido WHERE id_pedido = ?";
    String sqlInsertar = "INSERT INTO detalles_pedido (id_pedido, nombre_producto, precio) VALUES (?, ?, ?)";

    try (Connection con = ConexionBD.getConexion()) {
        con.setAutoCommit(false);

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
}
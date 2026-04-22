/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package progs.restaurante.datos;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import progs.restaurante.Producto;
import progs.restaurante.Productos.Platillo;
import progs.restaurante.Productos.Bebida;

public class ProductoDAO {

    public ObservableList<Producto> listarMenu() {
        ObservableList<Producto> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM productos";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement ps = 
                con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                String categoria = rs.getString("categoria");
                
                if (categoria.equalsIgnoreCase("Platillo")) {
                    lista.add(new Platillo(nombre, precio, 0));
                } else {
                    lista.add(new Bebida(nombre, precio, 0));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}

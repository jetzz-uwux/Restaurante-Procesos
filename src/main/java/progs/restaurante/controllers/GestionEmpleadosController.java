package progs.restaurante.controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.stage.Stage;

import progs.restaurante.datos.ConexionBD;

public class GestionEmpleadosController implements Initializable {

    @FXML
    private TableView<Empleado> tablaEmpleados;

    @FXML
    private TableColumn<Empleado, Number> colId;

    @FXML
    private TableColumn<Empleado, String> colNombre;

    @FXML
    private TableColumn<Empleado, String> colUsuario;

    @FXML
    private TableColumn<Empleado, String> colRol;

    private final ObservableList<Empleado> empleados =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colId.setCellValueFactory(
                data -> data.getValue().idEmpleadoProperty());

        colNombre.setCellValueFactory(
                data -> data.getValue().nombreProperty());

        colUsuario.setCellValueFactory(
                data -> data.getValue().usuarioProperty());

        colRol.setCellValueFactory(
                data -> data.getValue().rolProperty());

        cargarEmpleados();
    }

    private void cargarEmpleados() {

        empleados.clear();

        String sql =
                "SELECT id_empleado, nombre, usuario, rol " +
                "FROM empleados";

        try (
                Connection con = ConexionBD.getConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                empleados.add(
                        new Empleado(
                                rs.getInt("id_empleado"),
                                rs.getString("nombre"),
                                rs.getString("usuario"),
                                rs.getString("rol")
                        )
                );
            }

            tablaEmpleados.setItems(empleados);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void actualizarTabla(ActionEvent event) {
        cargarEmpleados();
    }

    @FXML
    private void cerrarVentana(ActionEvent event) {

        Stage stage =
                (Stage) tablaEmpleados.getScene().getWindow();

        stage.close();
    }

    public static class Empleado {

        private final SimpleIntegerProperty idEmpleado;
        private final SimpleStringProperty nombre;
        private final SimpleStringProperty usuario;
        private final SimpleStringProperty rol;

        public Empleado(
                int idEmpleado,
                String nombre,
                String usuario,
                String rol) {

            this.idEmpleado = new SimpleIntegerProperty(idEmpleado);
            this.nombre = new SimpleStringProperty(nombre);
            this.usuario = new SimpleStringProperty(usuario);
            this.rol = new SimpleStringProperty(rol);
        }

        public SimpleIntegerProperty idEmpleadoProperty() {
            return idEmpleado;
        }

        public SimpleStringProperty nombreProperty() {
            return nombre;
        }

        public SimpleStringProperty usuarioProperty() {
            return usuario;
        }

        public SimpleStringProperty rolProperty() {
            return rol;
        }
    }
}
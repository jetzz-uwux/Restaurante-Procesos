package progs.restaurante.controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import progs.restaurante.datos.ConexionBD;

public class AsistenciaController implements Initializable {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblMensaje;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    private void marcarAsistencia(ActionEvent event) {

        String usuario = txtUsuario.getText();
        String contrasena = txtContrasena.getText();

        if (usuario.isEmpty() || contrasena.isEmpty()) {

            lblMensaje.setText("Complete todos los campos");
            return;
        }

        String consulta = "SELECT * FROM empleados "
                + "WHERE usuario = ? AND contrasena = ?";

        try (Connection conexion = ConexionBD.getConexion();
             PreparedStatement ps = conexion.prepareStatement(consulta)) {

            ps.setString(1, usuario);
            ps.setString(2, contrasena);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                lblMensaje.setText("Asistencia registrada: " + usuario);

            } else {

                lblMensaje.setText("Nombre o contraseña incorrectos");
            }

        } catch (Exception e) {

            lblMensaje.setText("Error de conexión");
            e.printStackTrace();
        }
    }
}
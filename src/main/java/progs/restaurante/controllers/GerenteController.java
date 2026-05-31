package progs.restaurante.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class GerenteController {

    @FXML
    private Button btn_ayuda;

    @FXML
    private Button btn_cerrarsesion;

    @FXML
    private Button btn_reportes;

    @FXML
    private Button btn_inventario;

    /**
     * Abrir ventana de Reportes
     */
    @FXML
    private void abrirReportes(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/progs/fxml/VentanaVentas.fxml")
            );

            Stage stage = new Stage();
            stage.setTitle("Reportes");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Abrir ventana de Inventario
     */
    @FXML
    private void abrirInventario(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/progs/fxml/AlmacenVista.fxml")
            );

            Stage stage = new Stage();
            stage.setTitle("Inventario");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleGestionar(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/progs/fxml/VentanaGestionMenu.fxml")
            );

            Stage stage = new Stage();
            stage.setTitle("Inventario");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Abrir ventana de Asistencia
     */
    @FXML
    private void abrirAsistencia(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/progs/fxml/Asistencia.fxml")
            );

            Stage stage = new Stage();
            stage.setTitle("Asistencia");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Cerrar sesión
     */
    @FXML
    private void cerrarSesion(ActionEvent event) {

        Stage stage = (Stage) btn_cerrarsesion.getScene().getWindow();
        stage.close();
    }
}
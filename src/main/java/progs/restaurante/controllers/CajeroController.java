package progs.restaurante.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class CajeroController {

    @FXML
    private Button btn_asistencia;
    private Button btn_Facturacion;

    /**
     * Abrir la lista de asistencia
     */
    @FXML
    private void handleAbrirAsistencia() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/progs/fxml/Asistencia.fxml"));

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Lista de Asistencia");

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir la ventana de asistencia.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleFacturar() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/progs/fxml/VentanaFacturacion.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Tiempo de cobrar");

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir la ventana de cobro.");
            alert.showAndWait();
        }
    }
}

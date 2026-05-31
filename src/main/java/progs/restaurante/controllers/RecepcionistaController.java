package progs.restaurante.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class RecepcionistaController {

    @FXML
    private Button btn_asistencia;
    private Button btn_verMesas;
    private Button btn_reservas;
    private Button btn_listaEspera;
    

    /**
     * Abrir la lista de asistencia
     */
    @FXML
    private void handleRegistrar() {

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
    
    //Abrir la gestión de reservas
    
    @FXML
    private void handleReservas() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/progs/fxml/VistaGestionReservas.fxml"));

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Reservas");

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir la ventana de reservas.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleLista() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/progs/fxml/VentanaEspera.fxml"));

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Lista de Espera");

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir la ventana de la de espera.");
            alert.showAndWait();
        }
    }
}


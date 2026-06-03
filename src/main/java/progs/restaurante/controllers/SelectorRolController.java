package progs.restaurante.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import progs.restaurante.lib.EstilosApp;
import progs.restaurante.lib.EstilosApp.CSS;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import static progs.restaurante.lib.EstilosApp.cargarFuentes;

public class SelectorRolController implements Initializable {
    private static Scene scene;
    @FXML
    private Button btn_acceso_cliente;
    @FXML
    private Button btn_acceso_empleado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Enlace de las acciones correspondientes a cada flujo
        btn_acceso_cliente.setOnAction(e -> abrirVentana("/progs/fxml/VentanaCliente.fxml", "Carta Digital y Reservaciones"));
        btn_acceso_empleado.setOnAction(e -> abrirVentana("/progs/fxml/VistaInicioSesion.fxml", "Restaurante — Iniciar Sesión"));
    }
    
    private void abrirVentana(String rutaFXML, String tituloVentana) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(rutaFXML));
            Stage stageActual = (Stage) btn_acceso_cliente.getScene().getWindow();
            Scene escena = new Scene(root);
            
            EstilosApp.aplicar(escena, CSS.JUEGO, CSS.FUENTES, CSS.BOTONES, CSS.TEXTFIELD, CSS.IMAGEN, CSS.TEXTO, CSS.TABLA_1);
            cargarFuentes();
            stageActual.setScene(escena);
            stageActual.setTitle(tituloVentana);
            stageActual.centerOnScreen();
        } catch (IOException ex) {
            System.err.println("❌ Error al cambiar a la ventana [" + rutaFXML + "]: " + ex.getMessage());
        }
    }
}

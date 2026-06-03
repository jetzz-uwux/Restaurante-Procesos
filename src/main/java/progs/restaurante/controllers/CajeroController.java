package progs.restaurante.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import progs.restaurante.lib.EstilosApp;
import progs.restaurante.lib.EstilosApp.CSS;

public class CajeroController {

    // =========================================================
    // NODOS DEL FXML
    // btn_Facturacion faltaba el @FXML — sin él JavaFX no puede
    // inyectarlo y al hacer clic no pasaba nada
    // =========================================================
    @FXML private Button btn_asistencia;
    @FXML private Button btn_Facturacion;   // ← faltaba @FXML
    @FXML private Button btn_cerrarsesion;
    @FXML private Button btn_ayuda;

    // CSS comunes para todas las ventanas del cajero
    private static final CSS[] CSS_COMUN = {
        CSS.JUEGO, CSS.FUENTES, CSS.BOTONES,
        CSS.TEXTFIELD, CSS.IMAGEN, CSS.TEXTO, CSS.TABLA_1, CSS.TABLA_2
    };

    // =========================================================
    // MÉTODO AUXILIAR: evita repetir el mismo bloque en cada método
    // Orden correcto: cargarFuentes → root → scene → aplicar → show
    // =========================================================
    private void abrirVentana(String rutaFxml, String titulo, CSS... estilos) {
        try {
            EstilosApp.cargarFuentes();
            Parent root = FXMLLoader.load(getClass().getResource(rutaFxml));
            Scene scene = new Scene(root);
            EstilosApp.aplicar(scene, estilos);
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir: " + rutaFxml
                    + "\n" + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            alert.showAndWait();
        }
    }

    // =========================================================
    // ABRIR FACTURACIÓN
    // =========================================================
    @FXML
    private void handleFacturar() {
        abrirVentana("/progs/fxml/VentanaFacturacion.fxml", "Facturación", CSS_COMUN);
    }

    // =========================================================
    // ABRIR LISTA DE ASISTENCIA
    // =========================================================
    @FXML
    private void handleAbrirAsistencia() {
        abrirVentana("/progs/fxml/Asistencia.fxml", "Lista de Asistencia", CSS_COMUN);
    }

    // =========================================================
    // CERRAR SESION: cierra la ventana actual y regresa al login
    // =========================================================
    @FXML
    private void cerrarSesion() {
        irAlLogin((Stage) btn_cerrarsesion.getScene().getWindow());
    }

    // Regresa al login con los CSS exactos que usa LoginController
    private void irAlLogin(Stage stage) {
        try {
            EstilosApp.cargarFuentes();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/progs/fxml/VistaInicioSesion.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            EstilosApp.aplicar(scene,
                CSS.PANELES,
                CSS.FUENTES,
                CSS.BOTONES,
                CSS.TABLA_1,
                CSS.IMAGEN,
                CSS.DIALOGO,
                CSS.TEXTO,
                CSS.JUEGO,
                CSS.TABLA_2,
                CSS.TEXTFIELD,
                CSS.GESTIONEMPLEADOS,
                CSS.VISTAMESERO
            );
            stage.setScene(scene);
            stage.setTitle("Restaurante - Iniciar Sesion");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            System.err.println("Error al regresar al login: " + ex.getMessage());
        }
    }

}

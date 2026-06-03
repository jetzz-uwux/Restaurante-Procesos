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

public class RecepcionistaController {

    // =========================================================
    // NODOS DEL FXML
    // btn_verMesas, btn_reservas y btn_listaEspera no tenían
    // @FXML — JavaFX no los inyectaba y los botones no funcionaban
    // =========================================================
    @FXML private Button btn_asistencia;
    @FXML private Button btn_reservas;       // ← faltaba @FXML
    @FXML private Button btn_listaEspera;    // ← faltaba @FXML
    @FXML private Button btn_cerrarsesion;
    @FXML private Button btn_ayuda;

    // CSS comunes para todas las ventanas de la recepcionista
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
    // ABRIR GESTIÓN DE RESERVAS
    // =========================================================
    @FXML
    private void handleReservas() {
        abrirVentana("/progs/fxml/VistaGestionReservas.fxml", "Reservas", CSS_COMUN);
    }

    // =========================================================
    // ABRIR LISTA DE ESPERA
    // =========================================================
    @FXML
    private void handleLista() {
        abrirVentana("/progs/fxml/VentanaEspera.fxml", "Lista de Espera", CSS_COMUN);
    }

    // =========================================================
    // ABRIR REGISTRO DE ASISTENCIA
    // =========================================================
    @FXML
    private void handleRegistrar() {
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

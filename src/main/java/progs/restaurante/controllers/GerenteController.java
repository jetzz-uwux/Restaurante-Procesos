package progs.restaurante.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import progs.restaurante.lib.EstilosApp;
import progs.restaurante.lib.EstilosApp.CSS;

public class GerenteController {

    @FXML private Button btn_empleados;
    @FXML private Button btn_ayuda;
    @FXML private Button btn_cerrarsesion;
    @FXML private Button btn_reportes;
    @FXML private Button btn_inventario;
    @FXML private Button btn_asistencia;

    // CSS comunes para todas las ventanas del gerente
    private static final CSS[] CSS_COMUN = {
        CSS.JUEGO, CSS.FUENTES, CSS.BOTONES,
        CSS.TEXTFIELD, CSS.IMAGEN, CSS.TEXTO, CSS.TABLA_1, CSS.TABLA_2
    };

    // Método auxiliar: abre cualquier ventana con CSS y título dados
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
            System.err.println("Error abriendo: " + rutaFxml);
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirReportes(ActionEvent event) {
        abrirVentana("/progs/fxml/VentanaVentas.fxml", "Reportes", CSS_COMUN);
    }

    @FXML
    private void abrirInventario(ActionEvent event) {
        abrirVentana("/progs/fxml/AlmacenVista.fxml", "Inventario", CSS_COMUN);
    }

    @FXML
    private void handleGestionar(ActionEvent event) {
        abrirVentana("/progs/fxml/VentanaGestionMenu.fxml", "Gestion de Menu", CSS_COMUN);
    }

    @FXML
    private void abrirAsistencia(ActionEvent event) {
        abrirVentana("/progs/fxml/Asistencia.fxml", "Control de Asistencia", CSS_COMUN);
    }

    @FXML
    private void handleEmpleados(ActionEvent event) {
        abrirVentana(
            "/progs/fxml/GestionEmpleados.fxml",
            "Gestion de Empleados",
            CSS.JUEGO, CSS.FUENTES, CSS.BOTONES,
            CSS.TEXTFIELD, CSS.IMAGEN, CSS.TEXTO, CSS.GESTIONEMPLEADOS
        );
    }

    // =====================================================================
    // CERRAR SESION: regresa al login con todos los CSS exactos
    // =====================================================================
    @FXML
    private void cerrarSesion(ActionEvent event) {
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

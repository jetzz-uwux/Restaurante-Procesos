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
import static progs.restaurante.lib.EstilosApp.cargarFuentes;

public class GerenteController {
    
    private static Scene scene;
    @FXML
    private Button btn_empleados;

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
            scene = new Scene(root);
            EstilosApp.aplicar(scene,
                CSS.JUEGO,
                CSS.FUENTES,
                CSS.BOTONES,
                CSS.TEXTFIELD,
                CSS.IMAGEN,
                CSS.TEXTO,
                CSS.TABLA_1,
                CSS.TABLA_2
            );
            cargarFuentes();
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
            scene = new Scene(root);
            EstilosApp.aplicar(scene,
                CSS.JUEGO,
                CSS.FUENTES,
                CSS.BOTONES,
                CSS.TEXTFIELD,
                CSS.IMAGEN,
                CSS.TEXTO,
                CSS.TABLA_1,
                CSS.TABLA_2
            );
            cargarFuentes();
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
            scene = new Scene(root);
            EstilosApp.aplicar(scene,
                CSS.JUEGO,
                CSS.FUENTES,
                CSS.BOTONES,
                CSS.TEXTFIELD,
                CSS.IMAGEN,
                CSS.TEXTO,
                CSS.TABLA_1,
                CSS.TABLA_2
            );
            cargarFuentes();
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
            scene = new Scene(root);
            EstilosApp.aplicar(scene,
                CSS.JUEGO,
                CSS.FUENTES,
                CSS.BOTONES,
                CSS.TEXTFIELD,
                CSS.IMAGEN,
                CSS.TEXTO,
                CSS.TABLA_1,
                CSS.TABLA_2
            );
            cargarFuentes();
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

    @FXML
    private void handleEmpleados(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/progs/fxml/GestionEmpleados.fxml")
            );

            Stage stage = new Stage();
            stage.setTitle("Gestion Empleados");
            scene = new Scene(root);
            EstilosApp.aplicar(scene,
                CSS.JUEGO,
                CSS.FUENTES,
                CSS.BOTONES,
                CSS.TEXTFIELD,
                CSS.IMAGEN,
                CSS.TEXTO,
                CSS.GESTIONEMPLEADOS
            );
            cargarFuentes();
            
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

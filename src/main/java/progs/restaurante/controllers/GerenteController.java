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

    // =====================================================================
    // NODOS DEL FXML
    // Cada @FXML debe coincidir EXACTAMENTE con el fx:id del FXML.
    // Si falta alguno aquí JavaFX lanza NullPointerException silencioso
    // y la ventana aparece vacía o no carga el CSS correctamente.
    // =====================================================================
    @FXML private Button btn_empleados;
    @FXML private Button btn_ayuda;
    @FXML private Button btn_cerrarsesion;
    @FXML private Button btn_reportes;
    @FXML private Button btn_inventario;
    @FXML private Button btn_asistencia;   // ← FALTABA: está en el FXML pero no aquí

    // =====================================================================
    // MÉTODO AUXILIAR CENTRALIZADO
    // En lugar de repetir 20 líneas en cada método, usamos este helper.
    //
    // ORDEN CORRECTO:
    //   1. cargarFuentes()  → registra las fuentes ANTES de mostrar
    //   2. new Scene(root)  → crea la escena
    //   3. aplicar(scene)   → adjunta los CSS a la escena ya creada
    //   4. stage.setScene() → asigna la escena al stage  ← FALTABA en varios métodos
    //   5. stage.show()     → muestra la ventana
    // =====================================================================
    private void abrirVentana(String rutaFxml, String titulo, CSS... estilos) {
        try {
            // 1. Cargar fuentes primero (si ya están cargadas, no hace nada de más)
            EstilosApp.cargarFuentes();

            // 2. Cargar el FXML y crear la escena
            Parent root = FXMLLoader.load(getClass().getResource(rutaFxml));
            Scene scene = new Scene(root);

            // 3. Aplicar los CSS a la escena
            EstilosApp.aplicar(scene, estilos);

            // 4. Crear el Stage, asignarle la escena y mostrarlo
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(scene);   // ← paso crítico que faltaba en varios métodos
            stage.show();

        } catch (IOException e) {
            // Mostramos en consola qué archivo falló exactamente
            System.err.println("Error abriendo: " + rutaFxml);
            e.printStackTrace();
        }
    }

    // CSS comunes que se usan en casi todas las ventanas
    // Los agrupamos aquí para no repetirlos en cada método
    private static final CSS[] CSS_COMUN = {
        CSS.JUEGO,
        CSS.FUENTES,
        CSS.BOTONES,
        CSS.TEXTFIELD,
        CSS.IMAGEN,
        CSS.TEXTO,
        CSS.TABLA_1,
        CSS.TABLA_2
    };

    // =====================================================================
    // ABRIR REPORTES
    // =====================================================================
    @FXML
    private void abrirReportes(ActionEvent event) {
        abrirVentana("/progs/fxml/VentanaVentas.fxml", "Reportes", CSS_COMUN);
    }

    // =====================================================================
    // ABRIR INVENTARIO
    // =====================================================================
    @FXML
    private void abrirInventario(ActionEvent event) {
        abrirVentana("/progs/fxml/AlmacenVista.fxml", "Inventario", CSS_COMUN);
    }

    // =====================================================================
    // ABRIR GESTIÓN DE MENÚ
    // =====================================================================
    @FXML
    private void handleGestionar(ActionEvent event) {
        abrirVentana("/progs/fxml/VentanaGestionMenu.fxml", "Gestión de Menú", CSS_COMUN);
    }

    // =====================================================================
    // ABRIR CONTROL DE ASISTENCIA
    // =====================================================================
    @FXML
    private void abrirAsistencia(ActionEvent event) {
        abrirVentana("/progs/fxml/Asistencia.fxml", "Control de Asistencia", CSS_COMUN);
    }

    // =====================================================================
    // ABRIR GESTIÓN DE EMPLEADOS
    // Usa su propio CSS en lugar del conjunto común
    // =====================================================================
    @FXML
    private void handleEmpleados(ActionEvent event) {
        abrirVentana(
            "/progs/fxml/GestionEmpleados.fxml",
            "Gestión de Empleados",
            CSS.JUEGO,
            CSS.FUENTES,
            CSS.BOTONES,
            CSS.TEXTFIELD,
            CSS.IMAGEN,
            CSS.TEXTO,
            CSS.GESTIONEMPLEADOS
        );
    }

    // =====================================================================
    // CERRAR SESIÓN
    // =====================================================================
    @FXML
    private void cerrarSesion(ActionEvent event) {
        // Obtiene la ventana actual desde cualquier botón y la cierra
        Stage stage = (Stage) btn_cerrarsesion.getScene().getWindow();
        stage.close();
    }
}

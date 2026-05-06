package progs.restaurante.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import progs.restaurante.lib.EstilosApp;
import progs.restaurante.lib.EstilosApp.CSS;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  DialogoController.java — Controla la ventana emergente      ║
 * ║                                                              ║
 * ║  Dos modos:                                                  ║
 * ║    • ERROR   → panel rojo  + ícono ⚠️  + título "¡Error!"   ║
 * ║    • EXITO   → panel verde + ícono ✅  + título "¡Éxito!"   ║
 * ║                                                              ║
 * ║  Uso desde LoginController:                                  ║
 * ║    DialogoController.mostrarError(stage, "Tu mensaje");      ║
 * ║    DialogoController.mostrarExito(stage, "Tu mensaje");      ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class DialogoController implements Initializable {

    // ══════════════════════════════════════════════════════
    //  NODOS INYECTADOS DESDE EL FXML
    // ══════════════════════════════════════════════════════

    @FXML private VBox  panelDialogo;   // Panel principal (cambia de color según el tipo)
    @FXML private Label iconoLabel;     // Emoji grande (⚠️ o ✅)
    @FXML private Label tituloLabel;    // "¡Error!" o "¡Éxito!"
    @FXML private Label mensajeLabel;   // El texto descriptivo
    @FXML private Button botonCerrar;   // Botón "¡Entendido!"

    // ══════════════════════════════════════════════════════
    //  TIPOS DE DIÁLOGO
    // ══════════════════════════════════════════════════════

    public enum Tipo {
        ERROR,   // Panel rojo
        EXITO    // Panel verde
    }

    // ══════════════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // El panel comienza con estilo neutro.
        // El tipo se aplica en configurar() antes de mostrarlo.
    }

    // ══════════════════════════════════════════════════════
    //  CONFIGURAR EL DIÁLOGO (llamado antes de show)
    // ══════════════════════════════════════════════════════

    /**
     * Aplica el tipo, ícono, título y mensaje al diálogo.
     * Se llama desde los métodos estáticos mostrarError/mostrarExito.
     */
    private void configurar(Tipo tipo, String mensaje) {

        mensajeLabel.setText(mensaje);

        if (tipo == Tipo.ERROR) {
            // Modo ERROR — panel rojo
            panelDialogo.getStyleClass().add("panel-error");
            iconoLabel.setText("⚠️");
            tituloLabel.setText("¡Ups! Algo salió mal");
            botonCerrar.setText("¡Intentar de nuevo!");

        } else {
            // Modo ÉXITO — panel verde
            panelDialogo.getStyleClass().add("panel-exito");
            iconoLabel.setText("✅");
            tituloLabel.setText("¡Bienvenido a la Cocina!");
            botonCerrar.setText("¡A Cocinar! 🍳");
        }
    }

    // ══════════════════════════════════════════════════════
    //  CERRAR EL DIÁLOGO
    // ══════════════════════════════════════════════════════

    @FXML
    private void cerrarDialogo() {
        // Obtener el Stage de este diálogo y cerrarlo
        Stage stage = (Stage) botonCerrar.getScene().getWindow();
        stage.close();
    }

    // ══════════════════════════════════════════════════════
    //  MÉTODOS ESTÁTICOS DE FÁBRICA
    //  Son los que llamas desde LoginController con una sola línea
    // ══════════════════════════════════════════════════════

    /**
     * Muestra una ventana emergente de ERROR con el mensaje dado.
     *
     * Ejemplo de uso en LoginController:
     *   DialogoController.mostrarError(stagePadre, "Usuario o contraseña incorrectos.");
     *
     * @param stagePadre  El Stage del login (para centrar el diálogo sobre él)
     * @param mensaje     El texto que verá el usuario
     */
    public static void mostrarError(Stage stagePadre, String mensaje) {
        mostrar(stagePadre, Tipo.ERROR, mensaje);
    }

    /**
     * Muestra una ventana emergente de ÉXITO con el mensaje dado.
     *
     * Ejemplo de uso en LoginController:
     *   DialogoController.mostrarExito(stagePadre, "¡Bienvenido, Chef Juan!");
     *
     * @param stagePadre  El Stage del login (para centrar el diálogo sobre él)
     * @param mensaje     El texto que verá el usuario
     */
    public static void mostrarExito(Stage stagePadre, String mensaje) {
        mostrar(stagePadre, Tipo.EXITO, mensaje);
    }

    // ══════════════════════════════════════════════════════
    //  MÉTODO INTERNO: construye y muestra el Stage
    // ══════════════════════════════════════════════════════

    /**
     * Carga el FXML, configura el tipo y muestra la ventana modal.
     *
     * MODAL = el usuario no puede interactuar con el login
     *         hasta que cierre este diálogo.
     * TRANSPARENT = sin la barra de título del sistema operativo,
     *               para que se vea el panel dorado del juego.
     */
    private static void mostrar(Stage stagePadre, Tipo tipo, String mensaje) {
        try {
            // 1. Cargar el FXML del diálogo
            FXMLLoader loader = new FXMLLoader(
                DialogoController.class.getResource("/progs/fxml/dialogo.fxml")
            );
            Parent root = loader.load();

            // 2. Obtener el controlador y configurarlo con tipo + mensaje
            DialogoController ctrl = loader.getController();
            ctrl.configurar(tipo, mensaje);

            // 3. Crear el nuevo Stage (ventana)
            Stage dialogoStage = new Stage();
            
           dialogoStage.initStyle(StageStyle.TRANSPARENT);

            // APPLICATION_MODAL = bloquea el login mientras está abierto
            dialogoStage.initModality(Modality.APPLICATION_MODAL);

            // Asociar con el stage padre para que aparezca centrado sobre él
            dialogoStage.initOwner(stagePadre);

            // Fondo transparente para que se vea solo el panel dorado
            Scene escena = new Scene(root);
            escena.setFill(javafx.scene.paint.Color.TRANSPARENT);
            EstilosApp.aplicar(escena,
                CSS.DIALOGO
            );
            dialogoStage.setScene(escena);

            // 4. Centrar sobre el stage padre
            dialogoStage.setOnShown(e -> {
                dialogoStage.setX(
                    stagePadre.getX() + (stagePadre.getWidth()  - dialogoStage.getWidth())  / 2
                );
                dialogoStage.setY(
                    stagePadre.getY() + (stagePadre.getHeight() - dialogoStage.getHeight()) / 2
                );
            });

            // 5. Mostrar y ESPERAR a que el usuario cierre el diálogo
            //    showAndWait() bloquea la ejecución aquí hasta que se cierre
            dialogoStage.showAndWait();

        } catch (IOException ex) {
            // Si falla la carga del FXML, al menos mostrar un Alert nativo
            System.err.println("Error cargando dialogo.fxml: " + ex.getMessage());
            new javafx.scene.control.Alert(
                tipo == Tipo.ERROR
                    ? javafx.scene.control.Alert.AlertType.ERROR
                    : javafx.scene.control.Alert.AlertType.INFORMATION,
                mensaje
            ).showAndWait();
        }
    }
}

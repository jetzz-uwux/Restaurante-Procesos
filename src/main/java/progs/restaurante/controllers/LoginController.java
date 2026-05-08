package progs.restaurante.controllers;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import progs.restaurante.Empleado;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import progs.restaurante.Empleados.Administrador;
import progs.restaurante.Empleados.Cajero;
import progs.restaurante.Empleados.Chef;
import progs.restaurante.Empleados.Gerente;
import progs.restaurante.Empleados.Mesero;
import progs.restaurante.Empleados.Recepcionista;
import progs.restaurante.datos.ConexionBD;
import progs.restaurante.lib.EstilosApp;
import progs.restaurante.lib.EstilosApp.CSS;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  LoginController.java — Controlador del Login estilo "Cocina Game"  ║
 * ║                                                                      ║
 * ║  Los mensajes de error y éxito se muestran en ventanas emergentes    ║
 * ║  personalizadas (DialogoController) en vez de cajas inline.          ║
 * ║                                                                      ║
 * ║  Flujo:                                                              ║
 * ║   1. Validar campos vacíos → DialogoController.mostrarError(...)     ║
 * ║   2. Buscar en MySQL → Empleado o null                               ║
 * ║   3. null → DialogoController.mostrarError(...)                      ║
 * ║   4. Empleado OK → DialogoController.mostrarExito(...) → dashboard   ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class LoginController implements Initializable {

    // ══════════════════════════════════════════════════════
    //  NODOS INYECTADOS DESDE EL FXML
    //  NOTA: ya no necesitamos cajaError, cajaExito, textoError, textoExito
    //        porque los mensajes van en ventanas emergentes separadas.
    // ══════════════════════════════════════════════════════

    @FXML private TextField     txt_user;    // Campo texto del nombre de usuario
    @FXML private PasswordField txt_contrasena; // Campo contraseña (oculta el texto)
    @FXML private Button        btn_login;      // Botón ¡A COCINAR!

    // ══════════════════════════════════════════════════════
    //  CONSTANTES: tabla y columnas de MySQL
    // ══════════════════════════════════════════════════════

    private static final String TABLA          = "empleados";
    private static final String COL_ID         = "id_empleado";
    private static final String COL_NOMBRE     = "nombre";
    private static final String COL_USUARIO    = "usuario";
    private static final String COL_CONTRASENA = "contrasena";
    private static final String COL_ROL        = "rol";

    // ══════════════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Quitar el borde rojo de error cuando el usuario empiece a escribir
        txt_user.textProperty().addListener((obs, viejo, nuevo) ->
            txt_user.getStyleClass().remove("campo-error")
        );

        txt_contrasena.textProperty().addListener((obs, viejo, nuevo) ->
            txt_contrasena.getStyleClass().remove("campo-error")
        );
    }

    // ══════════════════════════════════════════════════════
    //  MÉTODO PRINCIPAL: manejarLogin
    //  Se dispara al presionar el botón O al dar Enter en cualquier campo
    // ══════════════════════════════════════════════════════

    @FXML
    private void manejarLogin(ActionEvent evento) {

        // Obtener el Stage actual (lo necesitamos para centrar los diálogos)
        Stage stagePadre = (Stage) ((Node) evento.getSource()).getScene().getWindow();

        // PASO 1 — Leer valores
        String usuario    = txt_user.getText().trim();
        String contrasena = txt_contrasena.getText(); // NO trim() en contraseñas

        // PASO 2 — Validar campos (sin tocar la BD)
        if (!validarCampos(usuario, contrasena, stagePadre)) {
            return;
        }

        // PASO 3 — Bloquear botón mientras trabaja el hilo
        btn_login.setDisable(true);
        btn_login.setText("⏳ Entrando...");

        // PASO 4 — Consultar la BD en hilo separado (evita congelar la UI)
        Task<Empleado> tareaLogin = new Task<>() {
            @Override
            protected Empleado call() throws Exception {
                return buscarEmpleadoEnBD(usuario, contrasena);
            }
        };

        // ── Consulta terminó SIN excepción ───────────────────────────────
        tareaLogin.setOnSucceeded(e -> {
            Empleado empleado = tareaLogin.getValue();

            if (empleado == null) {
                // Credenciales incorrectas → ventana emergente ROJA
                marcarCamposError();
                restaurarBoton();
                DialogoController.mostrarError(
                    stagePadre,
                    "El usuario o la contraseña no son correctos.\n¡Inténtalo de nuevo, Chef! 🍅"
                );

            } else {
                // Login correcto → ventana emergente VERDE, luego navegar
                restaurarBoton();
                DialogoController.mostrarExito(
                    stagePadre,
                    "¡Hola, " + empleado.getNombre() + "!\nPreparado para cocinar como " + capitalizar(empleado.getRol()) + " 🍳"
                );
                // showAndWait() del diálogo bloquea hasta que el usuario
                // cierra la ventana, luego continuamos con la navegación
                navegarSegunRol(empleado, stagePadre);
            }
        });

        // ── Consulta terminó CON excepción (BD apagada, error de red, etc.) ──
        tareaLogin.setOnFailed(e -> {
            Throwable error = tareaLogin.getException();
            System.err.println("Error en login: " + error.getMessage());

            String msg = error.getMessage() != null ? error.getMessage() : "";
            String mensajeUsuario;

            if (msg.contains("Communications link") || msg.contains("connect")) {
                mensajeUsuario = "No se pudo conectar al servidor.\n¿Está MySQL encendido? 🔌";
            } else if (msg.contains("Access denied")) {
                mensajeUsuario = "Error de acceso a la base de datos.\nContacta al administrador.";
            } else if (msg.contains("Unknown column") || msg.contains("Table")) {
                mensajeUsuario = "Error de configuración en la BD.\nContacta al administrador.";
            } else {
                mensajeUsuario = "Ocurrió un error inesperado.\nPor favor intenta de nuevo.";
            }

            restaurarBoton();
            DialogoController.mostrarError(stagePadre, mensajeUsuario);
        });

        // Iniciar hilo
        Thread hilo = new Thread(tareaLogin);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ══════════════════════════════════════════════════════
    //  CONSULTA A MySQL
    // ══════════════════════════════════════════════════════

    /**
     * Busca en MySQL un empleado con el usuario y contraseña dados.
     * Usa PreparedStatement para prevenir SQL Injection.
     *
     * Como Empleado es abstracta, lee primero el ROL del ResultSet
     * y luego instancia la subclase concreta que corresponde.
     *
     * @return La subclase correcta de Empleado, o null si no existe/no coincide
     */
    private Empleado buscarEmpleadoEnBD(String usuario, String contrasena) throws SQLException {

        String sql = "SELECT "
                   + COL_ID + ", " + COL_NOMBRE + ", "
                   + COL_USUARIO + ", " + COL_CONTRASENA + ", " + COL_ROL
                   + " FROM "  + TABLA
                   + " WHERE " + COL_USUARIO    + " = ?"
                   + "   AND " + COL_CONTRASENA + " = ?"
                   + " LIMIT 1";

        Connection conn = ConexionBD.getConexion();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            stmt.setString(2, contrasena);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    // 1. Leer el rol ANTES de instanciar
                    //    (lo normalizamos a minúsculas para que el switch no falle
                    //     si en la BD hay "CAJERO", "Cajero", etc.)
                    String rol = rs.getString(COL_ROL).toLowerCase();

                    // 2. Instanciar la subclase concreta según el rol
                    //    Empleado es abstracta → no podemos hacer new Empleado()
                    //    pero SÍ podemos usar la referencia polimórfica: Empleado e = new Chef()
                    Empleado empleado;
                    switch (rol) {
                        case "chef":          empleado = new Chef();          break;
                        case "cajero":        empleado = new Cajero();        break;
                        case "mesero":        empleado = new Mesero();        break;
                        case "recepcionista": empleado = new Recepcionista(); break;
                        case "gerente":       empleado = new Gerente();       break;
                        case "administrador": empleado = new Administrador(); break;
                        default:
                            // Rol en la BD que no tiene subclase definida
                            // Lanzamos excepción para que setOnFailed lo capture
                            throw new IllegalArgumentException(
                                "Rol desconocido en la BD: '" + rol + "'"
                            );
                    }

                    // 3. Poblar los atributos comunes usando los setters de Empleado
                    //    (heredados por todas las subclases)
                    empleado.setIdEmpleado(rs.getString(COL_ID));
                    empleado.setNombre(rs.getString(COL_NOMBRE));
                    empleado.setUsuario(rs.getString(COL_USUARIO));
                    empleado.setContrasena(rs.getString(COL_CONTRASENA));
                    empleado.setRol(rol); // ya está en minúsculas

                    return empleado; // ✅ subclase correcta lista para usar

                } else {
                    return null; // ❌ credenciales incorrectas
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //  NAVEGACIÓN SEGÚN ROL
    // ══════════════════════════════════════════════════════

    /**
     * Carga el FXML del dashboard correspondiente al rol del empleado.
     * Se llama DESPUÉS de que el diálogo de éxito se cierra (showAndWait).
     */
    private void navegarSegunRol(Empleado empleado, Stage stage) {

        String rol = empleado.getRol();
        String fxmlDestino;

        switch (rol) {
            case "administrador": fxmlDestino = "/progs/fxml/VentanaAdministrador.fxml"; break;
            case "gerente":       fxmlDestino = "/progs/fxml/VistaGerente.fxml";       break;
            case "cajero":        fxmlDestino = "/progs/fxml/VistaCajero.fxml";        break;
            case "mesero":        fxmlDestino = "/progs/fxml/VistaMesero.fxml";        break;
            case "recepcionista": fxmlDestino = "/progs/fxml/VistaRecepcionista.fxml"; break;
            case "chef":          fxmlDestino = "/progs/fxml/VentanaChef.fxml";          break;
            default:
                DialogoController.mostrarError(
                    stage,
                    "Rol desconocido: '" + rol + "'.\nContacta al administrador del sistema."
                );
                return;
        }

        // Navegar en el hilo de JavaFX
        // (navegarSegunRol ya se llama desde setOnSucceeded, que está en el FX thread)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlDestino));
            Parent root = loader.load();

            // ── PASAR EL OBJETO EMPLEADO AL DASHBOARD ────────────────
            // Si tu controlador de dashboard tiene recibirEmpleado(Empleado e),
            // descomenta esto:
            //
            //   Object ctrl = loader.getController();
            //   if (ctrl instanceof DashboardAdministradorController) {
            //       ((DashboardAdministradorController) ctrl).recibirEmpleado(empleado);
            //   }
            // ─────────────────────────────────────────────────────────
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
                CSS.TEXTFIELD
            );
            stage.setScene(scene);
            
            stage.setTitle("Sistema de Cocina — " + capitalizar(rol));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException ex) {
            System.err.println("Error cargando " + fxmlDestino + ": " + ex.getMessage());
            DialogoController.mostrarError(
                stage,
                "No se pudo cargar la pantalla de " + capitalizar(rol) + ".\nContacta al administrador."
            );
        }
    }

    // ══════════════════════════════════════════════════════
    //  VALIDACIONES DE UI
    // ══════════════════════════════════════════════════════

    /**
     * Verifica que los campos sean válidos antes de consultar la BD.
     * Si hay error, muestra el diálogo rojo y marca el campo con borde rojo.
     *
     * @return true si todo está bien, false si hay algún problema
     */
    private boolean validarCampos(String usuario, String contrasena, Stage stagePadre) {

        if (usuario.isEmpty() && contrasena.isEmpty()) {
            marcarCampoError(txt_user);
            marcarCampoError(txt_contrasena);
            DialogoController.mostrarError(stagePadre,
                "Ingresa tu nombre de Chef\ny tu contraseña secreta. 🍅");
            return false;
        }

        if (usuario.isEmpty()) {
            marcarCampoError(txt_user);
            DialogoController.mostrarError(stagePadre,
                "¡Falta tu nombre de Chef!\nEscríbelo en el campo USER. 👨‍🍳");
            return false;
        }

        if (contrasena.isEmpty()) {
            marcarCampoError(txt_contrasena);
            DialogoController.mostrarError(stagePadre,
                "¡Falta tu contraseña!\nEscríbela en el campo KEY. 🔑");
            return false;
        }

        return true;
    }

    // ══════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════

    private void marcarCampoError(TextField campo) {
        if (!campo.getStyleClass().contains("campo-error")) {
            campo.getStyleClass().add("campo-error");
        }
    }

    private void marcarCamposError() {
        marcarCampoError(txt_user);
        marcarCampoError(txt_contrasena);
    }

    private void restaurarBoton() {
        Platform.runLater(() -> {
            btn_login.setDisable(false);
            btn_login.setText("Iniciar Sesión");
        });
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}


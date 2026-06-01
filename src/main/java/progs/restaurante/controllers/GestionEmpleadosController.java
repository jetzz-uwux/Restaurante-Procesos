package progs.restaurante.controllers; // ← Tu paquete del proyecto

// =====================================================================
// IMPORTACIONES NECESARIAS
// JavaFX: para manejar la interfaz gráfica
// SQL: para conectarnos con la base de datos MySQL
// =====================================================================
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import progs.restaurante.datos.ConexionBD;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

/**
 * CONTROLADOR DE GESTIÓN DE EMPLEADOS
 * -------------------------------------
 * Esta clase maneja toda la lógica de la pantalla:
 *  - Mostrar la lista de empleados (READ)
 *  - Agregar nuevos empleados    (CREATE / ALTA)
 *  - Modificar empleados         (UPDATE / CAMBIO)
 *  - Eliminar empleados          (DELETE / BAJA)
 *
 * Implements Initializable → significa que JavaFX llamará al método
 * initialize() automáticamente cuando cargue la pantalla.
 */
public class GestionEmpleadosController implements Initializable {

    // =====================================================================
    // CONEXIÓN A LA IZQUIERDA: TABLA DE PERSONAL ACTIVO
    // @FXML conecta esta variable con el elemento que tiene ese fx:id en el FXML
    // =====================================================================

    @FXML private Label lblContador;          // Muestra "PERSONAL ACTIVO (X/∞)"
    @FXML private TableView<Empleado> tablaEmpleados;  // La tabla principal
    @FXML private TableColumn<Empleado, Integer> colId;        // Columna ID
    @FXML private TableColumn<Empleado, String>  colNombre;    // Columna Nombre
    @FXML private TableColumn<Empleado, String>  colUsuario;   // Columna Usuario
    @FXML private TableColumn<Empleado, String>  colRol;       // Columna Rol/Puesto

    // =====================================================================
    // PANEL DE ACCIÓN (derecha): Campos del formulario
    // =====================================================================
    @FXML private Label lblTituloPanel;       // "ALTA EMPLEADO" o "EDITAR: #001 - Chef Hoshii"
    @FXML private TextField txtNombre;        // Campo para escribir el nombre
    @FXML private TextField txtUsuario;       // Campo para el usuario (login)
    @FXML private PasswordField txtContrasena;// Campo para la contraseña (oculta los caracteres)
    @FXML private ComboBox<String> cmbRol;    // Lista desplegable de roles

    // =====================================================================
    // BOTONES DE ACCIÓN
    // =====================================================================
    @FXML private Button btnContratar;   // Verde: ALTA (insertar nuevo)
    @FXML private Button btnActualizar;  // Amarillo: MODIFICAR (actualizar)
    @FXML private Button btnDespedir;    // Rojo: BAJA (eliminar)
    @FXML private Button btnLimpiar;     // Gris: Limpiar formulario

    // =====================================================================
    // VARIABLES INTERNAS DEL CONTROLADOR
    // =====================================================================

    // Lista observable: JavaFX la "escucha" y actualiza la tabla automáticamente
    private ObservableList<Empleado> listaEmpleados = FXCollections.observableArrayList();

    // Guarda el ID del empleado seleccionado (-1 = ninguno seleccionado)
    private int idSeleccionado = -1;

    // =====================================================================
    // MÉTODO INITIALIZE
    // Se ejecuta automáticamente cuando JavaFX carga esta pantalla
    // =====================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // 1. Llenar el ComboBox con los roles disponibles
        cmbRol.setItems(FXCollections.observableArrayList(
                "Chef", "Cajero", "Gerente", "Mesero", "Recepcionista", "Administrador"
        ));
        cmbRol.getSelectionModel().selectFirst(); // Selecciona "Chef" por defecto

        // 2. Configurar las columnas de la tabla
        //    PropertyValueFactory("nombre") busca el método getNombre() en la clase Empleado
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        // 3. Agregar badge de color para el rol (chip colorido)
        agregarBadgeRol();

        // 4. Conectar la lista a la tabla
        tablaEmpleados.setItems(listaEmpleados);

        // 5. Cargar los datos desde la base de datos
        cargarEmpleados();

        // 6. Escuchar cuando el usuario hace clic en una fila de la tabla
        tablaEmpleados.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        // Cuando selecciona una fila, llena el formulario con sus datos
                        llenarFormulario(seleccionado);
                    }
                }
        );

        // 7. Estado inicial de los botones
        //    Al inicio solo "CONTRATAR" está disponible (modo alta)
        btnActualizar.setDisable(true);
        btnDespedir.setDisable(true);
    }

    // =====================================================================
    // CARGAR EMPLEADOS DESDE LA BASE DE DATOS
    // Ejecuta: SELECT * FROM empleados
    // =====================================================================
    private void cargarEmpleados() {
        listaEmpleados.clear(); // Limpia la lista antes de recargar

        // Obtenemos la conexión usando TU método getConexion() de conexionBD
        try (Connection con = ConexionBD.getConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_empleado, nombre, usuario, contrasena, rol FROM empleados")) {

            // Recorremos cada fila del resultado
            while (rs.next()) {
                Empleado emp = new Empleado(
                        rs.getInt("id_empleado"), // nombre real de la columna en tu BD
                        rs.getString("nombre"),
                        rs.getString("usuario"),
                        rs.getString("contrasena"),
                        rs.getString("rol")
                );
                listaEmpleados.add(emp); // Agrega a la lista observable
            }

        } catch (SQLException e) {
            mostrarAlerta("Error al cargar empleados:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }

        // Actualiza el contador del encabezado, ej: "PERSONAL ACTIVO (4)"
        lblContador.setText("PERSONAL ACTIVO (" + listaEmpleados.size() + ")");
    }

    // =====================================================================
    // ALTA: CONTRATAR NUEVO EMPLEADO
    // Ejecuta: INSERT INTO empleados (nombre, usuario, contrasena, rol)
    // =====================================================================
    @FXML
    private void accionContratar() {

        // Validar que los campos no estén vacíos
        if (!validarCampos()) return;

        String nombre    = txtNombre.getText().trim();
        String usuario   = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText().trim();
        String rol       = cmbRol.getValue();

        // Confirmación antes de insertar
        if (!confirmar("¿Deseas contratar a " + nombre + " como " + rol + "?")) return;

        String sql = "INSERT INTO empleados (nombre, usuario, contrasena, rol) VALUES (?, ?, ?, ?)";

        // "try-with-resources" cierra la conexión automáticamente al terminar
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // El "?" se reemplaza con los valores reales (evita inyección SQL)
            ps.setString(1, nombre);
            ps.setString(2, usuario);
            ps.setString(3, contrasena);
            ps.setString(4, rol);

            int filasAfectadas = ps.executeUpdate(); // Ejecuta el INSERT

            if (filasAfectadas > 0) {
                mostrarAlerta("✅ Empleado contratado exitosamente.", Alert.AlertType.INFORMATION);
                limpiarFormulario();    // Limpia el formulario
                cargarEmpleados();      // Recarga la tabla
            }

        } catch (SQLException e) {
            mostrarAlerta("Error al contratar empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // =====================================================================
    // CAMBIO: ACTUALIZAR / MODIFICAR EMPLEADO
    // Ejecuta: UPDATE empleados SET ... WHERE id = ?
    // =====================================================================
    @FXML
    private void accionActualizar() {

        // Verificar que hay un empleado seleccionado
        if (idSeleccionado == -1) {
            mostrarAlerta("⚠️ Selecciona un empleado de la tabla para modificar.", Alert.AlertType.WARNING);
            return;
        }

        if (!validarCampos()) return;

        String nombre    = txtNombre.getText().trim();
        String usuario   = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText().trim();
        String rol       = cmbRol.getValue();

        if (!confirmar("¿Deseas actualizar los datos de este empleado?")) return;

        // Si la contraseña está vacía, no la actualizamos (se conserva la anterior)
        String sql;
        if (contrasena.isEmpty()) {
            sql = "UPDATE empleados SET nombre=?, usuario=?, rol=? WHERE id_empleado=?";
        } else {
            sql = "UPDATE empleados SET nombre=?, usuario=?, contrasena=?, rol=? WHERE id_empleado=?";
        }

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, usuario);

            if (contrasena.isEmpty()) {
                ps.setString(3, rol);
                ps.setInt(4, idSeleccionado);
            } else {
                ps.setString(3, contrasena);
                ps.setString(4, rol);
                ps.setInt(5, idSeleccionado);
            }

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarAlerta("✅ Empleado actualizado correctamente.", Alert.AlertType.INFORMATION);
                limpiarFormulario();
                cargarEmpleados();
            }

        } catch (SQLException e) {
            mostrarAlerta("Error al actualizar empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // =====================================================================
    // BAJA: DESPEDIR / ELIMINAR EMPLEADO
    // Ejecuta: DELETE FROM empleados WHERE id = ?
    // =====================================================================
    @FXML
    private void accionDespedir() {

        if (idSeleccionado == -1) {
            mostrarAlerta("⚠️ Selecciona un empleado de la tabla para despedir.", Alert.AlertType.WARNING);
            return;
        }

        String nombreEmpleado = txtNombre.getText();

        // Doble confirmación para evitar borrados accidentales
        if (!confirmar("⚠️ ¿Estás seguro de que deseas DESPEDIR a " + nombreEmpleado + "?\nEsta acción no se puede deshacer.")) return;

        String sql = "DELETE FROM empleados WHERE id_empleado = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSeleccionado);
            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                mostrarAlerta("✅ Empleado dado de baja correctamente.", Alert.AlertType.INFORMATION);
                limpiarFormulario();
                cargarEmpleados();
            }

        } catch (SQLException e) {
            mostrarAlerta("Error al despedir empleado:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // =====================================================================
    // LLENAR EL FORMULARIO con los datos del empleado seleccionado
    // Se llama cuando el usuario hace clic en una fila de la tabla
    // =====================================================================
    private void llenarFormulario(Empleado emp) {
        idSeleccionado = emp.getId(); // Guardamos el ID para UPDATE/DELETE

        // Mostrar los datos en el panel de acción (derecha)
        lblTituloPanel.setText("EDITAR EMPLEADO: #" + String.format("%03d", emp.getId()) + " - " + emp.getNombre());
        txtNombre.setText(emp.getNombre());
        txtUsuario.setText(emp.getUsuario());
        txtContrasena.clear(); // Por seguridad, no mostramos la contraseña
        cmbRol.setValue(emp.getRol());

        // Activar botones de modificar y eliminar
        btnActualizar.setDisable(false);
        btnDespedir.setDisable(false);
        // Desactivar "Contratar" mientras editamos (para no confundir)
        btnContratar.setDisable(true);
    }

    // =====================================================================
    // LIMPIAR FORMULARIO y regresar al modo ALTA
    // =====================================================================
    @FXML
    private void limpiarFormulario() {
        idSeleccionado = -1; // Reseteamos el ID seleccionado
        lblTituloPanel.setText("NUEVO EMPLEADO");
        txtNombre.clear();
        txtUsuario.clear();
        txtContrasena.clear();
        cmbRol.getSelectionModel().selectFirst();
        tablaEmpleados.getSelectionModel().clearSelection();

        // Regresar al estado inicial de botones
        btnContratar.setDisable(false);
        btnActualizar.setDisable(true);
        btnDespedir.setDisable(true);
    }

    // =====================================================================
    // VALIDAR CAMPOS: Verifica que los campos obligatorios no estén vacíos
    // Retorna TRUE si todo está bien, FALSE si hay algún error
    // =====================================================================
    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta("⚠️ El campo NOMBRE es obligatorio.", Alert.AlertType.WARNING);
            txtNombre.requestFocus();
            return false;
        }
        if (txtUsuario.getText().trim().isEmpty()) {
            mostrarAlerta("⚠️ El campo USUARIO es obligatorio.", Alert.AlertType.WARNING);
            txtUsuario.requestFocus();
            return false;
        }
        // La contraseña solo es obligatoria en altas (idSeleccionado == -1)
        if (idSeleccionado == -1 && txtContrasena.getText().trim().isEmpty()) {
            mostrarAlerta("⚠️ El campo CONTRASEÑA es obligatorio para nuevos empleados.", Alert.AlertType.WARNING);
            txtContrasena.requestFocus();
            return false;
        }
        if (cmbRol.getValue() == null) {
            mostrarAlerta("⚠️ Debes seleccionar un ROL.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    // =====================================================================
    // MOSTRAR ALERTA: Ventana de mensaje para el usuario
    // tipo puede ser: ERROR, WARNING, INFORMATION, CONFIRMATION
    // =====================================================================
    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Central de Chefs");
        alert.setHeaderText(null); // Sin encabezado extra
        alert.setContentText(mensaje);
        alert.showAndWait(); // Espera a que el usuario cierre la ventana
    }

    // =====================================================================
    // CONFIRMAR: Ventana Sí/No para acciones importantes
    // Retorna TRUE si el usuario presionó "OK", FALSE si presionó "Cancelar"
    // =====================================================================
    private boolean confirmar(String mensaje) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmación");
        confirm.setHeaderText(null);
        confirm.setContentText(mensaje);
        return confirm.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }

    // =====================================================================
    // BADGE DE COLOR PARA EL ROL
    // Agrega un chip de color en la columna "Rol" según el valor
    // =====================================================================
    private void agregarBadgeRol() {
        colRol.setCellFactory(col -> new TableCell<Empleado, String>() {
            @Override
            protected void updateItem(String rol, boolean empty) {
                super.updateItem(rol, empty);
                if (empty || rol == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Creamos un Label con estilo de badge (pastilla de color)
                    Label badge = new Label(rol);
                    badge.getStyleClass().add("badge-rol"); // Clase CSS base

                    // Asignamos clase CSS según el rol (sintaxis clásica, compatible Java 11)
                    switch (rol) {
                        case "Chef":
                            badge.getStyleClass().add("badge-chef");
                            break;
                        case "Cajero":
                            badge.getStyleClass().add("badge-cajero");
                            break;
                        case "Gerente":
                            badge.getStyleClass().add("badge-gerente");
                            break;
                        case "Mesero":
                            badge.getStyleClass().add("badge-mesero");
                            break;
                        case "Recepcionista":
                            badge.getStyleClass().add("badge-recepcionista");
                            break;
                        case "Administrador":
                            badge.getStyleClass().add("badge-administrador");
                            break;
                    }
                    setGraphic(badge);
                    setText(null); // Quitamos el texto normal (solo mostramos el badge)
                }
            }
        });
    }

    // =====================================================================
    // CLASE INTERNA: MODELO DE DATOS - EMPLEADO
    // Representa un empleado con sus campos (igual a la tabla de BD)
    // JavaFX necesita esta clase para poblar la TableView
    // =====================================================================
    public static class Empleado {

        // Campos del empleado (coinciden con columnas de la BD)
        private int id;
        private String nombre;
        private String usuario;
        private String contrasena;
        private String rol;

        // Constructor: se usa cuando creamos un Empleado con todos sus datos
        public Empleado(int id, String nombre, String usuario, String contrasena, String rol) {
            this.id        = id;
            this.nombre    = nombre;
            this.usuario   = usuario;
            this.contrasena = contrasena;
            this.rol       = rol;
        }

        // GETTERS: JavaFX los usa automáticamente con PropertyValueFactory
        public int    getId()         { return id; }
        public String getNombre()     { return nombre; }
        public String getUsuario()    { return usuario; }
        public String getContrasena() { return contrasena; }
        public String getRol()        { return rol; }

        // SETTERS: por si necesitas modificar valores en memoria
        public void setId(int id)                { this.id = id; }
        public void setNombre(String nombre)     { this.nombre = nombre; }
        public void setUsuario(String usuario)   { this.usuario = usuario; }
        public void setContrasena(String c)      { this.contrasena = c; }
        public void setRol(String rol)           { this.rol = rol; }
    }
}

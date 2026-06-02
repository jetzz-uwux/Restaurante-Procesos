package progs.restaurante.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import progs.restaurante.Producto;
import progs.restaurante.datos.ConexionBD;
import progs.restaurante.datos.ProductoDAO;
import progs.restaurante.lib.EstilosApp;
import progs.restaurante.lib.EstilosApp.CSS;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ClienteController implements Initializable {

    // Componentes de la Tabla (Sección Izquierda)
    @FXML
    private TableView<Producto> tbl_menu_cliente;
    @FXML
    private TableColumn<Producto, String> col_categoria;
    @FXML
    private TableColumn<Producto, String> col_nombre;
    @FXML
    private TableColumn<Producto, Double> col_precio;
    @FXML
    private Button btn_regresar;

    // Componentes del Formulario (Sección Derecha)
    @FXML
    private TextField txt_nombre;
    @FXML
    private DatePicker dp_fecha;
    @FXML
    private TextField txt_hora;
    @FXML
    private TextField txt_mesa;
    @FXML
    private Button btn_solicitar_reservacion;

    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        col_categoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        col_nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        col_precio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        tbl_menu_cliente.setItems(listaProductos);

        // Cargar menú usando DAO
        cargarMenuDesdeBD();
       
        btn_solicitar_reservacion.setOnAction(e -> handleRegistrarReservacion());
        btn_regresar.setOnAction(e -> handleRegresarInicio());
    }

    /**
     * Carga de datos utilizando ProductoDAO
     */
    private void cargarMenuDesdeBD() {
        Task<ObservableList<Producto>> tareaCarga = new Task<>() {
            @Override
            protected ObservableList<Producto> call() throws Exception {
                return productoDAO.listarMenu();
            }
        };

        // Al completarse con éxito, llena la tabla en el hilo principal
        tareaCarga.setOnSucceeded(ev -> Platform.runLater(() -> {
            ObservableList<Producto> menuCompleto = tareaCarga.getValue();
            listaProductos.clear();
            for (Producto p : menuCompleto) {
                if (p.isDisponible()) {
                    listaProductos.add(p);
                }
            }
        }));

        tareaCarga.setOnFailed(ev -> Platform.runLater(() -> {
            System.err.println("❌ Error al cargar menú con ProductoDAO: " + tareaCarga.getException().getMessage());
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Datos", "No se pudo actualizar la lista de platillos.");
        }));

        Thread hilo = new Thread(tareaCarga);
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Procesa y valida el formulario de reservación utilizando ConexionBD
     */
    @FXML
    private void handleRegistrarReservacion() {
        //valida que ningún campo esté vacío
        if (txt_nombre.getText().trim().isEmpty()
                || dp_fecha.getValue() == null
                || txt_hora.getText().trim().isEmpty()
                || txt_mesa.getText().trim().isEmpty()) {

            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos",
                    "Todos los campos son obligatorios para generar la reservación.");
            return;
        }

        String nombreCliente = txt_nombre.getText().trim();
        java.sql.Date fechaReserva = java.sql.Date.valueOf(dp_fecha.getValue());
        String horaReserva = txt_hora.getText().trim();

        int numeroMesa;
        try {
            numeroMesa = Integer.parseInt(txt_mesa.getText().trim());
            if (numeroMesa <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato Incorrecto", "El número de mesa debe ser un entero positivo.");
            return;
        }

        //Validación: Lógica de negocio (No reservar fechas en el pasado)
        if (dp_fecha.getValue().isBefore(java.time.LocalDate.now())) {
            mostrarAlerta(Alert.AlertType.ERROR, "Fecha Inválida",
                    "Error: La fecha de la reservación debe ser posterior o igual al día de hoy.");
            return;
        }
        
        String sqlVerificarMesa = "SELECT COUNT(*) FROM mesas WHERE numero_mesa = ?";
        String sqlVerificarReserva = "SELECT COUNT(*) FROM reservaciones WHERE num_mesa = ? AND fecha = ? AND hora = ? AND estado = 'Activa'";
        String sqlInsertar = "INSERT INTO reservaciones (num_reservacion, nombre_cliente, fecha, hora, num_mesa, estado) VALUES (?, ?, ?, ?, ?, 'Activa')";

        try (Connection conn = ConexionBD.getConexion()) {

            // Validar que la mesa exista en el restaurante
            try (PreparedStatement stmtCheckMesa = conn.prepareStatement(sqlVerificarMesa)) {
                stmtCheckMesa.setInt(1, numeroMesa);
                try (ResultSet rs = stmtCheckMesa.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        mostrarAlerta(Alert.AlertType.ERROR, "Mesa No Existe", "La mesa ingresada no está registrada en el sistema.");
                        return;
                    }
                }
            }

            // Validar que la mesa no esté apartada para la misma fecha y hora
            try (PreparedStatement stmtCheckRes = conn.prepareStatement(sqlVerificarReserva)) {
                stmtCheckRes.setInt(1, numeroMesa);
                stmtCheckRes.setDate(2, fechaReserva);
                stmtCheckRes.setString(3, horaReserva);
                try (ResultSet rs = stmtCheckRes.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        mostrarAlerta(Alert.AlertType.ERROR, "Mesa Ocupada", "Esta mesa ya cuenta con una reservación activa para esa fecha y hora.");
                        return;
                    }
                }
            }

            // Generar folio aleatorio (Ej: R-8392)
            String numReservacion = "R-" + (int) (Math.random() * 9000 + 1000);

            //Inserción segura con recursos auto-cerrables
            try (PreparedStatement stmtInsertar = conn.prepareStatement(sqlInsertar)) {
                stmtInsertar.setString(1, numReservacion);
                stmtInsertar.setString(2, nombreCliente);
                stmtInsertar.setDate(3, fechaReserva);
                stmtInsertar.setString(4, horaReserva);
                stmtInsertar.setInt(5, numeroMesa);

                stmtInsertar.executeUpdate();

                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                        "¡Reservación Confirmada! Código de registro: " + numReservacion);

                limpiarFormulario();
            }

        } catch (SQLException ex) {
            System.err.println("❌ Error en persistencia: " + ex.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR, "Error Crítico", "No se pudo actualizar la base de datos.");
        }
    }

    @FXML
    private void handleRegresarInicio() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/progs/fxml/VentanaRol.fxml"));
            Stage stageActual = (Stage) btn_regresar.getScene().getWindow();
            Scene escena = new Scene(root);
            EstilosApp.aplicar(escena, CSS.JUEGO, CSS.FUENTES, CSS.BOTONES, CSS.TEXTFIELD);
            stageActual.setScene(escena);
            stageActual.setTitle("Restaurante — Iniciar Sesión");
            stageActual.centerOnScreen();
        } catch (IOException ex) {
            System.err.println("❌ Error al cambiar de escena a Login: " + ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        txt_nombre.clear();
        dp_fecha.setValue(null);
        txt_hora.clear();
        txt_mesa.clear();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}

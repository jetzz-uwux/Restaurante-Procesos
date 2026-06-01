package progs.restaurante.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import progs.restaurante.Mesa;
import progs.restaurante.Reservacion;
import progs.restaurante.datos.ConexionBD;
import progs.restaurante.lib.CSS;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  VistaGestionReservasController.java                                 ║
 * ║                                                                      ║
 * ║  Muestra todas las reservaciones desde MySQL.                        ║
 * ║  Permite buscar por nombre o número, cancelar y crear nuevas.        ║
 * ║                                                                      ║
 * ║  Tabla BD: reservaciones                                             ║
 * ║    (id_reservacion, num_reservacion, nombre_cliente,                 ║
 * ║     num_mesa, estado)                                                ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class VistaGestionReservasController implements Initializable {

    // ══════════════════════════════════════════════════════
    //  NODOS DEL FXML
    // ══════════════════════════════════════════════════════

    @FXML private Button                        btn_buscar;
    @FXML private Button                        btn_cancelar;
    @FXML private Button                        btn_nuevo;
    @FXML private TextField                     txt_buscar;
    @FXML private TableView<Reservacion>        tlb_reservaciones;
    @FXML private TableColumn<Reservacion, String> clb_1; // Num reservación
    @FXML private TableColumn<Reservacion, String> clb_2; // Nombre cliente
    @FXML private TableColumn<Reservacion, String> clb_3; // Mesa
    @FXML private TableColumn<Reservacion, String> clb_4; // Estado

    // ══════════════════════════════════════════════════════
    //  ESTADO INTERNO
    // ══════════════════════════════════════════════════════

    // Lista principal — siempre tiene TODOS los registros de la BD
    private final ObservableList<Reservacion> listaTodas =
        FXCollections.observableArrayList();

    CSS vista = new CSS();

    // ══════════════════════════════════════════════════════
    //  INITIALIZE
    // ══════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Configurar columnas
        clb_1.setCellValueFactory(new PropertyValueFactory<>("numreservacion"));
        clb_2.setCellValueFactory(new PropertyValueFactory<>("nombrecliente"));
        clb_3.setCellValueFactory(new PropertyValueFactory<>("numeroMesa"));
        clb_4.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Asignar lista a la tabla
        tlb_reservaciones.setItems(listaTodas);
        tlb_reservaciones.setPlaceholder(new Label("No hay reservaciones registradas"));

        // Cargar desde la BD
        cargarReservaciones();
    }

    // ══════════════════════════════════════════════════════
    //  CARGAR RESERVACIONES DESDE MySQL
    // ══════════════════════════════════════════════════════

    private void cargarReservaciones() {
        Task<ObservableList<Reservacion>> tarea = new Task<>() {
            @Override
            protected ObservableList<Reservacion> call() throws Exception {
                ObservableList<Reservacion> lista = FXCollections.observableArrayList();

                String sql = "SELECT num_reservacion, nombre_cliente, num_mesa, estado"
                           + " FROM reservaciones"
                           + " ORDER BY id_reservacion DESC";

                Connection conn = ConexionBD.getConexion();
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        String num    = rs.getString("num_reservacion");
                        String nombre = rs.getString("nombre_cliente");
                        int    numMesa = rs.getInt("num_mesa");
                        String estado = rs.getString("estado");

                        // Construir el objeto Mesa y Reservacion
                        // igual que tu clase Java espera
                        Mesa mesa = new Mesa(numMesa);
                        Reservacion r = new Reservacion(num, nombre, mesa, estado);
                        lista.add(r);
                    }
                }
                return lista;
            }
        };

        tarea.setOnSucceeded(e -> Platform.runLater(() ->
            listaTodas.setAll(tarea.getValue())
        ));

        tarea.setOnFailed(e -> {
            System.err.println("❌ Error cargando reservaciones: "
                + tarea.getException().getMessage());
            tarea.getException().printStackTrace();
        });

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ══════════════════════════════════════════════════════
    //  BOTÓN: BUSCAR
    //  Filtra localmente sin ir a la BD
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleBuscar() {
        String filtro = txt_buscar.getText().trim().toLowerCase();

        if (filtro.isEmpty()) {
            // Sin filtro → mostrar todas
            tlb_reservaciones.setItems(listaTodas);
            return;
        }

        // Filtrar por nombre o número de reservación
        ObservableList<Reservacion> resultados = FXCollections.observableArrayList();
        for (Reservacion r : listaTodas) {
            if (r.getNombrecliente().toLowerCase().contains(filtro)
             || r.getNumreservacion().toLowerCase().contains(filtro)) {
                resultados.add(r);
            }
        }
        tlb_reservaciones.setItems(resultados);
    }

    // ══════════════════════════════════════════════════════
    //  BOTÓN: NUEVO
    //  Abre VentanaNuevaReservacion y le pasa la lista
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleNuevo() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/progs/fxml/VentanaNuevaReservacion.fxml")
            );
            Parent root = loader.load();

            // Pasar la lista al controlador de la nueva reservación
            // para que al guardar en BD también actualice esta lista
            VistaNuevaReservacionController ctrl = loader.getController();
            ctrl.setListaReservas(this.listaTodas);

            vista.cargarVistaCSS(root, btn_nuevo);

        } catch (IOException e) {
            System.err.println("❌ Error abriendo nueva reservación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════
    //  BOTÓN: CANCELAR
    //  Cambia estado a 'Cancelada' en la BD y en la tabla
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleCancelar() {
        Reservacion seleccionada =
            tlb_reservaciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Atención",
                "Selecciona una reservación de la tabla primero.");
            return;
        }

        if ("Cancelada".equals(seleccionada.getEstado())) {
            mostrarAlerta("Atención",
                "Esta reservación ya está cancelada.");
            return;
        }

        // Confirmar antes de cancelar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cancelación");
        confirmacion.setHeaderText("¿Cancelar reservación de "
            + seleccionada.getNombrecliente() + "?");
        confirmacion.setContentText(
            "Reservación #" + seleccionada.getNumreservacion()
            + " — Mesa " + seleccionada.getNumeroMesa()
        );

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                cancelarEnBD(seleccionada);
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  CANCELAR EN MySQL
    // ══════════════════════════════════════════════════════

    private void cancelarEnBD(Reservacion reservacion) {
        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String sql = "UPDATE reservaciones"
                           + " SET estado = 'Cancelada'"
                           + " WHERE num_reservacion = ?";

                Connection conn = ConexionBD.getConexion();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, reservacion.getNumreservacion());
                    stmt.executeUpdate();
                }
                return null;
            }
        };

        tarea.setOnSucceeded(e -> Platform.runLater(() -> {
            // Actualizar localmente sin recargar toda la tabla
            reservacion.setEstado("Cancelada");
            tlb_reservaciones.refresh();
            mostrarAlerta("Información",
                "Reservación cancelada correctamente.");
        }));

        tarea.setOnFailed(e -> {
            System.err.println("❌ Error cancelando: "
                + tarea.getException().getMessage());
            Platform.runLater(() ->
                mostrarAlerta("Error",
                    "No se pudo cancelar. Intenta de nuevo.")
            );
        });

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ══════════════════════════════════════════════════════
    //  RECIBIR LISTA ACTUALIZADA desde NuevaReservacion
    //  Se llama cuando el mesero regresa después de crear una
    // ══════════════════════════════════════════════════════

    public void setListaReservaciones(ObservableList<Reservacion> lista) {
        this.listaTodas.setAll(lista);
        tlb_reservaciones.setItems(this.listaTodas);
    }

    // ══════════════════════════════════════════════════════
    //  HELPER
    // ══════════════════════════════════════════════════════

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

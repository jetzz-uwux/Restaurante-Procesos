package progs.restaurante.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import progs.restaurante.datos.ConexionBD;
import progs.restaurante.lib.EstilosApp;
import progs.restaurante.lib.EstilosApp.CSS;
import progs.restaurante.models.Notificacion;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗ ║
 * VistaMeseroController.java ║ ║ ║ ║ Ventana de notificaciones con DOS
 * PESTAÑAS: ║ ║ • Pendientes — órdenes listas sin servir (leida=FALSE) ║ ║ •
 * Historial — órdenes ya servidas con filtro de fecha ║ ║ ║ ║ Botón "Ver
 * Detalle" → ventana con productos del pedido ║ ║ Botón "Ya Serví" → mueve a
 * Historial (leida=TRUE) ║ ║ Sistema doble: notif SO + campanita 🔔 en el menú
 * ║ ╚══════════════════════════════════════════════════════════════════════╝
 */
public class VistaMeseroController implements Initializable {

    /**
     * NODOS DEL FXML
     */
    @FXML
    private Text txt_usuario;
    @FXML
    private StackPane badge_campana;
    @FXML
    private Label lbl_campana;
    @FXML
    private Button btn_notificaciones;
    @FXML
    private Button btn_cerrarsesion;
    @FXML
    private Button btn_pedidos;
    @FXML
    private Button btn_cerrarCuenta;

    /**
     * ESTADO INTERNO
     */
    private final ObservableList<Notificacion> listaPendientes
            = FXCollections.observableArrayList();

    private final ObservableList<Notificacion> listaHistorial
            = FXCollections.observableArrayList();

    private final Set<Integer> idsYaNotificados = new HashSet<>();

    private ScheduledExecutorService scheduler;
    private TrayIcon trayIcon;
    private boolean trayDisponible = false;

    /**
     * INICIALIZAR
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        badge_campana.setVisible(false);
        badge_campana.setManaged(false);

        inicializarTray();
        cargarPendientes();
        iniciarRefrescoAutomatico();
        btn_cerrarCuenta.setOnAction(event -> handleCerrarCuenta());
    }

    public void recibirNombre(String nombre) {
        if (txt_usuario != null) {
            txt_usuario.setText(nombre);
        }
    }

    @FXML
    private void handlePedidos() {
        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/progs/fxml/VentanaPedidos.fxml")
            );

            Stage stage = new Stage();
            stage.setTitle("Pedidos");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCerrarCuenta() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/progs/fxml/VentanaCerrarCuenta.fxml")
            );
            Stage stage = new Stage();
            stage.setTitle("Cerrar Cuenta");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error en FXML");
            alert.setHeaderText("VentanaCerrarCuenta.fxml tiene un error");
            alert.setContentText("Detalle técnico: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            alert.showAndWait();
        }
    }

    /**
     * SYSTEM TRAY
     */
    private void inicializarTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("⚠️ SystemTray no disponible.");
            return;
        }
        try {
            Image imagen;
            try {
                URL urlIcono = getClass().getResource("/progs/img/icono.png");
                imagen = urlIcono != null
                        ? Toolkit.getDefaultToolkit().getImage(urlIcono)
                        : Toolkit.getDefaultToolkit().createImage(new byte[0]);
            } catch (Exception e) {
                imagen = Toolkit.getDefaultToolkit().createImage(new byte[0]);
            }

            trayIcon = new TrayIcon(imagen, "Restaurante — Mesero");
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e
                    -> Platform.runLater(this::mostrarVentanaNotificaciones)
            );

            SystemTray.getSystemTray().add(trayIcon);
            trayDisponible = true;
            System.out.println("✅ SystemTray inicializado.");

        } catch (Exception e) {
            System.err.println("❌ Error SystemTray: " + e.getMessage());
        }
    }

    private void enviarNotifSistema(Notificacion notif) {
        if (!trayDisponible || trayIcon == null) {
            return;
        }
        trayIcon.displayMessage(
                "🍽️ ¡Orden Lista! — Mesa " + notif.getNumeroMesa(),
                "Pedido #" + notif.getIdPedido()
                + " listo para servir.\nHora: " + notif.getHora()
                + "\nHaz clic para ver los detalles.",
                MessageType.WARNING
        );
    }

    /**
     * CARGAR PENDIENTES Leida = false
     */
    private void cargarPendientes() {
        Task<List<Notificacion>> tarea = new Task<>() {
            @Override
            protected List<Notificacion> call() throws Exception {
                return consultarPorEstado(false, null, null);
            }
        };

        tarea.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Notificacion> nuevas = tarea.getValue();

            // Detectar nuevas para notif del SO
            for (Notificacion n : nuevas) {
                if (!idsYaNotificados.contains(n.getIdNotificacion())) {
                    enviarNotifSistema(n);
                    idsYaNotificados.add(n.getIdNotificacion());
                }
            }

            listaPendientes.setAll(nuevas);

            // Limpiar IDs que ya fueron leídos
            Set<Integer> actuales = new HashSet<>();
            for (Notificacion n : nuevas) {
                actuales.add(n.getIdNotificacion());
            }
            idsYaNotificados.retainAll(actuales);

            actualizarCampana();
        }));

        tarea.setOnFailed(e
                -> System.err.println("❌ Error pendientes: " + tarea.getException().getMessage())
        );

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * CARGAR HISTORIAL Leida = true
     */
    private void cargarHistorial(LocalDate desde, LocalDate hasta) {
        Task<List<Notificacion>> tarea = new Task<>() {
            @Override
            protected List<Notificacion> call() throws Exception {
                return consultarPorEstado(true, desde, hasta);
            }
        };

        tarea.setOnSucceeded(e
                -> Platform.runLater(() -> listaHistorial.setAll(tarea.getValue()))
        );

        tarea.setOnFailed(e
                -> System.err.println("❌ Error historial: " + tarea.getException().getMessage())
        );

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ══════════════════════════════════════════════════════
    //  CONSULTA MySQL
    // ══════════════════════════════════════════════════════
    private List<Notificacion> consultarPorEstado(boolean leida,
            LocalDate desde,
            LocalDate hasta)
            throws SQLException {

        List<Notificacion> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT id_notificacion, id_pedido, numero_mesa, hora"
                + " FROM notificaciones"
                + " WHERE leida = ?"
        );

        // Filtro de fecha solo para historial
        if (desde != null) {
            sql.append(" AND DATE(hora) >= ?");
        }
        if (hasta != null) {
            sql.append(" AND DATE(hora) <= ?");
        }
        sql.append(" ORDER BY hora DESC");

        Connection conn = ConexionBD.getConexion();

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            stmt.setBoolean(idx++, leida);
            if (desde != null) {
                stmt.setDate(idx++, java.sql.Date.valueOf(desde));
            }
            if (hasta != null) {
                stmt.setDate(idx++, java.sql.Date.valueOf(hasta));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("hora");
                    LocalDateTime hora = ts != null
                            ? ts.toLocalDateTime()
                            : LocalDateTime.now();

                    lista.add(new Notificacion(
                            rs.getInt("id_notificacion"),
                            rs.getInt("id_pedido"),
                            rs.getInt("numero_mesa"),
                            hora,
                            leida
                    ));
                }
            }
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════
    //  ACTUALIZAR CAMPANITA
    // ══════════════════════════════════════════════════════
    private void actualizarCampana() {
        int cantidad = listaPendientes.size();
        if (cantidad == 0) {
            badge_campana.setVisible(false);
            badge_campana.setManaged(false);
        } else {
            lbl_campana.setText("🔔 " + cantidad);
            badge_campana.setVisible(true);
            badge_campana.setManaged(true);
        }
    }

    // ══════════════════════════════════════════════════════
    //  CLIC CAMPANITA O BOTÓN NOTIFICACIONES
    // ══════════════════════════════════════════════════════
    @FXML
    private void handleCampana(MouseEvent evento) {
        mostrarVentanaNotificaciones();
    }

    @FXML
    private void handleNotificaciones() {
        mostrarVentanaNotificaciones();
    }

    // ══════════════════════════════════════════════════════
    //  VENTANA PRINCIPAL DE NOTIFICACIONES
    //  Dos pestañas: Pendientes | Historial
    // ══════════════════════════════════════════════════════
    private void mostrarVentanaNotificaciones() {
        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("🔔 Órdenes — Mesero");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // ── PESTAÑA 1: PENDIENTES ─────────────────────────────────
        Tab tabPendientes = new Tab("🍽️ Pendientes");
        tabPendientes.setContent(crearPanelPendientes(ventana));

        // ── PESTAÑA 2: HISTORIAL ──────────────────────────────────
        Tab tabHistorial = new Tab("📋 Historial");
        tabHistorial.setContent(crearPanelHistorial());

        // Cargar historial al abrir esa pestaña
        tabHistorial.setOnSelectionChanged(e -> {
            if (tabHistorial.isSelected()) {
                // Por defecto: hoy
                cargarHistorial(LocalDate.now(), LocalDate.now());
            }
        });

        tabPane.getTabs().addAll(tabPendientes, tabHistorial);

        Scene escena = new Scene(tabPane, 500, 480);
        EstilosApp.aplicar(escena, CSS.JUEGO, CSS.FUENTES, CSS.TABLA_1);
        ventana.setScene(escena);
        ventana.showAndWait();
    }

    // ══════════════════════════════════════════════════════
    //  PANEL PENDIENTES
    // ══════════════════════════════════════════════════════
    private VBox crearPanelPendientes(Stage ventana) {

        // ── Tabla ─────────────────────────────────────────────────
        TableView<Notificacion> tabla = new TableView<>();
        tabla.setItems(listaPendientes);
        tabla.setPlaceholder(new Label("✅ Sin órdenes pendientes"));
        tabla.setPrefHeight(220);

        TableColumn<Notificacion, Integer> colPedido = new TableColumn<>("No. Pedido");
        colPedido.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        colPedido.setPrefWidth(90);

        TableColumn<Notificacion, Integer> colMesa = new TableColumn<>("Mesa");
        colMesa.setCellValueFactory(new PropertyValueFactory<>("numeroMesa"));
        colMesa.setPrefWidth(70);

        TableColumn<Notificacion, String> colHora = new TableColumn<>("Hora");
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colHora.setPrefWidth(80);

        // Columna Ver Detalle — botón por fila
        TableColumn<Notificacion, Void> colDetalle = new TableColumn<>("Detalle");
        colDetalle.setPrefWidth(100);
        colDetalle.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Ver Detalle");

            {
                btn.setStyle(
                        "-fx-font-size: 11px; -fx-padding: 4px 8px;"
                        + "-fx-background-color: #3a7bd5; -fx-text-fill: white;"
                        + "-fx-background-radius: 8px; -fx-cursor: hand;"
                );
                btn.setOnAction(e -> {
                    Notificacion notif = getTableView().getItems().get(getIndex());
                    mostrarDetalleOrden(notif);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabla.getColumns().addAll(colPedido, colMesa, colHora, colDetalle);

        // ── Label contador ────────────────────────────────────────
        Label lblInfo = new Label();
        lblInfo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-text-fill: #cc2200; -fx-padding: 6px;");

        // Actualizar label cuando cambie la lista
        listaPendientes.addListener(
                (javafx.collections.ListChangeListener<Notificacion>) c -> {
                    int n = listaPendientes.size();
                    lblInfo.setText("🍽️ " + n + " orden(es) pendiente(s)");
                }
        );
        lblInfo.setText("🍽️ " + listaPendientes.size() + " orden(es) pendiente(s)");

        // ── Botón Ya Serví ────────────────────────────────────────
        Button btnServi = new Button("✅  Ya Serví esta orden");
        btnServi.setStyle(
                "-fx-font-size: 14px; -fx-padding: 10px 24px;"
                + "-fx-background-color: #2d7a3a; -fx-text-fill: white;"
                + "-fx-background-radius: 14px; -fx-cursor: hand;"
                + "-fx-font-weight: bold;"
        );
        btnServi.setDisable(true);

        tabla.getSelectionModel().selectedItemProperty().addListener(
                (obs, ant, actual) -> btnServi.setDisable(actual == null)
        );

        btnServi.setOnAction(e -> {
            Notificacion sel = tabla.getSelectionModel().getSelectedItem();
            if (sel != null) {
                marcarComoServida(sel);
            }
        });

        // ── Botón Cerrar ──────────────────────────────────────────
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle("-fx-padding: 8px 20px; -fx-background-radius: 10px;"
                + "-fx-cursor: hand;");
        btnCerrar.setOnAction(e -> ventana.close());

        HBox botones = new HBox(12, btnServi, btnCerrar);
        botones.setAlignment(Pos.CENTER);

        VBox panel = new VBox(12, lblInfo, tabla, botones);
        panel.setPadding(new Insets(16));
        panel.setAlignment(Pos.CENTER);
        return panel;
    }

    // ══════════════════════════════════════════════════════
    //  PANEL HISTORIAL con filtro de fecha
    // ══════════════════════════════════════════════════════
    private VBox crearPanelHistorial() {

        // ── Filtro de fecha ───────────────────────────────────────
        DatePicker dpDesde = new DatePicker(LocalDate.now());
        DatePicker dpHasta = new DatePicker(LocalDate.now());
        dpDesde.setPromptText("Desde");
        dpHasta.setPromptText("Hasta");

        Button btnFiltrar = new Button("🔍 Filtrar");
        btnFiltrar.setStyle(
                "-fx-padding: 6px 16px; -fx-background-radius: 10px;"
                + "-fx-background-color: #3a7bd5; -fx-text-fill: white;"
                + "-fx-cursor: hand;"
        );

        HBox filtroPanelBox = new HBox(10,
                new Label("Desde:"), dpDesde,
                new Label("Hasta:"), dpHasta,
                btnFiltrar
        );
        filtroPanelBox.setAlignment(Pos.CENTER);
        filtroPanelBox.setPadding(new Insets(8));

        // ── Tabla historial ───────────────────────────────────────
        TableView<Notificacion> tablaHist = new TableView<>();
        tablaHist.setItems(listaHistorial);
        tablaHist.setPlaceholder(new Label("Sin registros para este período"));
        tablaHist.setPrefHeight(260);

        TableColumn<Notificacion, Integer> colPedido = new TableColumn<>("No. Pedido");
        colPedido.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        colPedido.setPrefWidth(90);

        TableColumn<Notificacion, Integer> colMesa = new TableColumn<>("Mesa");
        colMesa.setCellValueFactory(new PropertyValueFactory<>("numeroMesa"));
        colMesa.setPrefWidth(70);

        TableColumn<Notificacion, String> colHora = new TableColumn<>("Hora");
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colHora.setPrefWidth(80);

        // Columna Ver Detalle en historial también
        TableColumn<Notificacion, Void> colDetalle = new TableColumn<>("Detalle");
        colDetalle.setPrefWidth(100);
        colDetalle.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Ver Detalle");

            {
                btn.setStyle(
                        "-fx-font-size: 11px; -fx-padding: 4px 8px;"
                        + "-fx-background-color: #3a7bd5; -fx-text-fill: white;"
                        + "-fx-background-radius: 8px; -fx-cursor: hand;"
                );
                btn.setOnAction(e -> {
                    Notificacion notif = getTableView().getItems().get(getIndex());
                    mostrarDetalleOrden(notif);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tablaHist.getColumns().addAll(colPedido, colMesa, colHora, colDetalle);

        // Acción del botón filtrar
        btnFiltrar.setOnAction(e -> {
            LocalDate desde = dpDesde.getValue();
            LocalDate hasta = dpHasta.getValue();

            if (desde == null || hasta == null) {
                System.err.println("⚠️ Selecciona ambas fechas.");
                return;
            }
            if (desde.isAfter(hasta)) {
                System.err.println("⚠️ La fecha inicio no puede ser mayor a la final.");
                return;
            }
            cargarHistorial(desde, hasta);
        });

        Label lblTotal = new Label();
        lblTotal.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        listaHistorial.addListener(
                (javafx.collections.ListChangeListener<Notificacion>) c
                -> lblTotal.setText("Total: " + listaHistorial.size() + " orden(es) servida(s)")
        );

        VBox panel = new VBox(10, filtroPanelBox, tablaHist, lblTotal);
        panel.setPadding(new Insets(12));
        panel.setAlignment(Pos.CENTER);
        return panel;
    }

    // ══════════════════════════════════════════════════════
    //  VER DETALLE DE UNA ORDEN
    //  Consulta detalles_pedido y los muestra en ventanita
    // ══════════════════════════════════════════════════════
    private void mostrarDetalleOrden(Notificacion notif) {
        Task<List<String[]>> tarea = new Task<>() {
            @Override
            protected List<String[]> call() throws Exception {
                List<String[]> productos = new ArrayList<>();

                String sql = "SELECT nombre_producto, precio"
                        + " FROM detalles_pedido"
                        + " WHERE id_pedido = ?";

                Connection conn = ConexionBD.getConexion();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, notif.getIdPedido());
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            productos.add(new String[]{
                                rs.getString("nombre_producto"),
                                String.format("$%.2f", rs.getDouble("precio"))
                            });
                        }
                    }
                }
                return productos;
            }
        };

        tarea.setOnSucceeded(e -> Platform.runLater(() -> {
            List<String[]> productos = tarea.getValue();

            Stage detalle = new Stage();
            detalle.initModality(Modality.APPLICATION_MODAL);
            detalle.setTitle("Pedido #" + notif.getIdPedido()
                    + " — Mesa " + notif.getNumeroMesa());

            // Tabla de productos
            TableView<String[]> tablaProductos = new TableView<>();
            tablaProductos.setPrefHeight(200);
            tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            TableColumn<String[], String> colNombre = new TableColumn<>("Producto");
            colNombre.setCellValueFactory(data
                    -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0])
            );

            TableColumn<String[], String> colPrecio = new TableColumn<>("Precio");
            colPrecio.setCellValueFactory(data
                    -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1])
            );
            colPrecio.setPrefWidth(80);

            tablaProductos.getColumns().addAll(colNombre, colPrecio);
            tablaProductos.getItems().addAll(productos);

            // Info del pedido
            Label lblInfo = new Label(
                    "Mesa: " + notif.getNumeroMesa()
                    + "   |   Hora: " + notif.getHora()
                    + "   |   " + (notif.isLeida() ? "✅ Servido" : "⏳ Pendiente")
            );
            lblInfo.setStyle("-fx-font-size: 13px; -fx-padding: 8px;"
                    + "-fx-font-weight: bold;");

            Button btnCerrar = new Button("Cerrar");
            btnCerrar.setStyle("-fx-padding: 8px 24px; -fx-background-radius: 10px;"
                    + "-fx-cursor: hand;");
            btnCerrar.setOnAction(ev -> detalle.close());

            VBox layout = new VBox(12, lblInfo, tablaProductos, btnCerrar);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(16));
            layout.setPrefSize(340, 320);

            Scene escena = new Scene(layout);
            EstilosApp.aplicar(escena, CSS.JUEGO, CSS.FUENTES, CSS.TABLA_1);
            detalle.setScene(escena);
            detalle.showAndWait();
        }));

        tarea.setOnFailed(e
                -> System.err.println("❌ Error cargando detalle: "
                        + tarea.getException().getMessage())
        );

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ══════════════════════════════════════════════════════
    //  MARCAR COMO SERVIDA → mueve al historial
    // ══════════════════════════════════════════════════════
    private void marcarComoServida(Notificacion notif) {
        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String sql = "UPDATE notificaciones"
                        + " SET leida = TRUE"
                        + " WHERE id_notificacion = ?";

                Connection conn = ConexionBD.getConexion();
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, notif.getIdNotificacion());
                    stmt.executeUpdate();
                }
                return null;
            }
        };

        tarea.setOnSucceeded(e -> Platform.runLater(() -> {
            // Quitar de pendientes
            listaPendientes.remove(notif);
            idsYaNotificados.remove(notif.getIdNotificacion());

            // Agregar al historial local (sin recargar de la BD)
            notif.setLeida(true);
            listaHistorial.add(0, notif); // al principio de la lista

            // Actualizar campanita
            actualizarCampana();
        }));

        tarea.setOnFailed(e
                -> System.err.println("❌ Error marcando servida: "
                        + tarea.getException().getMessage())
        );

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ══════════════════════════════════════════════════════
    //  REFRESCO AUTOMÁTICO
    // ══════════════════════════════════════════════════════
    private void iniciarRefrescoAutomatico() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(
                this::cargarPendientes, 5, 5, TimeUnit.SECONDS
        );
    }
    // ══════════════════════════════════════════════════════
    //  BOTONES DEL MENÚ
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleCerrarSesion() {
        detener();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/progs/fxml/login.fxml")
            );
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) btn_cerrarsesion.getScene().getWindow();
            Scene escena = new Scene(root);
            EstilosApp.aplicar(escena, CSS.JUEGO, CSS.FUENTES, CSS.BOTONES, CSS.TEXTFIELD);
            stage.setScene(escena);
            stage.setTitle("Restaurante — Iniciar Sesión");
            stage.centerOnScreen();
        } catch (Exception ex) {
            System.err.println("❌ Error cerrando sesión: " + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    //  DETENER AL CERRAR
    // ══════════════════════════════════════════════════════
    public void detener() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        if (trayDisponible && trayIcon != null) {
            try {
                SystemTray.getSystemTray().remove(trayIcon);
            } catch (Exception e) {
                System.err.println("Error tray: " + e.getMessage());
            }
        }
    }
}

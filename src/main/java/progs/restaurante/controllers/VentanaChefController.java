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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import progs.restaurante.lib.EstilosApp;
import progs.restaurante.lib.EstilosApp.CSS;
import progs.restaurante.models.DetallePedido;
import progs.restaurante.models.Pedido;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import progs.restaurante.datos.ConexionBD;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  VentanaChefController.java                                          ║
 * ║                                                                      ║
 * ║  Funcionalidades:                                                    ║
 * ║   1. Muestra pedidos 'pendiente' y 'en preparacion' del chef         ║
 * ║   2. Se auto-refresca cada 5 segundos en segundo plano               ║
 * ║   3. Colorea filas por urgencia (verde/naranja/rojo)                 ║
 * ║   4. DETALLES → ventana con los productos del pedido                 ║
 * ║   5. ORDEN LISTA → cambia estado a 'listo' en la BD                 ║
 * ║   6. Refrescar → recarga manual inmediata                            ║
 * ║                                                                      ║
 * ║  Tablas usadas:                                                      ║
 * ║    pedidos          (id_pedido, numero_mesa, total, estado, fecha)   ║
 * ║    detalles_pedido  (id_detalle, id_pedido, nombre_producto, precio) ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */
public class VentanaChefController implements Initializable {

    // ══════════════════════════════════════════════════════
    //  NODOS INYECTADOS DESDE EL FXML
    // ══════════════════════════════════════════════════════

    @FXML private TableView<Pedido>            tlb_pedidos;
    @FXML private TableColumn<Pedido, Integer> clb_1;   // No. Pedido
    @FXML private TableColumn<Pedido, Integer> clb_2;   // No. Mesa
    @FXML private TableColumn<Pedido, String>  clb_3;   // Estado
    @FXML private Button                       btn_editar;  // DETALLES
    @FXML private Button                       btn_servir;  // ORDEN LISTA
    @FXML private Button                       btn_salir;   // SALIR → login

    // ══════════════════════════════════════════════════════
    //  CONSTANTES — ajusta aquí si cambian los nombres en BD
    // ══════════════════════════════════════════════════════

    private static final String TABLA_PEDIDOS  = "pedidos";
    private static final String TABLA_DETALLES = "detalles_pedido";
    private static final String COL_ID         = "id_pedido";
    private static final String COL_MESA       = "numero_mesa";
    private static final String COL_TOTAL      = "total";
    private static final String COL_ESTADO     = "estado";
    private static final String COL_FECHA      = "fecha";

    // Valores exactos del campo estado en la BD (VARCHAR en minúsculas)
    private static final String ESTADO_PENDIENTE   = "pendiente";
    private static final String ESTADO_PREPARACION = "en preparacion";
    private static final String ESTADO_LISTO       = "listo";

    // Umbrales de urgencia en minutos
    private static final int MIN_URGENTE      = 15; // rojo
    private static final int MIN_ADVERTENCIA  = 8;  // naranja

    // ══════════════════════════════════════════════════════
    //  ESTADO INTERNO
    // ══════════════════════════════════════════════════════

    // Lista observable — la TableView la escucha automáticamente
    private final ObservableList<Pedido> listaPedidos =
        FXCollections.observableArrayList();

    // Guarda las fechas originales para calcular el tiempo de espera
    // sin necesidad de consultar la BD cada vez
    private final Map<Integer, LocalDateTime> fechasOriginales = new HashMap<>();

    // Hilo que ejecuta el refresco cada 5 segundos
    private ScheduledExecutorService scheduler;

    // ══════════════════════════════════════════════════════
    //  INITIALIZE — se ejecuta al cargar el FXML
    // ══════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // 1. Configurar columnas del FXML
        configurarColumnas();

        // 2. Configurar colores de filas por urgencia
        configurarColoresFilas();

        // 3. Asignar lista a la tabla
        tlb_pedidos.setItems(listaPedidos);

        // 4. Mensaje cuando no hay pedidos pendientes
        tlb_pedidos.setPlaceholder(
            new Label("✅ No hay pedidos pendientes")
        );

        // 5. Deshabilitar botones hasta que haya una fila seleccionada
        btn_editar.setDisable(true);
        btn_servir.setDisable(true);
        tlb_pedidos.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, anterior, actual) -> {
                boolean haySeleccion = actual != null;
                btn_editar.setDisable(!haySeleccion);
                btn_servir.setDisable(!haySeleccion);
            });

        // 6. Primera carga de datos
        cargarPedidos();

        // 7. Iniciar refresco automático cada 5 segundos
        iniciarRefrescoAutomatico();
    }

    // ══════════════════════════════════════════════════════
    //  CONFIGURAR COLUMNAS
    // ══════════════════════════════════════════════════════

    private void configurarColumnas() {

        // Columnas que vienen del FXML
        clb_1.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        clb_2.setCellValueFactory(new PropertyValueFactory<>("numeroMesa"));
        clb_3.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Columnas extra creadas desde Java
        TableColumn<Pedido, String> colHora = new TableColumn<>("Hora");
        colHora.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setPrefWidth(80);
        colHora.setEditable(false);

        TableColumn<Pedido, String> colEspera = new TableColumn<>("⏱ Espera");
        colEspera.setCellValueFactory(new PropertyValueFactory<>("tiempoEspera"));
        colEspera.setPrefWidth(90);
        colEspera.setEditable(false);

        TableColumn<Pedido, Double> colTotal = new TableColumn<>("Total $");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(80);
        colTotal.setEditable(false);

        tlb_pedidos.getColumns().addAll(colHora, colEspera, colTotal);

        // Ordenar por espera descendente: el más antiguo aparece arriba
        colEspera.setSortType(TableColumn.SortType.DESCENDING);
        tlb_pedidos.getSortOrder().add(colEspera);
    }

    // ══════════════════════════════════════════════════════
    //  COLOREAR FILAS POR URGENCIA
    //  < 8 min  → normal
    //  8-15 min → naranja (advertencia)
    //  > 15 min → rojo    (urgente)
    // ══════════════════════════════════════════════════════

    private void configurarColoresFilas() {
        tlb_pedidos.setRowFactory(tv -> new TableRow<Pedido>() {
            @Override
            protected void updateItem(Pedido pedido, boolean vacio) {
                super.updateItem(pedido, vacio);

                if (vacio || pedido == null) {
                    setStyle("");
                    return;
                }

                LocalDateTime fechaOriginal =
                    fechasOriginales.get(pedido.getIdPedido());

                long minutos = 0;
                if (fechaOriginal != null) {
                    minutos = java.time.Duration
                        .between(fechaOriginal, LocalDateTime.now())
                        .toMinutes();
                }

                if (minutos >= MIN_URGENTE) {
                    // Rojo — pedido urgente
                    setStyle("-fx-background-color: #cc2200; -fx-text-fill: white;");
                } else if (minutos >= MIN_ADVERTENCIA) {
                    // Naranja — pedido con tiempo
                    setStyle("-fx-background-color: #e87000; -fx-text-fill: white;");
                } else {
                    // Normal
                    setStyle("");
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  CARGAR PEDIDOS DESDE MySQL
    // ══════════════════════════════════════════════════════

    private void cargarPedidos() {
        Task<List<Pedido>> tarea = new Task<>() {
            @Override
            protected List<Pedido> call() throws Exception {
                return consultarPedidosEnBD();
            }
        };

        tarea.setOnSucceeded(e -> Platform.runLater(() -> {
            listaPedidos.setAll(tarea.getValue());
            tlb_pedidos.refresh();
        }));

        tarea.setOnFailed(e ->
            System.err.println("❌ Error cargando pedidos: "
                + tarea.getException().getMessage())
        );

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ══════════════════════════════════════════════════════
    //  CONSULTA MySQL — solo pendiente y en preparacion
    // ══════════════════════════════════════════════════════

    private List<Pedido> consultarPedidosEnBD() throws SQLException {
        List<Pedido> lista = new ArrayList<>();

        String sql = "SELECT " + COL_ID + ", " + COL_MESA + ", "
                   + COL_TOTAL + ", " + COL_ESTADO + ", " + COL_FECHA
                   + " FROM " + TABLA_PEDIDOS
                   + " WHERE " + COL_ESTADO + " IN (?, ?)"
                   + " ORDER BY " + COL_FECHA + " ASC";

        Connection conn = ConexionBD.getConexion();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ESTADO_PENDIENTE);
            stmt.setString(2, ESTADO_PREPARACION);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int    id     = rs.getInt(COL_ID);
                    int    mesa   = rs.getInt(COL_MESA);
                    double total  = rs.getDouble(COL_TOTAL);
                    String estado = rs.getString(COL_ESTADO);

                    // TIMESTAMP de MySQL → LocalDateTime de Java
                    Timestamp ts = rs.getTimestamp(COL_FECHA);
                    LocalDateTime fecha = ts != null
                        ? ts.toLocalDateTime()
                        : LocalDateTime.now();

                    // Guardar fecha real para calcular urgencia
                    fechasOriginales.put(id, fecha);

                    lista.add(new Pedido(id, mesa, total, estado, fecha));
                }
            }
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════
    //  REFRESCO AUTOMÁTICO CADA 5 SEGUNDOS
    // ══════════════════════════════════════════════════════

    private void iniciarRefrescoAutomatico() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // muere cuando cierra la app
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            // Recargar desde BD
            cargarPedidos();

            // Actualizar tiempo de espera de cada fila sin ir a la BD
            Platform.runLater(() -> {
                for (Pedido p : listaPedidos) {
                    LocalDateTime fechaOriginal =
                        fechasOriginales.get(p.getIdPedido());
                    if (fechaOriginal != null) {
                        p.actualizarEspera(fechaOriginal);
                    }
                }
                tlb_pedidos.refresh();
            });

        }, 30, 30, TimeUnit.SECONDS);
    }

    // ══════════════════════════════════════════════════════
    //  BOTÓN: REFRESCAR (manual)
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleRefrescar() {
        cargarPedidos();
    }

    // ══════════════════════════════════════════════════════
    //  BOTÓN: DETALLES
    //  Muestra los productos del pedido seleccionado
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleEditar() {
        Pedido seleccionado = tlb_pedidos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarError("Selecciona un pedido de la tabla primero.");
            return;
        }

        Task<List<DetallePedido>> tarea = new Task<>() {
            @Override
            protected List<DetallePedido> call() throws Exception {
                return consultarDetallesEnBD(seleccionado.getIdPedido());
            }
        };

        tarea.setOnSucceeded(e -> Platform.runLater(() ->
            mostrarVentanaDetalles(seleccionado, tarea.getValue())
        ));

        tarea.setOnFailed(e -> {
            System.err.println("❌ Error cargando detalles: "
                + tarea.getException().getMessage());
            Platform.runLater(() ->
                mostrarError("No se pudieron cargar los productos del pedido.")
            );
        });

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ══════════════════════════════════════════════════════
    //  CONSULTA DETALLES DEL PEDIDO
    // ══════════════════════════════════════════════════════

    private List<DetallePedido> consultarDetallesEnBD(int idPedido)
            throws SQLException {

        List<DetallePedido> lista = new ArrayList<>();

        String sql = "SELECT id_detalle, id_pedido, nombre_producto, precio"
                   + " FROM " + TABLA_DETALLES
                   + " WHERE id_pedido = ?";

        Connection conn = ConexionBD.getConexion();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DetallePedido(
                        rs.getInt("id_detalle"),
                        rs.getInt("id_pedido"),
                        rs.getString("nombre_producto"),
                        rs.getDouble("precio")
                    ));
                }
            }
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════
    //  VENTANA EMERGENTE DE DETALLES
    //  Construida desde Java sin FXML extra
    // ══════════════════════════════════════════════════════

    private void mostrarVentanaDetalles(Pedido pedido,
                                        List<DetallePedido> detalles) {
        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("Pedido #" + pedido.getIdPedido()
            + " — Mesa " + pedido.getNumeroMesa());

        // Tabla de productos
        TableView<DetallePedido> tabla = new TableView<>();

        TableColumn<DetallePedido, String> colNombre =
            new TableColumn<>("Producto");
        colNombre.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getNombreProducto()
            )
        );
        colNombre.setPrefWidth(220);

        TableColumn<DetallePedido, Double> colPrecio =
            new TableColumn<>("Precio $");
        colPrecio.setCellValueFactory(data ->
            new javafx.beans.property.SimpleDoubleProperty(
                data.getValue().getPrecio()
            ).asObject()
        );
        colPrecio.setPrefWidth(100);

        tabla.getColumns().addAll(colNombre, colPrecio);
        tabla.getItems().addAll(detalles);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Resumen del pedido
        Label lblResumen = new Label(
            "Mesa: " + pedido.getNumeroMesa()
            + "   |   Estado: " + pedido.getEstado()
            + "   |   Total: $" + String.format("%.2f", pedido.getTotal())
            + "   |   Hora: " + pedido.getFecha()
        );
        lblResumen.setStyle("-fx-font-size: 13px; -fx-padding: 8px;");

        // Botón cerrar
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setOnAction(e -> ventana.close());
        btnCerrar.setStyle("-fx-font-size: 14px; -fx-padding: 8px 24px;");

        // Layout
        VBox layout = new VBox(12, lblResumen, tabla, btnCerrar);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(16));
        layout.setPrefSize(380, 400);

        Scene escena = new Scene(layout);
        EstilosApp.aplicar(escena, CSS.JUEGO, CSS.FUENTES, CSS.TABLA_1);
        ventana.setScene(escena);
        ventana.showAndWait();
    }

    // ══════════════════════════════════════════════════════
    //  BOTÓN: ORDEN LISTA
    //  Cambia estado a 'listo' y notifica al mesero
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleCambiarEstado() {
        Pedido seleccionado = tlb_pedidos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarError("Selecciona un pedido de la tabla primero.");
            return;
        }

        // Pedir confirmación antes de cambiar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Marcar pedido #"
            + seleccionado.getIdPedido() + " como LISTO?");
        confirmacion.setContentText(
            "Mesa " + seleccionado.getNumeroMesa()
            + "\nEl mesero será notificado para servir la orden."
        );

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                actualizarEstadoEnBD(seleccionado);
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  ACTUALIZAR ESTADO EN MySQL
    // ══════════════════════════════════════════════════════

    private void actualizarEstadoEnBD(Pedido pedido) {
        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String sql = "UPDATE " + TABLA_PEDIDOS
                           + " SET " + COL_ESTADO + " = ?"
                           + " WHERE " + COL_ID + " = ?";

                Connection conn = ConexionBD.getConexion();

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, ESTADO_LISTO);
                    stmt.setInt(2, pedido.getIdPedido());
                    stmt.executeUpdate();
                }
                return null;
            }
        };

        tarea.setOnSucceeded(e -> Platform.runLater(() -> {
            // Quitar el pedido de la tabla (ya no es pendiente)
            listaPedidos.remove(pedido);
            fechasOriginales.remove(pedido.getIdPedido());

            // Notificar con ventana de éxito
            Stage stage = (Stage) btn_servir.getScene().getWindow();
            DialogoController.mostrarExito(
                stage,
                "¡Pedido #" + pedido.getIdPedido() + " marcado como listo!\n"
                + "Mesa " + pedido.getNumeroMesa() + " lista para servir. 🍽️"
            );
        }));

        tarea.setOnFailed(e -> {
            System.err.println("❌ Error actualizando estado: "
                + tarea.getException().getMessage());
            Platform.runLater(() ->
                mostrarError("No se pudo actualizar el estado. Intenta de nuevo.")
            );
        });

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ══════════════════════════════════════════════════════
    //  BOTÓN: SALIR
    //  Detiene el scheduler y vuelve a la pantalla de login
    // ══════════════════════════════════════════════════════

    @FXML
    private void handleSalir() {
        // 1. Detener el refresco automático antes de salir
        detener();

        try {
            // 2. Cargar el FXML del login
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/progs/fxml/VistaInicioSesion.fxml")
            );
            javafx.scene.Parent root = loader.load();

            // 3. Obtener el Stage actual desde cualquier nodo
            Stage stage = (Stage) btn_salir.getScene().getWindow();

            // 4. Cambiar la escena de vuelta al login
            javafx.scene.Scene escena = new javafx.scene.Scene(root);
            EstilosApp.aplicar(escena,
                CSS.JUEGO,
                CSS.FUENTES,
                CSS.BOTONES,
                CSS.TEXTFIELD,
                CSS.IMAGEN,
                CSS.TEXTO
            );
            stage.setScene(escena);
            stage.setTitle("Restaurante — Iniciar Sesión");
            stage.centerOnScreen();
            stage.show();

        } catch (java.io.IOException ex) {
            System.err.println("❌ Error al volver al login: " + ex.getMessage());
            mostrarError("No se pudo volver a la pantalla de inicio.");
        }
    }

    // ══════════════════════════════════════════════════════
    //  DETENER EL SCHEDULER AL CERRAR LA VENTANA
    //
    //  IMPORTANTE: llama esto donde abres VentanaChef.fxml:
    //    VentanaChefController ctrl = loader.getController();
    //    stage.setOnCloseRequest(e -> ctrl.detener());
    // ══════════════════════════════════════════════════════

    public void detener() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("🛑 Refresco automático detenido.");
        }
    }

    // ══════════════════════════════════════════════════════
    //  HELPER
    // ══════════════════════════════════════════════════════

    private void mostrarError(String mensaje) {
        Stage stage = (Stage) tlb_pedidos.getScene().getWindow();
        DialogoController.mostrarError(stage, mensaje);
    }
}

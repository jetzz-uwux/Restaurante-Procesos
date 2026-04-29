package progs.restaurante.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import progs.restaurante.Orden;
import progs.restaurante.datos.OrdenesDAO;

public class VentanaPedidosController implements Initializable {

    @FXML private TableView<Orden> tlb_pedidos;
    @FXML private TableColumn<Orden, Integer> clb_1;
    @FXML private TableColumn<Orden, Integer> clb_2;
    @FXML private TableColumn<Orden, String> clb_3;
    @FXML private TableColumn<Orden, Double> clb_4;
    @FXML private TextField txt_buscar;

    private ObservableList<Orden> listaPedidos = FXCollections.observableArrayList();
    private OrdenesDAO ordenesDAO = new OrdenesDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clb_1.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        clb_2.setCellValueFactory(new PropertyValueFactory<>("numeroMesa"));
        clb_3.setCellValueFactory(new PropertyValueFactory<>("estado"));
        clb_4.setCellValueFactory(new PropertyValueFactory<>("total"));

        cargarDatosDesdeBD();
    }

    private void cargarDatosDesdeBD() {
        listaPedidos = ordenesDAO.listarPedidos();
        tlb_pedidos.setItems(listaPedidos);
    }

    // NUEVO PEDIDO
    @FXML
    private void handleNuevoPedido() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/progs/fxml/VentanaNuevoPedido.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nuevo Pedido");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarDatosDesdeBD();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // EDITAR PEDIDO
    @FXML
    private void handleEditar() {
        Orden seleccionado = tlb_pedidos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un pedido.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/progs/fxml/VentanaEditarPedido.fxml"));
            Parent root = loader.load();

            VentanaEditarPedidoController controller = loader.getController();
            controller.cargarDatos(seleccionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Pedido");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarDatosDesdeBD();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CANCELAR PEDIDO
    @FXML
    private void handleBtnCancelarPedido() {
        Orden seleccionado = tlb_pedidos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un pedido para cancelarlo.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Cancelación");
        confirmacion.setHeaderText("¿Estás seguro de cancelar el pedido?");
        confirmacion.setContentText("Se eliminará permanentemente.");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            ordenesDAO.eliminarOrden(seleccionado.getIdPedido());
            cargarDatosDesdeBD();
            mostrarAlerta("Éxito", "Pedido eliminado.");
        }
    }

    // SERVIR (REGISTRA VENTA)
    @FXML
    private void handleCambiarEstado() {
        Orden seleccionada = tlb_pedidos.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Atención", "Selecciona un pedido primero.");
            return;
        }

        if ("Servido".equals(seleccionada.getEstado())) {
            mostrarAlerta("Aviso", "Este pedido ya fue servido.");
            return;
        }

        seleccionada.setEstado("Servido");

        // 🔥 Registrar venta
        ordenesDAO.registrarVenta(seleccionada);

        // Actualizar pedido
        ordenesDAO.actualizarOrden(seleccionada);

        tlb_pedidos.refresh();

        mostrarAlerta("Éxito", "Pedido servido y registrado como venta.");
    }

    // ABRIR VENTANA
    @FXML
    private void abrirVentas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/progs/fxml/VentanaVentas.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Reporte de Ventas");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ALERTAS
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
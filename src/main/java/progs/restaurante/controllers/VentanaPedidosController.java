package progs.restaurante.controllers;

import java.io.IOException;
import progs.restaurante.Mesa;
import progs.restaurante.Orden;
import progs.restaurante.Empleados.Mesero;
import progs.restaurante.datos.OrdenesDAO; // Importante
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

public class VentanaPedidosController implements Initializable {

    @FXML
    private TableView<Orden> tlb_pedidos;
    @FXML
    private TableColumn<Orden, Integer> clb_1; // ID Pedido
    @FXML
    private TableColumn<Orden, Integer> clb_2; // No. de mesa
    @FXML
    private TableColumn<Orden, String> clb_3;  // Estado
    @FXML
    private TableColumn<Orden, Double> clb_4;  // Total

    @FXML
    private TextField txt_buscar;

    private ObservableList<Orden> listaPedidos = FXCollections.observableArrayList();
    private OrdenesDAO ordenesDAO = new OrdenesDAO(); // Instancia para la BD

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuración de columnas
        clb_1.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        clb_2.setCellValueFactory(new PropertyValueFactory<>("numeroMesa"));
        clb_3.setCellValueFactory(new PropertyValueFactory<>("estado"));
        clb_4.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Carga inicial de datos desde la base de datos
        cargarDatosDesdeBD();
    }

    private void cargarDatosDesdeBD() {
        listaPedidos = ordenesDAO.listarPedidos();
        tlb_pedidos.setItems(listaPedidos);
    }

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

            cargarDatosDesdeBD(); // Refresh al volver
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        confirmacion.setContentText("Se eliminará permanentemente de la base de datos.");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            // Eliminación física en BD
            ordenesDAO.eliminarOrden(seleccionado.getIdPedido());

            cargarDatosDesdeBD(); // Refrescar tabla
            mostrarAlerta("Éxito", "El pedido ha sido eliminado.");
        }
    }

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
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarDatosDesdeBD(); // Refrescar después de editar

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCambiarEstado() {
        Orden seleccionada = tlb_pedidos.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Atención", "Selecciona un pedido primero.");
            return;
        }                

        //if (seleccionada.getEstado().equals("Listo")) {
            seleccionada.setEstado("Servido");

            // Persistir cambio en BD
            ordenesDAO.actualizarOrden(seleccionada);

            tlb_pedidos.refresh();
            mostrarAlerta("Éxito", "El pedido ha sido marcado como servido.");
        //} else {
            //mostrarAlerta("Acción Denegada", "Solo pedidos marcados como 'Listo' pueden pasarse a 'Servido'.");
        //}
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

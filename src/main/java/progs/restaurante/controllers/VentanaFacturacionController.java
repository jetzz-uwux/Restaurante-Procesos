package progs.restaurante.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import progs.restaurante.Orden;
import progs.restaurante.datos.OrdenesDAO;

public class VentanaFacturacionController implements Initializable {

    @FXML
    private TextField txt_idFactura;
    @FXML
    private TableView<Orden> tlb_facturacion;
    @FXML
    private TableColumn<Orden, Integer> colMesa;
    @FXML
    private TableColumn<Orden, Double> colTotal;
    @FXML
    private ComboBox<String> cmb_metodoPago;
    @FXML
    private Button btn_finalizar;

    private Orden ordenActual;
    private OrdenesDAO ordenesDAO = new OrdenesDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {        
        colMesa.setCellValueFactory(new PropertyValueFactory<>("numeroMesa"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        cmb_metodoPago.setItems(FXCollections.observableArrayList("Efectivo", "Tarjeta"));
        cmb_metodoPago.setValue("Efectivo");
    }

    @FXML
    private void handleBuscarPedido() {
        try {
            int id = Integer.parseInt(txt_idFactura.getText());
            Orden encontrada = ordenesDAO.buscarPedidoPorId(id);

            if (encontrada != null && "Cerrado".equals(encontrada.getEstado())) {
                this.ordenActual = encontrada;                
                ObservableList<Orden> data = FXCollections.observableArrayList(encontrada);
                tlb_facturacion.setItems(data);
            } else {
                mostrarAlerta("Atención", "No se encontró el pedido o no ha sido cerrado por el mesero.");
                tlb_facturacion.setItems(null);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El ID debe ser un número.");
        }
    }

    @FXML
    private void handleFinalizarPago() {
        if (ordenActual == null) {
            mostrarAlerta("Error", "Primero debes buscar un pedido.");
            return;
        }
        
        ordenesDAO.finalizarVenta(ordenActual);

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Venta Exitosa");
        success.setHeaderText(null);
        success.setContentText("Pago registrado con " + cmb_metodoPago.getValue() + ".\nLa mesa ha sido liberada.");
        success.showAndWait();

        limpiarInterfaz();
    }

    private void limpiarInterfaz() {
        txt_idFactura.clear();
        tlb_facturacion.setItems(null);
        ordenActual = null;
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

package progs.restaurante.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import progs.restaurante.Orden;
import progs.restaurante.Producto;

public class VentanaEditarPedidoController implements Initializable {

    @FXML
    private TableView<Producto> tlb_pedidos;
    @FXML
    private TableColumn<Producto, String> clb_1; // Platillo/Bebida
    @FXML
    private TableColumn<Producto, Integer> clb_2; // Cantidad
    @FXML
    private TableColumn<Producto, Double> clb_3;  // Precio unitario
    @FXML
    private TableColumn<Producto, Double> clb_4;  // Subtotal

    @FXML
    private TextField txt_numeroMesa;

    private ObservableList<Producto> productosEditados = FXCollections.observableArrayList();
    private Orden ordenOriginal;
    private Orden ordenEditar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clb_1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        clb_2.setCellValueFactory(new PropertyValueFactory<>("stock"));
        clb_3.setCellValueFactory(new PropertyValueFactory<>("precio"));

        // Subtotal: Precio * Cantidad (stock)
        clb_4.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            return new javafx.beans.property.SimpleDoubleProperty(p.getPrecio() * p.getStock()).asObject();
        });

        tlb_pedidos.setItems(productosEditados);
    }

    public void cargarDatos(Orden orden) {
        this.ordenOriginal = orden;
        this.txt_numeroMesa.setText(String.valueOf(orden.getMesa().getNumero()));

        if (orden.getItems() != null) {
            java.util.Map<String, Producto> agrupados = new java.util.HashMap<>();

            for (Producto p : orden.getItems()) {
                if (agrupados.containsKey(p.getNombre())) {                    
                    agrupados.get(p.getNombre()).reducirStock(-1);
                } else {                   
                    p.reducirStock(p.getStock());
                    p.reducirStock(-1);
                    agrupados.put(p.getNombre(), p);
                }
            }
            this.productosEditados.setAll(agrupados.values());
        }
    }

    @FXML
    private void handleAceptar() {
        ordenOriginal.getItems().clear();

        for (Producto p : productosEditados) {
            int cantidad = p.getStock();

            for (int i = 0; i < cantidad; i++) {
                ordenOriginal.getItems().add(p);
            }
        }

        mostrarAlerta("Actualización", "El pedido ha sido modificado.");
        cerrarVentana();
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) tlb_pedidos.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

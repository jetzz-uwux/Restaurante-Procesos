package progs.restaurante.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import progs.restaurante.datos.OrdenesDAO;

public class VentanaCerrarCuentaController implements Initializable {

    @FXML
    private TextField txt_buscarMesa;
    @FXML
    private TableView<Producto> tlb_resumen;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, Integer> colCantidad;
    @FXML
    private TableColumn<Producto, Double> colPrecio;
    @FXML
    private TableColumn<Producto, Double> colSubtotal;
    @FXML
    private Button btn_cerrarCuenta;

    private Orden ordenActual;
    private OrdenesDAO ordenesDAO = new OrdenesDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        colSubtotal.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            return new javafx.beans.property.SimpleDoubleProperty(p.getPrecio() * p.getStock()).asObject();
        });
    }

    @FXML
    private void handleBuscar() {
        String numMesaText = txt_buscarMesa.getText();

        if (numMesaText.isEmpty()) {
            mostrarAlerta("Atención", "Ingresa un número de mesa para buscar.");
            return;
        }

        try {
            int numMesa = Integer.parseInt(numMesaText);
            Orden pedidoEncontrado = ordenesDAO.buscarPedidoActivoPorMesa(numMesa);

            if (pedidoEncontrado != null) {
                this.ordenActual = pedidoEncontrado;

                ObservableList<Producto> itemsTabla = agruparProductos(pedidoEncontrado.getItems());
                tlb_resumen.setItems(itemsTabla);

            } else {
                mostrarAlerta("Sin resultados", "No hay pedidos activos para la mesa " + numMesa);
                limpiarInterfaz();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El número de mesa debe ser un valor numérico.");
        }
    }

    @FXML
    private void handleCerrar() {
        if (ordenActual == null) {
            mostrarAlerta("Atención", "Primero debe buscar una mesa con un pedido.");
            return;
        }

        ordenActual.setEstado("Cerrado");
        ordenesDAO.actualizarOrden(ordenActual);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cuenta Cerrada");
        alert.setHeaderText("Folio generado con éxito");
        alert.setContentText("ID de Pedido: " + ordenActual.getIdPedido()
                + "\nIndique al cliente que pase a caja con este número.");
        alert.showAndWait();

        ((Stage) btn_cerrarCuenta.getScene().getWindow()).close();
    }

    private ObservableList<Producto> agruparProductos(ArrayList<Producto> items) {
        Map<String, Producto> mapa = new HashMap<>();
        for (Producto p : items) {
            if (mapa.containsKey(p.getNombre())) {
                Producto existente = mapa.get(p.getNombre());
                existente.setStock(existente.getStock() + 1);
            } else {
                Producto nuevo = new Producto(p.getNombre(), p.getPrecio(), 1) {
                };
                mapa.put(p.getNombre(), nuevo);
            }
        }
        return FXCollections.observableArrayList(mapa.values());
    }

    private void limpiarInterfaz() {
        tlb_resumen.setItems(null);
        ordenActual = null;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

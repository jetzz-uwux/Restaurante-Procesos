package progs.restaurante.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import javafx.stage.Stage;
import progs.restaurante.Mesa;
import progs.restaurante.Producto;
import progs.restaurante.Orden;
// IMPORTAMOS LOS NUEVOS DAO
import progs.restaurante.datos.ProductoDAO;
import progs.restaurante.datos.OrdenesDAO;

public class VentanaNuevoPedidoController implements Initializable {

    private ObservableList<Orden> listaPrincipal;
    private ArrayList<Producto> seleccionadosParaLaOrden = new ArrayList<>();

    // Instanciar los DAOs para acceso a BD
    private ProductoDAO productoDAO = new ProductoDAO();
    private OrdenesDAO ordenDAO = new OrdenesDAO();

    @FXML
    private TableView<Producto> tlb_pedidos;
    @FXML
    private TableColumn<Producto, String> clb_1;
    @FXML
    private TableColumn<Producto, Integer> clb_2;
    @FXML
    private TableColumn<Producto, Double> clb_3;
    @FXML
    private TableColumn<Producto, Double> clb_4;

    @FXML
    private TextField txt_numeroMesa;

    private ObservableList<Producto> productosMenu;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clb_1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        clb_3.setCellValueFactory(new PropertyValueFactory<>("precio"));

        tlb_pedidos.setEditable(true);
        clb_2.setCellValueFactory(new PropertyValueFactory<>("stock"));
        clb_2.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        
        productosMenu = productoDAO.listarMenu();
        tlb_pedidos.setItems(productosMenu);

        clb_4.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            double subtotal = p.getPrecio() * p.getStock();
            return new javafx.beans.property.SimpleDoubleProperty(subtotal).asObject();
        });
    }

    @FXML
    private void handleCambioCantidad(TableColumn.CellEditEvent<Producto, Integer> event) {
        Producto p = event.getRowValue();
        int nuevaCantidad = event.getNewValue();

        // Limpiamos ocurrencias previas de este producto si se edita la celda varias veces
        seleccionadosParaLaOrden.removeIf(prod -> prod.getNombre().equals(p.getNombre()));

        p.reducirStock(-nuevaCantidad);

        for (int i = 0; i < nuevaCantidad; i++) {
            seleccionadosParaLaOrden.add(p);
        }

        tlb_pedidos.refresh();
    }

    @FXML
    private void handleGenerar() {
        try {
            if (seleccionadosParaLaOrden.isEmpty()) {
                mostrarAlerta("Error", "No has ingresado cantidades para ningún producto.");
                return;
            }

            int numMesaIngresado = Integer.parseInt(txt_numeroMesa.getText());
            Mesa mesaReal = new Mesa(numMesaIngresado);
            mesaReal.setEstado("Ocupada");

            // Creamos el objeto Orden
            Orden nuevaOrden = new Orden(0, mesaReal); // El ID lo dará la BD
            nuevaOrden.getItems().addAll(seleccionadosParaLaOrden);
            nuevaOrden.setEstado("Pendiente");

            // CAMBIO 2: Guardar permanentemente en la Base de Datos
            ordenDAO.registrarOrden(nuevaOrden);

            // Actualizar la lista visual de la ventana principal si es necesario
            if (listaPrincipal != null) {
                listaPrincipal.add(nuevaOrden);
            }

            mostrarAlerta("Éxito", "Pedido guardado en BD para la mesa: " + numMesaIngresado);
            ((Stage) txt_numeroMesa.getScene().getWindow()).close();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Por favor, ingresa un número de mesa válido.");
        } catch (Exception e) {
            mostrarAlerta("Error de BD", "No se pudo guardar el pedido: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        ((Stage) tlb_pedidos.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void setListaPedidos(ObservableList<Orden> lista) {
        this.listaPrincipal = lista;
    }
}

package progs.restaurante.controllers;

import java.net.URL;
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
import progs.restaurante.Orden;
import progs.restaurante.Producto;
import progs.restaurante.datos.OrdenesDAO;
import progs.restaurante.datos.ProductoDAO;

public class VentanaEditarPedidoController implements Initializable {

    @FXML
    private TableView<Producto> tlb_pedidos;
    @FXML
    private TableColumn<Producto, String> clb_1; // Nombre
    @FXML
    private TableColumn<Producto, Integer> clb_2; // Cantidad (Editable)
    @FXML
    private TableColumn<Producto, Double> clb_3; // Precio
    @FXML
    private TableColumn<Producto, Double> clb_4; // Subtotal
    @FXML
    private TextField txt_numeroMesa;

    private ObservableList<Producto> productosMenu = FXCollections.observableArrayList();
    private Orden ordenOriginal;
    private OrdenesDAO ordenesDAO = new OrdenesDAO();
    private ProductoDAO productoDAO = new ProductoDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        clb_1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        clb_3.setCellValueFactory(new PropertyValueFactory<>("precio"));

        tlb_pedidos.setEditable(true);
        clb_2.setCellValueFactory(new PropertyValueFactory<>("stock"));
        clb_2.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        clb_4.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            return new javafx.beans.property.SimpleDoubleProperty(p.getPrecio() * p.getStock()).asObject();
        });

        productosMenu = productoDAO.listarMenu();
        tlb_pedidos.setItems(productosMenu);
    }

    public void cargarDatos(Orden orden) {
        this.ordenOriginal = orden;
        this.txt_numeroMesa.setText(String.valueOf(orden.getMesa().getNumero()));

        for (Producto pMenu : productosMenu) {
            int contador = 0;
            
            if (orden.getItems() != null) {
                for (Producto pOrden : orden.getItems()) {
                    if (pMenu.getNombre().trim().equalsIgnoreCase(pOrden.getNombre().trim())) {
                        contador++;
                    }
                }
            }
            
            pMenu.setStock(contador);
        }
        tlb_pedidos.refresh();
    }

    @FXML
    private void handleAceptar() {
        ordenOriginal.getItems().clear();
        for (Producto p : productosMenu) {
            if (p.getStock() > 0) {
                for (int i = 0; i < p.getStock(); i++) {
                    ordenOriginal.getItems().add(p);
                }
            }
        }

        try {
            ordenesDAO.actualizarOrden(ordenOriginal);
            ordenesDAO.actualizarDetalles(ordenOriginal);

            cerrarVentana();
        } catch (Exception e) {
            System.out.println("Error al actualizar: " + e.getMessage());
        }
    }

    @FXML
    private void handleCambioCantidad(TableColumn.CellEditEvent<Producto, Integer> event) {
        Producto p = event.getRowValue();
        p.reducirStock(p.getStock()); // Reset
        p.reducirStock(-event.getNewValue()); // Nueva cantidad
        tlb_pedidos.refresh();
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        ((Stage) tlb_pedidos.getScene().getWindow()).close();
    }
}

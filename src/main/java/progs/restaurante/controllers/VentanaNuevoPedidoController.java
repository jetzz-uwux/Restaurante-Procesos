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
import javafx.scene.control.cell.TextFieldTableCell; // Para editar celdas
import javafx.util.converter.IntegerStringConverter; // Para convertir texto a número
import javafx.stage.Stage;
import progs.restaurante.Mesa;
import progs.restaurante.Producto;
import progs.restaurante.Empleados.Mesero;
import progs.restaurante.Orden;
import progs.restaurante.Productos.Platillo;
import progs.restaurante.Productos.Bebida;

public class VentanaNuevoPedidoController implements Initializable {

    private ObservableList<Orden> listaPrincipal;
    
    // Aquí guardamos los productos finales con su cantidad
    private ArrayList<Producto> seleccionadosParaLaOrden = new ArrayList<>();

    @FXML
    private TableView<Producto> tlb_pedidos;
    @FXML
    private TableColumn<Producto, String> clb_1; // Platillo/Bebida
    @FXML
    private TableColumn<Producto, Integer> clb_2; // Cantidad (Editable)
    @FXML
    private TableColumn<Producto, Double> clb_3;  // Precio unitario
    @FXML
    private TableColumn<Producto, Double> clb_4;  // Subtotal

    @FXML
    private TextField txt_numeroMesa;

    private ObservableList<Producto> productosMenu = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        clb_1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        clb_3.setCellValueFactory(new PropertyValueFactory<>("precio"));
       
        tlb_pedidos.setEditable(true); // Habilita edición en la tabla
        clb_2.setCellValueFactory(new PropertyValueFactory<>("stock")); 
        
        // Esto permite que al dar doble clic aparezca un TextField dentro de la celda
        clb_2.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        
        productosMenu.add(new Platillo("Tacos al Pastor", 50.0, 0));
        productosMenu.add(new Platillo("Enchiladas", 85.0, 0));
        productosMenu.add(new Bebida("Horchata", 25.0, 0));
        productosMenu.add(new Bebida("Refresco", 30.0, 0));

        tlb_pedidos.setItems(productosMenu);
        
        clb_4.setCellValueFactory(cellData -> {
    Producto p = cellData.getValue();
    
    double subtotal = p.getPrecio() * p.getStock(); 
    return new javafx.beans.property.SimpleDoubleProperty(subtotal).asObject();
});
    }

    // MÉTODO QUE SE ACTIVA AL DAR ENTER EN LA CELDA DE CANTIDAD
    @FXML
    private void handleCambioCantidad(TableColumn.CellEditEvent<Producto, Integer> event) {
        Producto p = event.getRowValue();
        int nuevaCantidad = event.getNewValue();
        
        // Actualiza el "stock" del objeto en la tabla para que se vea el número
        p.reducirStock(-nuevaCantidad); //como reducirStock resta, pasamos a negativo para que sume
        
        // Añadimos a nuestra lista real de la orden
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

            if (listaPrincipal != null) {
                Orden nuevaOrden = new Orden(listaPrincipal.size() + 1, mesaReal);
                nuevaOrden.getItems().addAll(seleccionadosParaLaOrden);
                listaPrincipal.add(nuevaOrden);
            }

            mostrarAlerta("Éxito", "Pedido generado para la mesa: " + numMesaIngresado);
            ((Stage) txt_numeroMesa.getScene().getWindow()).close();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Por favor, ingresa un número de mesa válido.");
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
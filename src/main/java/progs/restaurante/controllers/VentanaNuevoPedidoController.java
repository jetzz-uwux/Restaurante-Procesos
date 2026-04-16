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
import javafx.stage.Stage;
import progs.restaurante.Mesa;
import progs.restaurante.Producto;
import progs.restaurante.Empleados.Mesero;

public class VentanaNuevoPedidoController implements Initializable {

    @FXML
    private TableView<Producto> tlb_pedidos;
    @FXML
    private TableColumn<Producto, String> clb_1; // Nombre del producto
    @FXML
    private TableColumn<Producto, Integer> clb_2; // Cantidad
    @FXML
    private TableColumn<Producto, Double> clb_3;  // Precio unitario
    @FXML
    private TableColumn<Producto, Double> clb_4;  // Subtotal

    @FXML
    private TextField txt_numeroMesa; //Campo que muestra el "No. de mesa"

    // Lista que alimenta la tabla de la interfaz
    private ObservableList<Producto> productosEnOrden = FXCollections.observableArrayList();

    // Objetos de lógica de negocio
    private Mesero meseroActual;
    private Mesa mesaActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Enlace de las columnas con los atributos de la clase Producto
        clb_1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        clb_3.setCellValueFactory(new PropertyValueFactory<>("precio"));        
        
        tlb_pedidos.setItems(productosEnOrden);
    }

    public void setDatos(Mesa mesa, Mesero mesero) {
        this.mesaActual = mesa;
        this.meseroActual = mesero;
        this.txt_numeroMesa.setText(String.valueOf(mesa.getNumero()));
    }

    @FXML
    private void handleGenerar() {
        if (productosEnOrden.isEmpty()) {
            mostrarAlerta("Error", "No has agregado ningún producto al pedido.");
            return;
        }
        
        ArrayList<Producto> listaParaRegistrar = new ArrayList<>(productosEnOrden);
        meseroActual.registrarPedido(mesaActual, listaParaRegistrar);

        mostrarAlerta("Éxito", "Pedido enviado con éxito a la pantalla de cocina.");

        // Cerrar la ventana después de generar
        Stage stage = (Stage) tlb_pedidos.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancelar() {
        //Cierra la ventana sin guardar cambios
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

package progs.restaurante.controllers;

import java.io.IOException;
import progs.restaurante.Mesa;
import progs.restaurante.Orden; // Importante para la tabla de pedidos
import progs.restaurante.Empleados.Mesero;
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
        
    @FXML private TableView<Orden> tlb_pedidos;
    @FXML private TableColumn<Orden, Integer> clb_1; // No. Pedido
    @FXML private TableColumn<Orden, Integer> clb_2; // No. de mesa
    @FXML private TableColumn<Orden, String> clb_3;  // Estado
    @FXML private TableColumn<Orden, Double> clb_4;  // Total
    
    @FXML private TextField txt_buscar;
    
    private ObservableList<Orden> listaPedidos = FXCollections.observableArrayList();
    private Mesero meseroActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        clb_1.setCellValueFactory(new PropertyValueFactory<>("idPedido")); 
        clb_2.setCellValueFactory(new PropertyValueFactory<>("numeroMesa"));
        clb_3.setCellValueFactory(new PropertyValueFactory<>("estado"));
        clb_4.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        tlb_pedidos.setItems(listaPedidos);
    }    
    
    @FXML
    private void handleNuevoPedido() {        
        Orden pedidoSeleccionado = tlb_pedidos.getSelectionModel().getSelectedItem();
        
        if (pedidoSeleccionado == null) {
            mostrarAlerta("Atención", "Por favor, selecciona una fila de la tabla para continuar.");
            return;
        }

        try {            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/progs/restaurante/view/VentanaNuevoPedido.fxml"));
            Parent root = loader.load();
                                    
            Mesa mesaParaAtender = pedidoSeleccionado.getMesa();             
            
            Stage stage = new Stage();
            stage.setTitle("Registro de Nuevo Pedido - Mesa " + mesaParaAtender.getNumero());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tlb_pedidos.getScene().getWindow());
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {        
            mostrarAlerta("Error", "No se pudo abrir la ventana de pedidos: " + e.getMessage());
        }
    }
    
    @FXML
private void handleBtnCancelarPedido() {    
    Orden seleccionado = tlb_pedidos.getSelectionModel().getSelectedItem();

    if (seleccionado == null) {
        mostrarAlerta("Atención", "Selecciona un pedido de la tabla para cancelarlo.");
        return;
    }
    
    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
    confirmacion.setTitle("Confirmar Cancelación");
    confirmacion.setHeaderText("¿Estás seguro de cancelar el pedido?");
    confirmacion.setContentText("Esta acción no se puede deshacer.");

    if (confirmacion.showAndWait().get() == ButtonType.OK) {       
        listaPedidos.remove(seleccionado);
                
        seleccionado.getMesa().setEstado("Disponible");
        
        mostrarAlerta("Éxito", "El pedido ha sido cancelado correctamente.");
    }
}
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
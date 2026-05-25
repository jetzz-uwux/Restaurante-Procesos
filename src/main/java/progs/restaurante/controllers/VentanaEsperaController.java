package progs.restaurante.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import progs.restaurante.Cliente;

public class VentanaEsperaController {
    
    @FXML
    private TextField txt_nombreCliente;
    @FXML
    private TextField txt_cantidadPersonas;

    @FXML
    private TableView<Cliente> tbl_listaEspera;
    @FXML
    private TableColumn<Cliente, String> colNombre;
    @FXML
    private TableColumn<Cliente, Integer> colPersonas;
    @FXML
    private TableColumn<Cliente, String> colHora;
    
    private ObservableList<Cliente> listaClientesEnEspera = FXCollections.observableArrayList();

    /**
     * El método initialize se ejecuta automáticamente cuando se carga la
     * pantalla. Aquí configuramos las columnas de la tabla.
     */
    @FXML
    public void initialize() {        
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPersonas.setCellValueFactory(new PropertyValueFactory<>("personas"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("horaLlegada"));
       
        tbl_listaEspera.setItems(listaClientesEnEspera);
    }

    @FXML
    private void handleAgregarCliente() {
        String nombre = txt_nombreCliente.getText().trim();
        String personasStr = txt_cantidadPersonas.getText().trim();
        
        if (nombre.isEmpty() || personasStr.isEmpty()) {
            mostrarAlerta("Atención", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        int cantidadPersonas;        
        try {
            cantidadPersonas = Integer.parseInt(personasStr);
            if (cantidadPersonas <= 0) {
                mostrarAlerta("Error", "La cantidad de personas debe ser un número entero positivo.", Alert.AlertType.ERROR);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad de personas debe ser un número entero positivo.", Alert.AlertType.ERROR);
            return;
        }

        Cliente nuevoCliente = new Cliente(nombre, cantidadPersonas);
        listaClientesEnEspera.add(nuevoCliente);
        
        limpiarCampos();
        mostrarAlerta("Éxito", "Cliente registrado correctamente en la lista.", Alert.AlertType.INFORMATION);
    }
  
    @FXML
    private void handleAsignarMesa() {        
        Cliente clienteSeleccionado = tbl_listaEspera.getSelectionModel().getSelectedItem();

        if (clienteSeleccionado == null) {
            mostrarAlerta("Atención", "Debe seleccionar un cliente de la lista para asignarle una mesa.", Alert.AlertType.WARNING);
            return;
        }

        listaClientesEnEspera.remove(clienteSeleccionado);
        
        tbl_listaEspera.getSelectionModel().clearSelection();

        mostrarAlerta("Mesa Asignada", "El cliente " + clienteSeleccionado.getNombre() + " ha sido removido de la lista de espera.", Alert.AlertType.INFORMATION);
    }

    /**
     * Limpia las cajas de texto de la interfaz
     */
    private void limpiarCampos() {
        txt_nombreCliente.clear();
        txt_cantidadPersonas.clear();
    }

    /**
     * Método auxiliar genérico para mostrar ventanas de diálogo (Alerts)
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

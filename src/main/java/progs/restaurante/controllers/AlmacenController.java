package progs.restaurante.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import progs.restaurante.Item;
import progs.restaurante.Item;

public class AlmacenController {

    @FXML private TableView<Item> tabla;
    @FXML private TableColumn<Item, String> colNombre;
    @FXML private TableColumn<Item, Integer> colCantidad;
    @FXML private TableColumn<Item, String> colUnidad;

    @FXML private TextField txtNombre;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtUnidad;

    private final ObservableList<Item> lista = FXCollections.observableArrayList();

    /**
     * Inicializar tabla del almacén
     */
    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(data -> data.getValue().nameProperty());
        colCantidad.setCellValueFactory(data -> data.getValue().quantityProperty().asObject());
        colUnidad.setCellValueFactory(data -> data.getValue().unitProperty());

        tabla.setItems(lista);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                txtNombre.setText(newItem.getName());
                txtCantidad.setText(String.valueOf(newItem.getQuantity()));
                txtUnidad.setText(newItem.getUnit());
            }
        });
    }

    /**
     * Agregar materia prima al almacén
     */
    @FXML
    private void agregar() {
        if (!validarCampos()) return;

        String nombre = txtNombre.getText();
        int cantidad = Integer.parseInt(txtCantidad.getText());
        String unidad = txtUnidad.getText();

        lista.add(new Item(nombre, cantidad, unidad));
        limpiarCampos();
    }

    /**
     * Editar productos disponibles
     */
    @FXML
    private void editar() {
        Item seleccionado = tabla.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Aviso", "Selecciona un elemento para editar");
            return;
        }

        if (!validarCampos()) return;

        seleccionado.setName(txtNombre.getText());
        seleccionado.setQuantity(Integer.parseInt(txtCantidad.getText()));
        seleccionado.setUnit(txtUnidad.getText());

        tabla.refresh();
        limpiarCampos();
    }

    /**
     * Eliminar productos
     */
    @FXML
    private void eliminar() {
        Item seleccionado = tabla.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Aviso", "Selecciona un elemento para eliminar");
            return;
        }

        lista.remove(seleccionado);
        limpiarCampos();
    }

    /**
     * Limpiar campos de la tabla
     */
    @FXML
    private void limpiarCampos() {
        txtNombre.clear();
        txtCantidad.clear();
        txtUnidad.clear();
        tabla.getSelectionModel().clearSelection();
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() ||
            txtCantidad.getText().isEmpty() ||
            txtUnidad.getText().isEmpty()) {

            mostrarAlerta("Error", "Todos los campos son obligatorios");
            return false;
        }

        try {
            Integer.parseInt(txtCantidad.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "La cantidad debe ser un número");
            return false;
        }

        return true;
    }

    /**
     * Mensaje de error 
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
package progs.restaurante.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import progs.restaurante.Producto;
import progs.restaurante.Productos.Bebida;
import progs.restaurante.Productos.Platillo;
import progs.restaurante.Productos.Postre;
import progs.restaurante.datos.ProductoDAO;

public class VentanaMenuController {

    @FXML
    private TextField txt_nombrePlatillo;
    @FXML
    private TextField txt_precioPlatillo;
    @FXML
    private ComboBox<String> cmb_categoria;
    @FXML
    private CheckBox chk_disponible;

    @FXML
    private TableView<Producto> tbl_menu;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, Double> colPrecio;
    @FXML
    private TableColumn<Producto, String> colCategoria;
    @FXML
    private TableColumn<Producto, String> colDisponible;

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private ProductoDAO productoDAO = new ProductoDAO();
    private Producto productoSeleccionado = null;

    @FXML
    public void initialize() {
        cmb_categoria.setItems(FXCollections.observableArrayList("Platillo", "Bebida", "Postre"));

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        colDisponible.setCellValueFactory(cellData -> {
            boolean disponible = cellData.getValue().isDisponible();
            return new javafx.beans.property.SimpleStringProperty(disponible ? "Disponible" : "Agotado");
        });

        tbl_menu.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                productoSeleccionado = newSelection;
                txt_nombrePlatillo.setText(productoSeleccionado.getNombre());
                txt_precioPlatillo.setText(String.valueOf(productoSeleccionado.getPrecio()));
                cmb_categoria.setValue(productoSeleccionado.getCategoria());
                chk_disponible.setSelected(productoSeleccionado.isDisponible());
            }
        });

        cargarMenuDesdeBD();
    }

    private void cargarMenuDesdeBD() {
        listaProductos.clear();
        listaProductos.addAll(productoDAO.listarMenu());
        tbl_menu.setItems(listaProductos);
    }

    @FXML
    private void handleGuardarCambios() {
        String nombre = txt_nombrePlatillo.getText().trim();
        String precioStr = txt_precioPlatillo.getText().trim();
        String categoria = cmb_categoria.getValue();
        boolean disponible = chk_disponible.isSelected();

        if (nombre.isEmpty() || precioStr.isEmpty() || categoria == null) {
            mostrarAlerta("Atención", "Todos los campos son obligatorios para guardar el producto.", Alert.AlertType.WARNING);
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio <= 0) {
                mostrarAlerta("Error", "El precio debe ser un número decimal mayor a cero.", Alert.AlertType.ERROR);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El formato del precio es inválido. Ingrese un valor numérico.", Alert.AlertType.ERROR);
            return;
        }

        if (productoSeleccionado == null) {
            Producto nuevoProducto = null;

            switch (categoria) {
                case "Platillo":
                    nuevoProducto = new Platillo(nombre, precio, disponible);
                    break;
                case "Bebida":
                    nuevoProducto = new Bebida(nombre, precio, disponible);
                    break;
                case "Postre":
                    nuevoProducto = new Postre(nombre, precio, disponible);
                    break;
            }

            listaProductos.add(nuevoProducto);
            mostrarAlerta("Registro Exitoso", "El nuevo producto se ha añadido a la carta.", Alert.AlertType.INFORMATION);

        } else {
            productoSeleccionado.setNombre(nombre);
            productoSeleccionado.setPrecio(precio);
            productoSeleccionado.setDisponible(disponible);

            tbl_menu.refresh();
            mostrarAlerta("Actualización Exitosa", "Los datos del producto han sido modificados.", Alert.AlertType.INFORMATION);
        }

        limpiarFormulario();
    }

    @FXML
    private void handleCancelar() {
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        txt_nombrePlatillo.clear();
        txt_precioPlatillo.clear();
        cmb_categoria.setValue(null);
        chk_disponible.setSelected(true);
        tbl_menu.getSelectionModel().clearSelection();
        productoSeleccionado = null;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

package progs.restaurante.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import progs.restaurante.Venta;
import progs.restaurante.datos.OrdenesDAO;

public class VentanaVentasController implements Initializable {

    @FXML private TableView<Venta> tbl_ventas;
    @FXML private TableColumn<Venta, Integer> col_id;
    @FXML private TableColumn<Venta, Integer> col_pedido;
    @FXML private TableColumn<Venta, Integer> col_mesa;
    @FXML private TableColumn<Venta, Double> col_total;
    @FXML private TableColumn<Venta, Object> col_fecha;

    private OrdenesDAO dao = new OrdenesDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        col_id.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        col_pedido.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        col_mesa.setCellValueFactory(new PropertyValueFactory<>("numeroMesa"));
        col_total.setCellValueFactory(new PropertyValueFactory<>("total"));
        col_fecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));

        cargarVentas();
    }

    private void cargarVentas() {
        ObservableList<Venta> lista = dao.listarVentas();
        tbl_ventas.setItems(lista);
    } 
}
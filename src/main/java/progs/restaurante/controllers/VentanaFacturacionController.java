package progs.restaurante.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import progs.restaurante.Orden;
import progs.restaurante.Producto;
import progs.restaurante.datos.OrdenesDAO;

public class VentanaFacturacionController implements Initializable {

    @FXML
    private TextField txt_idFactura;
    @FXML
    private TableView<Orden> tlb_facturacion;
    @FXML
    private TableColumn<Orden, Integer> colMesa;
    @FXML
    private TableColumn<Orden, Double> colTotal;
    @FXML
    private ComboBox<String> cmb_metodoPago;

    private Orden ordenActual;
    private OrdenesDAO ordenesDAO = new OrdenesDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar columnas de la tabla (usando los métodos de tu clase Orden)
        colMesa.setCellValueFactory(new PropertyValueFactory<>("numeroMesa"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Opciones de pago según CU-08
        cmb_metodoPago.setItems(FXCollections.observableArrayList("Efectivo", "Tarjeta"));
        cmb_metodoPago.setValue("Efectivo");
    }

    @FXML
    private void handleBuscarPedido() {
        try {
            int id = Integer.parseInt(txt_idFactura.getText());
            Orden encontrada = ordenesDAO.buscarPedidoPorId(id);

            // Regla de negocio: Solo cobrar si está "Cerrado"
            if (encontrada != null && "Cerrado".equals(encontrada.getEstado())) {
                this.ordenActual = encontrada;
                ObservableList<Orden> data = FXCollections.observableArrayList(encontrada);
                tlb_facturacion.setItems(data);
            } else {
                mostrarAlerta("Atención", "Pedido no encontrado o no ha sido cerrado por el mesero.");
                tlb_facturacion.setItems(null);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Ingresa un folio numérico válido.");
        }
    }

    @FXML
    private void handleTicket() {
        if (ordenActual == null) {
            mostrarAlerta("Error", "Busca un pedido primero.");
            return;
        }

        System.out.println("\n======= TICKET DE VENTA (Tacos Don Juanito) =======");
        System.out.println("Folio Pedido: " + ordenActual.getIdPedido());
        System.out.println("Mesa: " + ordenActual.getNumeroMesa());
        System.out.println("Fecha: " + java.time.LocalDateTime.now());
        System.out.println("------------------------------------------------");
        for (Producto p : ordenActual.getItems()) {
            System.out.printf("%-20s $%.2f\n", p.getNombre(), p.getPrecio());
        }
        System.out.println("------------------------------------------------");
        System.out.printf("TOTAL: $%.2f\n", ordenActual.getTotal());
        System.out.println("================================================\n");
    }

    @FXML
    private void handleFactura() {
        if (ordenActual == null) {
            return;
        }

        //Generar folio único
        String folioFiscal = UUID.randomUUID().toString().toUpperCase().substring(0, 8);

        System.out.println("\n--- SIMULACIÓN DE FACTURA FISCAL ---");
        System.out.println("RFC EMISOR: RUV900101UV1");
        System.out.println("FOLIO FISCAL: " + folioFiscal);
        System.out.println("METODO DE PAGO: " + cmb_metodoPago.getValue());
        System.out.println("------------------------------------");
        //Calcula el IVA de 16%
        double subtotal = ordenActual.getTotal() / 1.16;
        double iva = subtotal * 0.16;
        System.out.printf("SUBTOTAL:     $%.2f\n", subtotal);
        System.out.printf("IVA (16%%):    $%.2f\n", iva);
        System.out.printf("TOTAL:        $%.2f\n", ordenActual.getTotal());
        System.out.println("------------------------------------\n");
    }

    @FXML
    private void handleFinalizarPago() {
        if (ordenActual == null) {
            return;
        }

        // Actualiza BD (Estado Pagado + Mesa Disponible)
        ordenesDAO.finalizarVenta(ordenActual);

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Venta Exitosa");
        success.setContentText("Pago registrado. La mesa " + ordenActual.getNumeroMesa() + " ahora está libre.");
        success.showAndWait();

        limpiarInterfaz();
    }

    private void limpiarInterfaz() {
        txt_idFactura.clear();
        tlb_facturacion.setItems(null);
        ordenActual = null;
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}

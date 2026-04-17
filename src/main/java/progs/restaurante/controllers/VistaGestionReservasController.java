package progs.restaurante.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import progs.restaurante.Mesa;
import progs.restaurante.Orden;
import progs.restaurante.Reservacion;
import progs.restaurante.lib.CSS;

public class VistaGestionReservasController implements Initializable {

    @FXML private Button btn_buscar, btn_cancelar, btn_nuevo;
    @FXML private TextField txt_buscar;
    @FXML private TableView<Reservacion> tlb_reservaciones;
    @FXML private TableColumn<Reservacion, String> clb_1; //Número de reservación
    @FXML private TableColumn<Reservacion, String> clb_2; //Nombre del cliente
    @FXML private TableColumn<Reservacion, String> clb_3; //Mesa asignada
    @FXML private TableColumn<Reservacion, String> clb_4; //Estado
    
    private ObservableList<Reservacion> listareservaciones = FXCollections.observableArrayList();

    CSS vista = new CSS(); //Para cargar las fuentes
    private ObservableList<Orden> listareservas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clb_1.setCellValueFactory(new PropertyValueFactory<>("numreservacion"));
        clb_2.setCellValueFactory(new PropertyValueFactory<>("nombrecliente"));
        clb_3.setCellValueFactory(new PropertyValueFactory<>("numeroMesa"));
        clb_4.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tlb_reservaciones.setItems(listareservaciones);

        Mesa mesa1 = new Mesa(5);
        mesa1.setEstado("Ocupada");
        Reservacion reservacionprueba = new Reservacion("1", "Leonardo DiCaprio", mesa1, "Activa");
        Mesa mesa2 = new Mesa(2);
        mesa1.setEstado("Ocupada");
        Reservacion reservacionprueba2 = new Reservacion("2", "Taylor Swift", mesa2, "Activa");
        listareservaciones.add(reservacionprueba);
        listareservaciones.add(reservacionprueba2);
        tlb_reservaciones.setItems(listareservaciones);
    }
    
    @FXML
    private void handleBuscar(){
        String filtro = txt_buscar.getText().toLowerCase(); // Lo que escribió el usuario
    
        // Si el buscador está vacío, mostramos la lista original completa
        if (filtro.isEmpty()) {
            tlb_reservaciones.setItems(listareservaciones);
            return;
        }

        // Creamos una lista temporal para guardar los resultados
        ObservableList<Reservacion> resultados = FXCollections.observableArrayList();

        for (Reservacion res : listareservaciones) {
            // ¿El nombre o el número coinciden con lo que busco?
            if (res.getNombrecliente().toLowerCase().contains(filtro) || 
                res.getNumreservacion().toLowerCase().contains(filtro)) {

                resultados.add(res); // Si sí, lo agregamos a los resultados
            }
        }

        // Le decimos a la tabla que ahora muestre solo los resultados
        tlb_reservaciones.setItems(resultados);
    }
     @FXML
    private void handleNuevo(){
         try {
             
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/progs/fxml/VentanaNuevaReservacion.fxml"));
            Parent root = loader.load(); 
            VistaNuevaReservacionController controller = loader.getController();
            controller.setListaReservas(this.listareservaciones);
            vista.cargarVistaCSS(root, btn_nuevo);


        } catch (IOException e) {
            System.err.println("Error al regresar a la pantalla anterior: " + e.getMessage());
            e.printStackTrace();
        }
    }
     @FXML
    private void handleCancelar(){
        Reservacion seleccionada = tlb_reservaciones.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Atención", "Por favor, selecciona una reservación de la tabla para cancelarla.");
            return;
        }   
        seleccionada.setEstado("Cancelada");
        tlb_reservaciones.refresh();

        mostrarAlerta("Información", "La reservación ahora aparece como Cancelada.");
    }
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    public void setListaReservaciones(ObservableList<Reservacion> lista) {
        this.listareservaciones = lista;
        tlb_reservaciones.setItems(listareservaciones);
    }
}


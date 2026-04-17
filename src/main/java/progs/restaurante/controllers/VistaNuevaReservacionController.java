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
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import progs.restaurante.Mesa;
import progs.restaurante.Reservacion;
import progs.restaurante.lib.CSS;

public class VistaNuevaReservacionController implements Initializable {

    @FXML private Button btn_agregar, btn_cancelar;
    @FXML private TextField txt_numreservacion, txt_nombrecliente, txt_mesa, txt_estado;
    private ObservableList<Reservacion> listaPrincipal;

    
    private ObservableList<Reservacion> listareservaciones = FXCollections.observableArrayList();

    CSS vista = new CSS(); //Para cargar las fuentes

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
    
     @FXML
    private void handleAgregar(){
        try {
            String id = txt_numreservacion.getText();
            String nombre = txt_nombrecliente.getText();
            String mesaTexto = txt_mesa.getText();
            String estado = txt_estado.getText();

            if (id.isEmpty() || nombre.isEmpty() || mesaTexto.isEmpty()) {
                System.out.println("¡Llena todos los campos primero!");
                return;
            }

            int numMesa = Integer.parseInt(mesaTexto);
            Mesa mesaNueva = new Mesa(numMesa);

            Reservacion nueva = new Reservacion(id, nombre, mesaNueva, estado);

            if (listaPrincipal != null) {
                    listaPrincipal.add(nueva); 

                  handleCancelar(); 
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: En la mesa pon solo números.");
        }
    }
     @FXML
    private void handleCancelar(){
        try {
            // 1. Cargamos la vista de Gestión (la anterior)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/progs/fxml/VistaGestionReservas.fxml"));
            Parent root = loader.load();

            // 2. ¡ESTO ES LO CLAVE! 
            // Obtenemos el controlador de la vista a la que vamos a regresar
            VistaGestionReservasController controllerAtras = loader.getController();

            // 3. Le pasamos la lista que ya tiene al nuevo cliente
            controllerAtras.setListaReservaciones(this.listaPrincipal);

            // 4. Mostramos la pantalla usando tu clase CSS
            vista.cargarVistaCSS(root, btn_cancelar);

        } catch (IOException e) {
            System.err.println("Error al regresar: " + e.getMessage());
        }
    }
    
    public void setListaReservas(ObservableList<Reservacion> lista) {
        this.listaPrincipal = lista;
    }
}

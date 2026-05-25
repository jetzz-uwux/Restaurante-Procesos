package progs.restaurante;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import progs.restaurante.lib.EstilosApp;
import progs.restaurante.lib.EstilosApp.CSS;
import static progs.restaurante.lib.EstilosApp.cargarFuentes;

/**
 * JavaFX App aaaa
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        /*FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/progs/fxml/VentanaCerrarCuenta.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Gestión de Pedidos - Restaurante UV");
        stage.setScene(scene);
        stage.show();*/
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/progs/fxml/VentanaGestionMenu.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        // Aquí aplica los CSS de la pantalla de login
        EstilosApp.aplicar(scene,
            CSS.JUEGO,
            CSS.FUENTES,
            CSS.BOTONES,
            CSS.TEXTFIELD,
            CSS.IMAGEN,
            CSS.TEXTO
        );
        cargarFuentes();

        stage.setScene(scene);
        stage.show();

    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}


package progs.restaurante.lib;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CSS {
   
    public void cargarVistaCSS(FXMLLoader loader, Button btn) throws IOException{
        Parent root = loader.load();

        Stage stage = (Stage) btn.getScene().getWindow();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/progs/fonts/fuentes.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }
    
    public void cargarVistaCSS(FXMLLoader loader, Stage stage, String titulo) throws IOException{
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("progs/fonts/fuentes.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle(titulo);

        stage.show();
    }

    public void cargarVistaCSS(Parent root, Button btn) {
    Stage stage = (Stage) btn.getScene().getWindow();
        Scene scene = new Scene(root);

        // Verifica que la ruta de la fuente sea correcta
        URL cssURL = getClass().getResource("/progs/fonts/fuentes.css");
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        }

        stage.setScene(scene);
        stage.show();
    }    

}
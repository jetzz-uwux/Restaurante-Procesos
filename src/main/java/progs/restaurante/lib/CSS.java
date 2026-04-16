
package progs.restaurante.lib;

import java.io.IOException;
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
        scene.getStylesheets().add(getClass().getResource("/fonts/fuentes.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }
    
    public void cargarVistaCSS(FXMLLoader loader, Button btn, String titulo) throws IOException{
        Parent root = loader.load();

        Stage stage = (Stage) btn.getScene().getWindow();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/fonts/fuentes.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle(titulo);

        stage.show();
    }
}
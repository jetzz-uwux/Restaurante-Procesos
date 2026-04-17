module progs.restaurante {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    
    opens progs.fxml to javafx.fxml;
    
    opens progs.restaurante to javafx.fxml;
    opens progs.restaurante.controllers to javafx.fxml;
    exports progs.restaurante;
}

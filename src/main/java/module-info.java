module progs.restaurante {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    requires mysql.connector.j;
    
    opens progs.fxml to javafx.fxml;
    
    opens progs.restaurante to javafx.fxml;
    opens progs.restaurante.controllers to javafx.fxml;
    exports progs.restaurante;        
}

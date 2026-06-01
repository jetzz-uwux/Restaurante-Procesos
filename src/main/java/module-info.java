module progs.restaurante {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    requires java.desktop;        // ← necesario para SystemTray (AWT)
    requires mysql.connector.j;

    opens progs.fxml to javafx.fxml;
    opens progs.restaurante to javafx.fxml;
    opens progs.restaurante.controllers to javafx.fxml, javafx.base, javafx.controls;
    opens progs.restaurante.models to javafx.base, javafx.controls;

    exports progs.restaurante;
}

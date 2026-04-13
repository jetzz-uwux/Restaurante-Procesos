module progs.restaurante {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens progs.restaurante to javafx.fxml;
    exports progs.restaurante;
}

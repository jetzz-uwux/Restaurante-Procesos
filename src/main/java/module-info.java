module progs.restaurante {
    requires javafx.controls;
    requires javafx.fxml;

    opens progs.restaurante to javafx.fxml;
    exports progs.restaurante;
}

module com.nightowl {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;

    opens com.nightowl to javafx.fxml;
    exports com.nightowl;
}

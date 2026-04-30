module com.nightowl {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;

    opens com.nightowl to javafx.fxml;
    exports com.nightowl;
}
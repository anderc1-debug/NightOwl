module com.nightowl {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;
    requires java.sql;
    requires org.apache.derby.engine;

    opens com.nightowl to javafx.fxml;
    exports com.nightowl;
}

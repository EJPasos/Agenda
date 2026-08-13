module com.example.agenda {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;

    opens com.example.agenda to javafx.fxml;
    exports com.example.agenda;

    opens com.example.agenda.view to javafx.fxml;
    exports com.example.agenda.view;

    exports com.example.agenda.model;
}
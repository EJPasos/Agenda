package com.example.agenda;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Asegúrate de que la ruta coincida con donde colocaste Agenda.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/agenda/view/Agenda.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Gestión de Agenda");
        primaryStage.setScene(new Scene(root, 750, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
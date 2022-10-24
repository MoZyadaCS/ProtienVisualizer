package com.example.protienvisualizer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        File file = new File("src/main/resources/com/example/protienvisualizer/main.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(file.toURI().toURL());
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Protein Visualizer Project");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}
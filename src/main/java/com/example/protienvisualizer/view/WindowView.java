package com.example.protienvisualizer.view;

import com.example.protienvisualizer.controller.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.File;
import java.io.IOException;


public class WindowView {
	private final Controller controller;
	private final Parent root;





	public WindowView() throws IOException {
		File file = new File("src/main/resources/com/example/protienvisualizer/main.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(file.toURI().toURL());
		controller = fxmlLoader.getController();
		root = fxmlLoader.load();
	}


	public Controller getController() {
		return controller;
	}

	public Parent getRoot() {
		return root;
	}
}

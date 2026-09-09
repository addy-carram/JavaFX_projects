package com.example.graphic_interface_lab1;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // <-- Added this import
import javafx.scene.Parent;      // <-- Added this import
import javafx.scene.Scene;       // <-- Added this import
import javafx.stage.Stage;       // <-- Added this import
import javafx.scene.control.Label;
import java.io.IOException;
import java.net.URL;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
    @FXML
    protected void onNextTaskClick(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/graphic_interface_lab1/news-view.fxml")
            );
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Next Task Window");
            newStage.setScene(new Scene(root));
            newStage.show();

            // linia care lipsea - închide fereastra veche
            Stage currentStage = (Stage) welcomeText.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            welcomeText.setText("Error: Could not load the new page!");
        }
    }
}

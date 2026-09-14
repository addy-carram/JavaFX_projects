package com.example.catalog_products_interface_lab3_4;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private void onHelloButtonClick(ActionEvent event) throws IOException {

        // incarcam fisierul FXML pentru a doua pagina
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("produs-catalog.fxml"));
        Parent root = fxmlLoader.load();

        // luam stage-ul curent de pe butonul apasat
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        // cream o noua scena cu continutul din process.fxml
        Scene scene = new Scene(root, 320, 240);

        stage.setScene(scene);
        stage.setTitle("Process");
        stage.show();
    }
}

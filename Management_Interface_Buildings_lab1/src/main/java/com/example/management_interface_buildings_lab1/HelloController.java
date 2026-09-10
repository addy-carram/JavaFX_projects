package com.example.management_interface_buildings_lab1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    @FXML
    private void onHelloButtonClick(ActionEvent event) throws IOException {

        // incarcam fisierul FXML pentru a doua pagina
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("procces.fxml"));
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
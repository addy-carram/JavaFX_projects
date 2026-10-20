package com.example.clients_row_lab14;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ClientNouDialog {
    private final Stage dialog = new Stage();

    public ClientNouDialog(Stage parinte, Consumer<String> laAdaugare) {
        TextField nume = new TextField();
        nume.setPromptText("Nume client");
        ComboBox<String> serviciu = new ComboBox<>();
        serviciu.setPromptText("Serviciu");
        serviciu.getItems().addAll("Depunere", "Retragere", "Informații");

        Button btnAdauga = new Button("_Adaugă");
        btnAdauga.setDefaultButton(true);   // Enter
        btnAdauga.disableProperty().bind(nume.textProperty().isEmpty());
        btnAdauga.setOnAction(e -> {
            laAdaugare.accept(nume.getText() + " – " + serviciu.getValue());
            dialog.close();
        });

        Button btnAnuleaza = new Button("A_nulează");
        btnAnuleaza.setCancelButton(true);  // Esc
        btnAnuleaza.setOnAction(e -> dialog.close());

        VBox root = new VBox(10, new Label("Nume:"), nume,
                new Label("Serviciu:"), serviciu,
                new HBox(8, btnAdauga, btnAnuleaza));
        root.setPadding(new Insets(12));
        dialog.setOnShown(e -> { nume.clear(); nume.requestFocus(); });
        dialog.initOwner(parinte);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Client nou");
        dialog.setScene(new Scene(root, 300, 220));
    }

    public void arata() { dialog.show(); }
}

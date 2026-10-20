package com.example.learning_command_lab14;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CautaDialog {
    private final Stage dialog = new Stage();
    private int pozitie = 0;

    public CautaDialog(Stage parinte, TextArea editor) {
        TextField cautat = new TextField();
        cautat.setPromptText("Ce caut?");
        Label rezultat = new Label();

        Button btnGaseste = new Button("_Găsește următorul");
        btnGaseste.setDefaultButton(true);   // Enter
        btnGaseste.disableProperty().bind(cautat.textProperty().isEmpty());

        Button btnInchide = new Button("În_chide");
        btnInchide.setCancelButton(true);    // Esc

        btnGaseste.setOnAction(e -> {
            String text = editor.getText().toLowerCase();
            String cuv = cautat.getText().toLowerCase();
            int gasit = text.indexOf(cuv, pozitie);
            if (gasit < 0 && pozitie > 0) {        // reluăm de la început
                gasit = text.indexOf(cuv);
            }
            if (gasit >= 0) {
                editor.selectRange(gasit, gasit + cuv.length());
                pozitie = gasit + cuv.length();
                rezultat.setText("Găsit la poziția " + gasit);
            } else {
                rezultat.setText("Nu s-a găsit");
            }
        });
        btnInchide.setOnAction(e -> dialog.close());

        HBox butoane = new HBox(8, btnGaseste, btnInchide);
        VBox root = new VBox(10, cautat, butoane, rezultat);
        root.setPadding(new Insets(12));

        dialog.initOwner(parinte);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Găsește");
        dialog.setScene(new Scene(root, 320, 130));
        dialog.setOnShown(e -> { pozitie = 0; cautat.requestFocus(); });
    }

    public void arata() { dialog.show(); }
}
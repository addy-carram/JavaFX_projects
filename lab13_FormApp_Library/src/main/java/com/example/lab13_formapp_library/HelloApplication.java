package com.example.lab13_formapp_library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private final Label stare = new Label("Completează formularul");

    @Override
    public void start(Stage stage) {
        TextField nume = camp("Nume Prenume");
        TextField idnp = camp("IDNP, 13 cifre");
        TextField telefon = camp("Telefon, 0XXXXXXXX");
        TextField email = camp("Email");

        TextField[] campuri = {nume, idnp, telefon, email};
        Label[] erori = {eroare(), eroare(), eroare(), eroare()};
        String[] reguli = {"[A-Za-zĂÂÎȘȚăâîșț\\- ]{3,}", "\\d{13}", "0\\d{8}", "[\\w.]+@[\\w.]+\\.[a-z]{2,}"};
        String[] mesaje = {"Minimum 3 litere", "Exact 13 cifre", "0 urmat de 8 cifre", "Email invalid"};

        // Validare la pierderea focusului
        for (int i = 0; i < campuri.length; i++) {
            leagaValidarea(campuri[i], erori[i], reguli[i], mesaje[i]);
        }

        // IDNP: doar cifre, maximum 13
        idnp.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (!e.getCharacter().matches("\\d") || idnp.getText().length() >= 13) {
                e.consume();
            }
        });

        Button inregistreaza = new Button("Înregistrează");
        Button ajutor = new Button("?");
        ajutor.setFocusTraversable(false);
        ajutor.setOnAction(e -> stare.setText("Treci între câmpuri cu Tab; erorile apar la ieșirea din câmp"));

        inregistreaza.setOnAction(e -> {
            for (int i = 0; i < campuri.length; i++) {
                if (!valideaza(campuri[i], erori[i], reguli[i], mesaje[i])) {
                    campuri[i].requestFocus();
                    stare.setText("Corectează câmpul marcat cu roșu");
                    return;
                }
            }
            stare.setText("Card înregistrat pentru " + nume.getText());
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));
        String[] etichete = {"Nume:", "IDNP:", "Telefon:", "Email:"};
        for (int i = 0; i < campuri.length; i++) {
            grid.addRow(i, new Label(etichete[i]), campuri[i], erori[i]);
        }
        grid.addRow(4, ajutor, inregistreaza);

        BorderPane root = new BorderPane(grid);
        root.setBottom(stare);
        Scene scene = new Scene(root, 560, 260);

        // Bara de stare urmărește nodul cu focus
        scene.focusOwnerProperty().addListener((obs, vechi, nou) -> {
            if (nou instanceof TextField tf) {
                stare.setText("Completezi: " + tf.getPromptText());
            }
        });

        stage.setTitle("Laborator 13 – Card de cititor");
        stage.setScene(scene);
        stage.show();
        nume.requestFocus();   // după show()
    }

    private void leagaValidarea(TextField tf, Label err, String regula, String mesaj) {
        tf.focusedProperty().addListener((obs, aveaFocus, areFocus) -> {
            if (areFocus) {
                tf.setStyle("-fx-border-color: dodgerblue;");
            } else {
                valideaza(tf, err, regula, mesaj);
            }
        });
    }

    private boolean valideaza(TextField tf, Label err, String regula, String mesaj) {
        boolean ok = tf.getText().trim().matches(regula);
        tf.setStyle(ok ? "-fx-border-color: green;" : "-fx-border-color: red;");
        err.setText(ok ? "" : mesaj);
        return ok;
    }

    private TextField camp(String indiciu) {
        TextField tf = new TextField();
        tf.setPromptText(indiciu);
        return tf;
    }

    private Label eroare() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: #c62828;");
        return l;
    }

    public static void main(String[] args) { launch(); }
}


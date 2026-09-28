package com.example.registration_form;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Controalele
        TextField nameField = new TextField();
        nameField.setPromptText("Numele complet");

        TextField emailField = new TextField();
        emailField.setPromptText("adresa@email.com");

        // Spinner<Integer>(min, max, valoare inițială)
        Spinner<Integer> seatsSpinner = new Spinner<>(1, 10, 1);
        seatsSpinner.setEditable(true);

        Button submitButton = new Button("Trimite înscrierea");
        Label summaryLabel = new Label();

        // 2. GridPane: add(nod, coloana, rândul)
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        form.add(new Label("Nume:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Email:"), 0, 1);
        form.add(emailField, 1, 1);
        form.add(new Label("Nr. locuri:"), 0, 2);
        form.add(seatsSpinner, 1, 2);
        form.add(submitButton, 1, 3);

        // coloana 2 se întinde pe lățimea disponibilă
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(col1, col2);

        // 3. Acțiunea butonului
        submitButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                summaryLabel.setText("Completează numele și emailul!");
                return;
            }

            int seats = seatsSpinner.getValue();
            summaryLabel.setText(
                    "Înscriere confirmată:\n" +
                            "Nume: " + name + "\n" +
                            "Email: " + email + "\n" +
                            "Locuri: " + seats
            );
        });

        // 4. BorderPane
        Label title = new Label("Înscriere eveniment");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(15, 0, 0, 0));

        BorderPane.setMargin(summaryLabel, new Insets(0, 20, 20, 20));

        BorderPane root = new BorderPane();
        root.setTop(title);
        root.setCenter(form);
        root.setBottom(summaryLabel);

        // 5. Scene + Stage
        Scene scene = new Scene(root, 450, 380);
        stage.setTitle("Formular de înscriere");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
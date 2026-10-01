package com.example.calculating_the_book_cost_lab9;

import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
        @Override
        public void start(Stage stage) throws IOException {

                Slider pagini = new Slider(0, 100, 10);
                pagini.setShowTickLabels(true);
                pagini.setShowTickMarks(true);
                pagini.setMajorTickUnit(20);
                pagini.setMinorTickCount(19);
                pagini.setBlockIncrement(1);
                pagini.setSnapToTicks(true);
                CheckBox color = new CheckBox("Tipărire color");
                TextField nume = new TextField("Elev");
                TextField ecou = new TextField();
                Label nr = new Label();
                Label cost = new Label();
                Label mesaj = new Label();
                Button comanda = new Button("Confirmă");
                GridPane root = new GridPane();
                root.setPadding(new Insets(18));
                root.setHgap(12);
                root.setVgap(12);
                root.addRow(0, new Label("Pagini"), pagini);
                root.addRow(1, new Label("Număr selectat"), nr);
                root.addRow(2, new Label("Opțiune"), color);
                root.addRow(3, new Label("Nume"), nume);
                root.addRow(4, new Label("Nume sincronizat"), ecou);
                root.addRow(5, new Label("Total"), cost);
                root.addRow(6, comanda, mesaj);


                cost.getStyleClass().add("label-total");

                // Conectarea slider-ului la numărul de pagini
                IntegerProperty cantitate = new SimpleIntegerProperty();
                DoubleProperty tarif = new SimpleDoubleProperty();
                DoubleProperty total = new SimpleDoubleProperty();

                // A1, A2
                cantitate.bind(Bindings.createIntegerBinding(
                        () -> (int) Math.round(pagini.getValue()),
                        pagini.valueProperty()));
                // A3: alb-negru 0.5 lei/pagină, color 2.0 lei/pagină
                tarif.bind(Bindings.when(color.selectedProperty())
                        .then(2.0).otherwise(0.5));
                // A4
                total.bind(cantitate.multiply(tarif));
                nr.textProperty().bind(cantitate.asString());
                // A5
                cost.textProperty().bind(total.asString("%.2f lei"));

                // B2: validare (butonul e activ doar dacă sunt pagini și numele nu e gol)
                BooleanBinding numeInvalid = Bindings.createBooleanBinding(
                        () -> nume.getText().trim().isEmpty(),
                        nume.textProperty());
                // A6 + B2
                comanda.disableProperty().bind(
                        cantitate.lessThanOrEqualTo(0).or(numeInvalid));

                comanda.setOnAction(e -> {
                        mesaj.setText("Comandă pentru " + nume.getText());
                        System.out.println(total.get()); // A7
                });

                // Experiment separat cu proprietăți numerice:
                DoubleProperty sursa = new SimpleDoubleProperty(10);
                DoubleProperty copie = new SimpleDoubleProperty();
                copie.bind(sursa);
                sursa.set(12);                       // A8
                System.out.println(copie.get());     // 12.0
                copie.unbind();                      // A9
                copie.set(20);
                System.out.println(copie.get());     // A10 -> 20.0

                // B1: sincronizarea numelor (în ambele sensuri)
                ecou.textProperty().bindBidirectional(nume.textProperty());

                // B3: constrângeri GridPane
                GridPane.setHgrow(nume, Priority.ALWAYS);
                nume.setMaxWidth(Double.MAX_VALUE);
                GridPane.setColumnSpan(mesaj, 2);
                GridPane.setRowIndex(mesaj, 7);
                GridPane.setColumnIndex(mesaj, 0);
                GridPane.setMargin(comanda, new Insets(8, 0, 0, 0));

                stage.setScene(new Scene(root, 620, 380));
                stage.setTitle("Calculator pentru tipărire");
                stage.show();
                comanda.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                root.setStyle("-fx-background-color: #f4f4f4;");
                cost.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 16px;");


                cost.getStyleClass().add("label-total");
                cost.styleProperty().bind(
                        Bindings.when(color.selectedProperty())
                                .then("-fx-text-fill: red;")
                                .otherwise("-fx-text-fill: black;"));

        }
}
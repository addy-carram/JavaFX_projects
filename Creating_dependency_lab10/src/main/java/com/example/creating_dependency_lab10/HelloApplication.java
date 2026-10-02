package com.example.creating_dependency_lab10;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        Produs produs = new Produs("Tastatură", 350, 1);
        Cos cos = new Cos(produs);

        TextField tfNume = new TextField();
        TextField tfPret = new TextField();
        Spinner<Integer> spCant = new Spinner<>(0, 99, 1);
        Slider slReducere = new Slider(0, 50, 0);
        Label lReducere = new Label();
        Label lRezumat = new Label(), lTva = new Label(), lTotal = new Label();
        CheckBox cbLivrare = new CheckBox("Livrare gratuită");
        Button bPlaseaza = new Button("Plasează comanda");

        // ---------- Checklist comenzi ----------
        ObservableList<Comanda> comenzi = FXCollections.observableArrayList(
                c -> new Observable[]{ c.selectatProperty() });
        ListView<Comanda> lvComenzi = new ListView<>(comenzi);
        lvComenzi.setCellFactory(CheckBoxListCell.forListView(Comanda::selectatProperty));
        lvComenzi.setPrefHeight(150);
        Label lTotalGeneral = new Label();
        Button bSterge = new Button("Șterge comenzile bifate");

        // ---------- Proprietăți atașate: GridPane ----------
        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(16));
        g.add(new Label("Denumire:"), 0, 0); g.add(tfNume, 1, 0);
        g.add(new Label("Preț (lei):"), 0, 1); g.add(tfPret, 1, 1);
        g.add(new Label("Cantitate:"), 0, 2); g.add(spCant, 1, 2);
        g.add(new Label("Reducere:"), 0, 3);

        HBox hbRed = new HBox(8, slReducere, lReducere);
        HBox.setHgrow(slReducere, Priority.ALWAYS); // proprietate atașată HBox
        g.add(hbRed, 1, 3);
        g.add(lRezumat, 0, 4, 2, 1); // columnSpan = 2
        g.add(lTva, 0, 5, 2, 1);
        g.add(lTotal, 0, 6, 2, 1);
        g.add(cbLivrare, 0, 7, 2, 1);
        g.add(bPlaseaza, 0, 8);
        g.add(new Label("Comenzi plasate:"), 0, 9, 2, 1);
        g.add(lvComenzi, 0, 10, 2, 1);
        g.add(lTotalGeneral, 0, 11, 2, 1);
        g.add(bSterge, 0, 12, 2, 1);
        GridPane.setHalignment(bSterge, HPos.RIGHT);
        GridPane.setColumnSpan(bPlaseaza, 2);
        GridPane.setHalignment(bPlaseaza, HPos.RIGHT);
        GridPane.setHgrow(tfNume, Priority.ALWAYS);
        // TODO 4.1
        GridPane.setHgrow(tfPret, Priority.ALWAYS);

        // ---------- Legături UI ↔ model ----------
        tfNume.textProperty().bindBidirectional(produs.numeProperty());
        Bindings.bindBidirectional(tfPret.textProperty(),
                produs.pretProperty(),
                new NumberStringConverter("#0.00"));
        produs.cantitateProperty().bind(spCant.valueProperty()); // valueProperty e read-only
        slReducere.valueProperty().bindBidirectional(cos.reducereProperty());
        lReducere.textProperty().bind(slReducere.valueProperty().asString("%.0f %%"));
        lRezumat.textProperty().bind(cos.rezumatBinding());
        lTva.textProperty().bind(Bindings.format("TVA (20%%): %.2f lei", cos.tvaBinding()));

        // TODO 4.2
        lTotal.textProperty().bind(
                Bindings.format("Total de plată: %.2f lei", cos.totalDePlataBinding()));

        // TODO 4.3
        cbLivrare.selectedProperty().bind(cos.livrareGratuitaBinding());
        cbLivrare.setDisable(true);

        // TODO 4.4
        bPlaseaza.disableProperty().bind(
                produs.cantitateProperty().isEqualTo(0).or(produs.numeProperty().isEmpty()));

        // 4.6 + adăugare în checklist
        bPlaseaza.setOnAction(e -> {
            String text = cos.rezumatBinding().get();
            comenzi.add(new Comanda(text, cos.totalDePlataBinding().doubleValue()));
            new Alert(Alert.AlertType.INFORMATION, text).showAndWait();
        });

        // total general = suma comenzilor bifate (se actualizează la bifare/debifare)
        lTotalGeneral.textProperty().bind(Bindings.createStringBinding(() -> {
            double suma = 0;
            for (Comanda c : comenzi) if (c.isSelectat()) suma += c.getTotal();
            return String.format("Total comenzi bifate: %.2f lei", suma);
        }, comenzi));

        bSterge.setOnAction(e -> comenzi.removeIf(Comanda::isSelectat));

        // ---------- Ascultători ----------
        cos.totalDePlataBinding().addListener((obs, vechi, nou) ->
                System.out.printf("Total: %.2f -> %.2f%n",
                        vechi.doubleValue(), nou.doubleValue()));

        // TODO 4.5
        produs.pretProperty().addListener(obs -> System.out.println("preț invalidat"));

        stage.setScene(new Scene(g, 480, 640));
        stage.setTitle("Laborator 10 – Crearea dependențelor");
        stage.show();
    }

    public static void main(String[] args) { launch(); }
}
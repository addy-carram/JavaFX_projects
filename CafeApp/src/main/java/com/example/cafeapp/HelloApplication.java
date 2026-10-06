package com.example.cafeapp;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {
    private final ListView<String> jurnal = new ListView<>();
    private final ListView<String> comanda = new ListView<>();
    private final TextField cantitate = new TextField("1");
    private final Label total = new Label("Total: 0 lei");
    private final Button finalizeaza = new Button("Finalizează");
    private final TilePane meniu = new TilePane(8, 8);
    private VBox panouComanda;
    private double suma = 0;
    private final List<Double> valori = new ArrayList<>();
    private boolean procesare = false;

    @Override
    public void start(Stage stage) {
        String[][] produse = {{"Espresso","25"},{"Latte","38"},{"Cappuccino","35"},
                {"Ceai","20"},{"Croissant","28"},{"Prăjitură","45"}};
        for (String[] p : produse) {
            Button b = new Button(p[0] + "\n" + p[1] + " lei");
            b.setUserData(p);                 // numele și prețul, pentru delegare
            b.setPrefSize(110, 60);
            meniu.getChildren().add(b);
        }
        meniu.setPadding(new Insets(10));

        cantitate.setPrefColumnCount(4);
        panouComanda = new VBox(8, new Label("Comanda curentă"), comanda,
                new HBox(8, new Label("Cantitate:"), cantitate), total, finalizeaza);
        panouComanda.setPadding(new Insets(10));
        panouComanda.setPrefWidth(240);

        jurnal.setPrefHeight(130);
        BorderPane root = new BorderPane(meniu, null, panouComanda, jurnal, null);
        Scene scene = new Scene(root, 760, 520);

        // TODO Sarcinile 1–5 se scriu aici
        meniu.setId("meniu"); panouComanda.setId("panouComanda"); cantitate.setId("cantitate");
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, e ->
                log("MOUSE  țintă=" + numeTinta(e)));
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e ->
                log("TASTĂ  " + e.getCode() + "  țintă=" + numeTinta(e)));
        scene.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
                log("button  " + e.getButton() + "  țintă=" + numeTinta(e)));
        //2 sarcina

        meniu.addEventHandler(ActionEvent.ACTION, e -> {
            if (e.getTarget() instanceof Button b && b.getUserData() instanceof String[] p) {
                int q = cantitateCurenta();
                double v = q * Double.parseDouble(p[1]);
                comanda.getItems().add(p[0] + " × " + q + " = " + v + " lei");
                valori.add(v);
                actualizeazaTotal();
            }
        });

        //sarcina3

        cantitate.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            String c = e.getCharacter();
            if (!c.matches("\\d") || cantitate.getText().length() >= 2) {
                e.consume();
            }
        });
        EventHandler<InputEvent> blocare = e -> { if (procesare) e.consume(); };
        meniu.addEventFilter(InputEvent.ANY, blocare);
        panouComanda.addEventFilter(InputEvent.ANY, blocare);
        finalizeaza.setOnAction(e -> {
            procesare = true;
            meniu.setOpacity(0.5);
            panouComanda.setOpacity(0.5);

            PauseTransition pauza = new PauseTransition(Duration.seconds(2));
            pauza.setOnFinished(ev -> {
                procesare = false;
                meniu.setOpacity(1);
                panouComanda.setOpacity(1);
            });
            pauza.play();
        });

        //sarcina 4
        KeyCombination ctrlN = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (ctrlN.match(e)) {
                comandaNoua();
                e.consume();
            } else if (e.getCode() == KeyCode.DELETE) {
                int i = comanda.getSelectionModel().getSelectedIndex();
                if (i >= 0) { comanda.getItems().remove(i); valori.remove(i); actualizeazaTotal(); e.consume(); }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cantitate.setText("1");
                e.consume();
            }
        });

        //sarcina 5

        //sarcina6
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED,  e -> System.out.print("S-f "));
        panouComanda.addEventFilter(MouseEvent.MOUSE_CLICKED,   e -> System.out.print("V-f "));
        meniu.addEventFilter(MouseEvent.MOUSE_CLICKED,   e -> System.out.print("R-f "));
        meniu.addEventHandler(MouseEvent.MOUSE_CLICKED,  e -> System.out.print("R-h "));
        panouComanda.addEventHandler(MouseEvent.MOUSE_CLICKED,  e -> System.out.print("V-h "));
        scene.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> System.out.println("S-h"));



        stage.setTitle("Lab 11 – Cafeneaua");
        stage.setScene(scene);
        stage.show();
    }
    public class ComandaEvent extends Event {
        public static final EventType<ComandaEvent> ANY =
                new EventType<>(Event.ANY, "COMANDA_ANY");
        public static final EventType<ComandaEvent> ADAUGAT =
                new EventType<>(ANY, "COMANDA_ADAUGAT");
        public static final EventType<ComandaEvent> STERS =
                new EventType<>(ANY, "COMANDA_STERS");
        public static final EventType<ComandaEvent> FINALIZAT =
                new EventType<>(ANY, "COMANDA_FINALIZAT");

        private final String produs;
        private final double valoare;

        public ComandaEvent(EventType<ComandaEvent> tip, String produs, double valoare) {
            super(tip);
            this.produs = produs;
            this.valoare = valoare;
        }
        public String getProdus() { return produs; }
        public double getValoare() { return valoare; }
    }
    private int cantitateCurenta() {

        try{
            String text = cantitate.getText().trim();
            if (text.isEmpty()) {
                return 1;
            }
            return Integer.parseInt(text);
        }
        catch (Exception e){
            return 1;
        }

    }
    private void actualizeazaTotal(){
        double s=0;
        for(double v : valori){
            s=s+v;

        }
         total.setText("Total "+s+" lei");

    }
    private void comandaNoua() {
        comanda.getItems().clear();
        valori.clear();
        actualizeazaTotal();
    }
    private void log(String text) {
        jurnal.getItems().add(text);
        jurnal.scrollTo(jurnal.getItems().size() - 1);
    }
    private String numeTinta(Event e) {
        Object t = e.getTarget();
        return (t instanceof Node n && n.getId() != null) ? n.getId() : t.getClass().getSimpleName();
    }

}

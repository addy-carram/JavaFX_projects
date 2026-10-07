package com.example.game_x_and_0;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    private static final int[][] LINII = {
            {0,1,2}, {3,4,5}, {6,7,8},
            {0,3,6}, {1,4,7}, {2,5,8},
            {0,4,8}, {2,4,6}
    };

    private final Button[] celule = new Button[9];
    private final ListView<String> jurnal = new ListView<>();
    private boolean joacaX = true;
    private boolean terminat = false;
    private int mutari = 0;

    @Override
    public void start(Stage stage) {
        // ---------- 1. TABLA ----------
        GridPane grila = new GridPane();
        grila.setHgap(5);
        grila.setVgap(5);
        grila.setPadding(new Insets(10));

        for (int i = 0; i < 9; i++) {
            Button b = new Button("");
            b.setPrefSize(80, 80);
            b.setUserData(i);                       // indexul celulei
            celule[i] = b;
            grila.add(b, i % 3, i / 3);
        }

        jurnal.setPrefHeight(120);
        BorderPane root = new BorderPane(grila);
        root.setBottom(jurnal);
        Scene scene = new Scene(root, 300, 450);

        // ---------- 2. DELEGARE: un singur handler pe grilă ----------
        grila.addEventHandler(ActionEvent.ACTION, e -> {
            if (e.getTarget() instanceof Button b && b.getText().isEmpty()
                    && b.getUserData() instanceof Integer idx) {
                String simbol = joacaX ? "X" : "0";
                b.fireEvent(new MutareEvent(MutareEvent.MUTARE, idx, simbol));
            }
        });

        // ---------- 3. LOGICA JOCULUI (pe grilă) ----------
        grila.addEventHandler(MutareEvent.MUTARE, e -> {
            celule[e.getIndex()].setText(e.getSimbol());
            mutari++;
            if (areCastigator()) {
                grila.fireEvent(new MutareEvent(MutareEvent.CASTIG, e.getIndex(), e.getSimbol()));
            } else if (mutari == 9) {
                grila.fireEvent(new MutareEvent(MutareEvent.REMIZA, e.getIndex(), e.getSimbol()));
            } else {
                joacaX = !joacaX;
            }
        });

        grila.addEventHandler(MutareEvent.CASTIG, e -> {
            terminat = true;
            log("Câștigă " + e.getSimbol() + "! Apasă Ctrl+N pentru joc nou.");
        });

        grila.addEventHandler(MutareEvent.REMIZA, e -> {
            terminat = true;
            log("Remiză! Apasă Ctrl+N pentru joc nou.");
        });

        grila.addEventHandler(MutareEvent.ANY, e ->
                log("EVENIMENT " + e.getEventType()));

        // ---------- 4. FILTRUL DE BLOCARE (pe grilă, NU pe Scene) ----------
        EventHandler<InputEvent> blocare = e -> { if (terminat) e.consume(); };
        grila.addEventFilter(InputEvent.ANY, blocare);

        // ---------- 5. CTRL+N: filtru pe Scene ----------
        KeyCombination ctrlN = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (ctrlN.match(e)) {
                jocNou();
                e.consume();
            }
        });

        // ---------- 6. JURNALIZARE (ca la Cafenea) ----------
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e ->
                log("MOUSE  țintă=" + numeTinta(e)));

        stage.setTitle("X și 0");
        stage.setScene(scene);
        stage.show();
    }

    // ---------- METODE AJUTĂTOARE (la nivel de clasă!) ----------

    private boolean areCastigator() {
        for (int[] l : LINII) {
            String a = celule[l[0]].getText();
            String b = celule[l[1]].getText();
            String c = celule[l[2]].getText();
            if (!a.isEmpty() && a.equals(b) && a.equals(c)) {
                return true;
            }
        }
        return false;
    }

    private void jocNou() {
        for (Button b : celule) {
            b.setText("");
        }
        joacaX = true;
        mutari = 0;
        terminat = false;
        log("--- Joc nou ---");
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
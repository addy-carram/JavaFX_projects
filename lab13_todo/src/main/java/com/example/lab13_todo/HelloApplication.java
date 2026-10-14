package com.example.lab13_todo;

import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.Map;

public class HelloApplication extends Application {
    // Cele 3 grupuri, fiecare cu titlu și culoare proprie (nuanțe de violet)
    private enum Stare {
        NEREALIZAT("Nerealizate", "#a78bfa"),
        IN_PROCES("În proces", "#7c3aed"),
        FINALIZAT("Finalizate", "#4c1d95");

        final String titlu, culoare;
        Stare(String titlu, String culoare) { this.titlu = titlu; this.culoare = culoare; }
    }

    private static class Sarcina {
        final String text;
        final ObjectProperty<Stare> stare = new SimpleObjectProperty<>(Stare.NEREALIZAT);
        Sarcina(String text) { this.text = text; }
    }

    // Paleta aplicației
    private static final String FUNDAL = "#f5f3ff";
    private static final String VIOLET_INCHIS = "#4c1d95";
    private static final String VIOLET = "#7c3aed";
    private static final String VIOLET_DESCHIS = "#c4b5fd";
    private static final String STIL_CAMP = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8 10; -fx-border-color: ";
    private static final String STIL_BTN = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand; -fx-background-color: ";

    // Lista principală: se actualizează și când se schimbă starea unei sarcini
    private final ObservableList<Sarcina> sarcini =
            FXCollections.observableArrayList(s -> new Observable[]{s.stare});
    private final Map<Stare, ListView<Sarcina>> liste = new EnumMap<>(Stare.class);
    private final Map<Stare, Label> antete = new EnumMap<>(Stare.class);
    private final TextField camp = new TextField();
    private final Label stare = new Label();

    @Override
    public void start(Stage stage) {
        camp.setPromptText("Sarcină nouă (minimum 3 caractere)");
        camp.setStyle(STIL_CAMP + VIOLET_DESCHIS + ";");
        Button adauga = new Button("Adaugă");
        adauga.setStyle(STIL_BTN + VIOLET + ";");

        // Validare la pierderea focusului + requestFocus
        leagaValidarea();

        // addEventFilter + consume(): maximum 50 de caractere în câmp
        camp.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            String c = e.getCharacter();
            if (!c.isEmpty() && c.charAt(0) >= 32 && c.charAt(0) != 127
                    && camp.getText().length() >= 50) {
                e.consume();
                stare.setText("Maximum 50 de caractere");
            }
        });

        // Enter adaugă sarcina, Escape golește câmpul
        camp.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                adaugaSarcina();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                camp.clear();
                actualizeaza("Câmp golit");
            }
        });

        adauga.setOnAction(e -> adaugaSarcina());
        // MOUSE_ENTERED / MOUSE_EXITED pe buton
        adauga.setOnMouseEntered(e -> {
            adauga.setStyle(STIL_BTN + VIOLET_INCHIS + ";");
            stare.setText("Adaugă sarcina din câmpul de text (sau apasă Enter)");
        });
        adauga.setOnMouseExited(e -> {
            adauga.setStyle(STIL_BTN + VIOLET + ";");
            actualizeaza("Gata");
        });

        // Cele 3 coloane
        HBox coloane = new HBox(12);
        coloane.setPadding(new Insets(0, 14, 10, 14));
        for (Stare st : Stare.values()) {
            FilteredList<Sarcina> filtrata = new FilteredList<>(sarcini, s -> s.stare.get() == st);
            ListView<Sarcina> lv = new ListView<>(filtrata);
            lv.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 10 10; "
                    + "-fx-border-color: " + st.culoare + "; -fx-border-radius: 0 0 10 10; -fx-border-width: 0 2 2 2;");
            Label gol = new Label("Nicio sarcină");
            gol.setStyle("-fx-text-fill: " + VIOLET_DESCHIS + ";");
            lv.setPlaceholder(gol);
            lv.setCellFactory(l -> creeazaCelula(l));

            // O singură sarcină selectată în tot ecranul
            lv.getSelectionModel().selectedItemProperty().addListener((obs, vechi, nou) -> {
                if (nou != null) {
                    for (ListView<Sarcina> alta : liste.values()) {
                        if (alta != lv) alta.getSelectionModel().clearSelection();
                    }
                }
            });

            Label antet = new Label();
            antet.setMaxWidth(Double.MAX_VALUE);
            antet.setStyle("-fx-background-color: " + st.culoare + "; -fx-text-fill: white; -fx-font-size: 14px; "
                    + "-fx-font-weight: bold; -fx-padding: 10 14; -fx-background-radius: 10 10 0 0;");

            VBox coloana = new VBox(antet, lv);
            VBox.setVgrow(lv, Priority.ALWAYS);
            HBox.setHgrow(coloana, Priority.ALWAYS);
            coloana.setPrefWidth(280);
            coloane.getChildren().add(coloana);

            liste.put(st, lv);
            antete.put(st, antet);
        }

        Label titlu = new Label("Lista mea de sarcini");
        titlu.setStyle("-fx-text-fill: " + VIOLET_INCHIS + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        HBox sus = new HBox(10, camp, adauga);
        HBox.setHgrow(camp, Priority.ALWAYS);
        VBox antetAplicatie = new VBox(10, titlu, sus);
        antetAplicatie.setPadding(new Insets(14));

        stare.setMaxWidth(Double.MAX_VALUE);
        stare.setStyle("-fx-background-color: " + VIOLET_INCHIS + "; -fx-text-fill: white; -fx-padding: 8 14;");

        BorderPane root = new BorderPane(coloane);
        root.setTop(antetAplicatie);
        root.setBottom(stare);
        root.setStyle("-fx-background-color: " + FUNDAL + ";");
        Scene scene = new Scene(root, 920, 520);

        // Combinații de taste globale: Delete, Shift+Delete, Ctrl+D, Ctrl+P, Ctrl+N
        scene.setOnKeyPressed(e -> {
            boolean inCamp = scene.getFocusOwner() == camp;
            if (e.getCode() == KeyCode.DELETE && e.isShiftDown()) {
                curataFinalizate();
            } else if (e.getCode() == KeyCode.DELETE && !inCamp) {
                stergeSarcina(selectata());
            } else if (e.isShortcutDown() && e.getCode() == KeyCode.D) {
                muta(selectata(), Stare.FINALIZAT);
            } else if (e.isShortcutDown() && e.getCode() == KeyCode.P) {
                muta(selectata(), Stare.IN_PROCES);
            } else if (e.isShortcutDown() && e.getCode() == KeyCode.N) {
                camp.requestFocus();
                stare.setText("Sarcină nouă");
            }
        });

        actualizeaza("Scrie o sarcină și apasă Enter");
        stage.setTitle("Laborator 13 – Listă de sarcini (To-Do)");
        stage.setScene(scene);
        stage.show();
        camp.requestFocus();   // după show()
    }

    // Celulă: card alb cu text, dublu-clic, meniu cu clic dreapta
    private ListCell<Sarcina> creeazaCelula(ListView<Sarcina> lv) {
        Text txt = new Text();
        txt.setFont(Font.font("System", FontWeight.NORMAL, 13));

        ListCell<Sarcina> cell = new ListCell<>() {
            @Override
            protected void updateItem(Sarcina s, boolean gol) {
                super.updateItem(s, gol);
                if (gol || s == null) {
                    setGraphic(null);
                } else {
                    boolean gata = s.stare.get() == Stare.FINALIZAT;
                    txt.setText(s.text);
                    txt.setStrikethrough(gata);
                    txt.setFill(gata ? Color.web("#9ca3af") : Color.web(VIOLET_INCHIS));
                    setGraphic(txt);
                }
                stilizeaza(this);
            }
        };
        cell.selectedProperty().addListener((obs, vechi, nou) -> stilizeaza(cell));

        MenuItem mNereal = new MenuItem("Mută în Nerealizate");
        MenuItem mProces = new MenuItem("Mută în În proces");
        MenuItem mFinal = new MenuItem("Mută în Finalizate");
        MenuItem sterge = new MenuItem("Șterge");
        MenuItem curata = new MenuItem("Șterge cele finalizate");
        mNereal.setOnAction(e -> muta(cell.getItem(), Stare.NEREALIZAT));
        mProces.setOnAction(e -> muta(cell.getItem(), Stare.IN_PROCES));
        mFinal.setOnAction(e -> muta(cell.getItem(), Stare.FINALIZAT));
        sterge.setOnAction(e -> stergeSarcina(cell.getItem()));
        curata.setOnAction(e -> curataFinalizate());
        ContextMenu meniu = new ContextMenu(mNereal, mProces, mFinal, new SeparatorMenuItem(), sterge, curata);
        cell.emptyProperty().addListener((obs, vechi, nou) -> cell.setContextMenu(nou ? null : meniu));

        // MOUSE_PRESSED – clic dreapta selectează celula și deschide meniul
        cell.setOnMousePressed(e -> {
            if (!cell.isEmpty() && e.getButton() == MouseButton.SECONDARY) {
                lv.getSelectionModel().select(cell.getIndex());
                stare.setText("Meniu pentru: " + cell.getItem().text);
            }
        });

        // MOUSE_CLICKED – dublu-clic marchează sarcina realizată (sau o redeschide)
        cell.setOnMouseClicked(e -> {
            if (!cell.isEmpty() && e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                Sarcina s = cell.getItem();
                muta(s, s.stare.get() == Stare.FINALIZAT ? Stare.NEREALIZAT : Stare.FINALIZAT);
            }
        });

        // MOUSE_ENTERED / MOUSE_EXITED pe celulă
        cell.setOnMouseEntered(e -> {
            if (!cell.isEmpty()) {
                stare.setText("Dublu-clic = realizat, clic dreapta = meniu");
            }
        });
        cell.setOnMouseExited(e -> actualizeaza("Gata"));
        return cell;
    }

    private void stilizeaza(ListCell<Sarcina> c) {
        if (c.isEmpty() || c.getItem() == null) {
            c.setStyle("-fx-background-color: transparent;");
            return;
        }
        String fundal = c.isSelected() ? "#ddd6fe" : "#faf5ff";
        c.setStyle("-fx-background-color: " + fundal + "; -fx-background-radius: 8; -fx-background-insets: 3 4 3 4; "
                + "-fx-border-color: " + VIOLET_DESCHIS + "; -fx-border-radius: 8; -fx-border-insets: 3 4 3 4; "
                + "-fx-padding: 10 14;");
    }

    private void leagaValidarea() {
        camp.focusedProperty().addListener((obs, aveaFocus, areFocus) -> {
            if (areFocus) {
                camp.setStyle(STIL_CAMP + VIOLET + ";");
            } else {
                String t = camp.getText().trim();
                if (!t.isEmpty() && t.length() < 3) {
                    camp.setStyle(STIL_CAMP + "#dc2626;");
                    stare.setText("Sarcina trebuie să aibă minimum 3 caractere");
                } else {
                    camp.setStyle(STIL_CAMP + VIOLET_DESCHIS + ";");
                }
            }
        });
    }

    private Sarcina selectata() {
        for (ListView<Sarcina> lv : liste.values()) {
            Sarcina s = lv.getSelectionModel().getSelectedItem();
            if (s != null) return s;
        }
        return null;
    }

    private void adaugaSarcina() {
        String t = camp.getText().trim();
        if (t.length() < 3) {
            camp.setStyle(STIL_CAMP + "#dc2626;");
            stare.setText("Sarcina trebuie să aibă minimum 3 caractere");
            camp.requestFocus();
            return;
        }
        sarcini.add(new Sarcina(t));   // sarcina nouă intră în „Nerealizate"
        camp.clear();
        camp.setStyle(STIL_CAMP + "#16a34a;");
        camp.requestFocus();   // după adăugare focusul revine în câmp
        actualizeaza("Adăugat: " + t);
    }

    private void muta(Sarcina s, Stare nou) {
        if (s == null) {
            stare.setText("Selectează mai întâi o sarcină");
            return;
        }
        if (s.stare.get() == nou) {
            stare.setText("Sarcina este deja în „" + nou.titlu + "\"");
            return;
        }
        s.stare.set(nou);
        liste.get(nou).getSelectionModel().select(s);
        actualizeaza("Mutată în „" + nou.titlu + "\": " + s.text);
    }

    private void stergeSarcina(Sarcina s) {
        if (s == null) {
            stare.setText("Selectează mai întâi o sarcină");
            return;
        }
        sarcini.remove(s);
        actualizeaza("Ștearsă: " + s.text);
    }

    private void curataFinalizate() {
        int inainte = sarcini.size();
        sarcini.removeIf(s -> s.stare.get() == Stare.FINALIZAT);
        actualizeaza("Șterse " + (inainte - sarcini.size()) + " sarcini finalizate");
    }

    private void actualizeaza(String mesaj) {
        StringBuilder rezumat = new StringBuilder();
        for (Stare st : Stare.values()) {
            long n = sarcini.stream().filter(s -> s.stare.get() == st).count();
            antete.get(st).setText(st.titlu + "  (" + n + ")");
            rezumat.append("  |  ").append(st.titlu).append(": ").append(n);
        }
        stare.setText(mesaj + "  |  Total: " + sarcini.size() + rezumat);
    }

    public static void main(String[] args) { launch(); }
}
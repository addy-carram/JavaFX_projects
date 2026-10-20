package com.example.clients_row_lab14;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    // ---------- Componente principale ----------
    private final ListView<String> listaCoada = new ListView<>();
    private final Label lblAcum = new Label("—");
    private final Label lblStareGhiseu = new Label("Ghișeu: deschis");
    private final Label lblNrCoada = new Label("În coadă: 0");
    private Stage stage;
    private final Label lblUltima = new Label("Gata");
    private final ObservableList<String> coada = FXCollections.observableArrayList();
    private final BooleanProperty inPauza = new SimpleBooleanProperty(false);
    private int numarator = 0;
    private int serviti = 0;
    private int anulati = 0;

    @Override
    public void start(Stage stage) {
        //inceputul proiectului
        this.stage = stage;
        listaCoada.setItems(coada);


        // ---------- Elemente de meniu ----------
        MenuItem miNou = new MenuItem("Client _nou");
        miNou.setAccelerator(KeyCombination.keyCombination("Shortcut+N"));

        MenuItem miCheama = new MenuItem("_Cheamă următorul");
        miCheama.setAccelerator(KeyCombination.keyCombination("Space"));

        MenuItem miAnuleaza = new MenuItem("_Anulează client");
        miAnuleaza.setAccelerator(KeyCombination.keyCombination("Delete"));

        MenuItem miPauza = new MenuItem("_Pauză ghișeu");
        miPauza.setAccelerator(KeyCombination.keyCombination("P"));

        MenuItem miStatistica = new MenuItem("_Statistică");
        miStatistica.setAccelerator(KeyCombination.keyCombination("F5"));

        MenuItem miReset = new MenuItem("_Resetare");

        Menu mClienti = new Menu("_Clienți", null, miNou, miCheama, miAnuleaza);
        Menu mGhiseu = new Menu("_Ghișeu", null, miPauza, miStatistica,
                new SeparatorMenuItem(), miReset);
        MenuBar meniu = new MenuBar(mClienti, mGhiseu);

        // ---------- Bara de instrumente ----------
        Button btNou = new Button("➕ Client nou");
        btNou.setTooltip(new Tooltip("Client nou (Ctrl+N)"));
        Button btCheama = new Button("📢 Cheamă următorul");
        btCheama.setTooltip(new Tooltip("Cheamă următorul (Space)"));
        Button btAnuleaza = new Button("❌ Anulează");
        btAnuleaza.setTooltip(new Tooltip("Anulează client (Delete)"));
        ToggleButton btPauza = new ToggleButton("⏸ Pauză");
        btPauza.setTooltip(new Tooltip("Pauză ghișeu (P)"));
        Button btStatistica = new Button("📊 Statistică");
        btStatistica.setTooltip(new Tooltip("Statistică (F5)"));

        ToolBar bara = new ToolBar(btNou, btCheama, btAnuleaza,
                new Separator(), btPauza, btStatistica);
        // TODO: setFocusTraversable(false) pe butoane, ca scurtăturile să meargă

        // ---------- Zona centrală: coada (stânga) + "se servește acum" (dreapta) ----------
        Label titluCoada = new Label("Coada de așteptare");
        titluCoada.setStyle("-fx-font-weight: bold;");
        VBox panouCoada = new VBox(8, titluCoada, listaCoada);
        VBox.setVgrow(listaCoada, Priority.ALWAYS);
        panouCoada.setPadding(new Insets(10));

        Label titluAcum = new Label("Se servește acum");
        titluAcum.setStyle("-fx-font-weight: bold;");
        lblAcum.setStyle("-fx-font-size: 36px;");
        VBox panouAcum = new VBox(12, titluAcum, lblAcum, lblStareGhiseu);
        panouAcum.setAlignment(Pos.CENTER);
        panouAcum.setPadding(new Insets(10));
        panouAcum.setPrefWidth(240);

        SplitPane centru = new SplitPane(panouCoada, panouAcum);
        centru.setDividerPositions(0.6);

        // ---------- Meniu contextual pe lista cozii ----------
        MenuItem ctxCheama = new MenuItem("Cheamă următorul");
        MenuItem ctxAnuleaza = new MenuItem("Anulează client");
        listaCoada.setContextMenu(new ContextMenu(ctxCheama, ctxAnuleaza));

        // ---------- Bara de stare ----------
        Label lblUltima = new Label("Gata");
        Region spatiu = new Region();
        HBox.setHgrow(spatiu, Priority.ALWAYS);
        HBox baraStare = new HBox(12, lblUltima, spatiu, lblNrCoada);
        baraStare.setPadding(new Insets(4, 8, 4, 8));
        baraStare.setStyle("-fx-border-color: #ccc transparent transparent transparent;");

        // ---------- Asamblare ----------
        BorderPane root = new BorderPane(centru);
        root.setTop(new VBox(meniu, bara));
        root.setBottom(baraStare);

        btNou.getStyleClass().add("btn-nou");
//        btSalveaza.getStyleClass().add("btn-salveaza");
//        btMajuscule.getStyleClass().add("btn-majuscule");
//        HBox baraStare = new HBox(20, stare, caractere);
        baraStare.getStyleClass().add("bara-stare");

        stage.setTitle("Coadă de clienți la ghișeu");
        Scene scena = new Scene(root, 640, 420);
        scena.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setScene(scena);
        stage.show();

        // TODO: obiecte Command pentru cele 6 comenzi + bindTo(...) pe meniu/bară/contextual
        ClientNouDialog dlg = new ClientNouDialog(stage,
                text -> coada.add(String.format("A%03d  %s", ++numarator, text)));

        Command cmdNou = new Command("Client nou", dlg::arata);
        Command cmdCheama = new Command("Cheamă următorul", this::cheamaUrmatorul);
        Command cmdAnuleaza = new Command("Anulează client", this::anuleazaClient);
        Command cmdPauza = new Command("Pauză ghișeu", this::comutaPauza);
        Command cmdStatistica = new Command("Statistică", this::statistica);
        Command cmdReset = new Command("Resetare", this::resetare);
        // TODO: reguli bind(): "Cheamă" inactivă cu coada goală SAU în pauză (.or)
        cmdCheama.activaProperty().bind(Bindings.isNotEmpty(coada).and(inPauza.not()));
        cmdAnuleaza.activaProperty().bind(
                listaCoada.getSelectionModel().selectedItemProperty().isNotNull());
        // TODO: lblNrCoada legat de dimensiunea listei (Bindings.size / Bindings.concat)
        cmdNou.bindTo(miNou);
        cmdNou.bindTo(btNou);

        cmdCheama.bindTo(miCheama);
        cmdCheama.bindTo(btCheama);
        cmdCheama.bindTo(ctxCheama);

        cmdAnuleaza.bindTo(miAnuleaza);
        cmdAnuleaza.bindTo(btAnuleaza);
        cmdAnuleaza.bindTo(ctxAnuleaza);

        cmdPauza.bindTo(miPauza);
        btPauza.selectedProperty().bindBidirectional(inPauza);

        cmdStatistica.bindTo(miStatistica);
        cmdStatistica.bindTo(btStatistica);

        cmdReset.bindTo(miReset);
//etickete legate
        lblNrCoada.textProperty().bind(Bindings.concat("În coadă: ", Bindings.size(coada)));
        lblStareGhiseu.textProperty().bind(
                Bindings.when(inPauza).then("Ghișeu: în pauză").otherwise("Ghișeu: deschis"));

        //focusul butoanelor
        btNou.setFocusTraversable(false);
        btCheama.setFocusTraversable(false);
        btAnuleaza.setFocusTraversable(false);
        btPauza.setFocusTraversable(false);
        btStatistica.setFocusTraversable(false);
    }
    private void stare(String text) {
        lblUltima.setText(text);
    }

    private void cheamaUrmatorul() {
        String client = coada.remove(0);
        lblAcum.setText(client);
        serviti++;
        stare("Chemat: " + client);
    }

    private void anuleazaClient() {
        int index = listaCoada.getSelectionModel().getSelectedIndex();
        if (index < 0) return;
        String client = coada.remove(index);
        anulati++;
        stare("Anulat: " + client);
    }

    private void comutaPauza() {
        inPauza.set(!inPauza.get());
        stare(inPauza.get() ? "Ghișeu în pauză" : "Ghișeu redeschis");
    }

    private void statistica() {
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Serviți: " + serviti + "\nAnulați: " + anulati + "\nÎn coadă: " + coada.size());
        a.initOwner(stage);
        a.showAndWait();
    }

    private void resetare() {
        coada.clear();
        serviti = 0;
        anulati = 0;
        numarator = 0;
        lblAcum.setText("—");
        stare("Resetat");
    }

    public static void main(String[] args) { launch(); }
}

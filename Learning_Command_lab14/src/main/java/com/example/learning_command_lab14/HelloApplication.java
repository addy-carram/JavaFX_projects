package com.example.learning_command_lab14;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;

public class HelloApplication extends Application {

        private final TextArea editor = new TextArea();
        private final Label stare = new Label("Gata");
        private final Path fisier = Path.of("notite.txt");
        private final BooleanProperty modificat = new SimpleBooleanProperty(false);
        private Stage stage;

        @Override
        public void start(Stage stage) {
            this.stage = stage;

            // ---------- Elementele de meniu ----------
            MenuItem miNou = item("_Nou", "Shortcut+N", this::nou);
            MenuItem miDeschide = item("_Deschide", "Shortcut+O", this::deschide);
            MenuItem miSalveaza = item("_Salvează", "Shortcut+S", this::salveaza);
            MenuItem miIesire = item("_Ieșire", "Alt+F4", Platform::exit);
            MenuItem miMajuscule = item("_Majuscule", "Shortcut+Shift+U", this::majuscule);
            MenuItem miData = item("_Data și ora", "F5", this::dataOra);
            MenuItem miDespre = item("Despre", "F1", this::despre);

            Menu fisierMenu = new Menu("_Fișier", null, miNou, miDeschide, miSalveaza,
                    new SeparatorMenuItem(), miIesire);
            Menu editareMenu = new Menu("_Editare", null, miMajuscule, miData);
            Menu ajutorMenu = new Menu("_Ajutor", null, miDespre);
            MenuBar meniu = new MenuBar(fisierMenu, editareMenu, ajutorMenu);

            // ---------- Bara de instrumente: aceleași metode ----------
            Button btNou = buton("📄", "Nou (Ctrl+N)", this::nou);
            Button btDeschide = buton("📂", "Deschide (Ctrl+O)", this::deschide);
            Button btSalveaza = buton("💾", "Salvează (Ctrl+S)", this::salveaza);
            Button btMajuscule = buton("Aa", "Majuscule (Ctrl+Shift+U)", this::majuscule);

            ToolBar bara = new ToolBar(btNou, btDeschide, btSalveaza,
                    new Separator(), btMajuscule);
            // câmpuri noi


// în start(), după crearea editorului
            editor.textProperty().addListener((obs, vechi, nou) -> modificat.set(true));

            Command cmdNou = new Command("Nou", this::nou);
            Command cmdSalveaza = new Command("Salvează", this::salveaza);
            Command cmdMajuscule = new Command("Majuscule", this::majuscule);
            Command cmdCopiaza = new Command("Copiază selecția", editor::copy);

            // Starea comenzilor legată de date
            cmdSalveaza.activaProperty().bind(modificat);
            cmdNou.activaProperty().bind(editor.textProperty().isNotEmpty());
            cmdMajuscule.activaProperty().bind(editor.textProperty().isNotEmpty());
            cmdCopiaza.activaProperty().bind(editor.selectedTextProperty().isNotEmpty());

// Starea comenzilor legată de date
            cmdSalveaza.bindTo(miSalveaza);
            cmdSalveaza.bindTo(btSalveaza);
            cmdNou.bindTo(miNou);
            cmdNou.bindTo(btNou);
            cmdMajuscule.bindTo(miMajuscule);
            cmdMajuscule.bindTo(btMajuscule);

// "Copiază selecția" doar prin legare
            MenuItem miCopiaza = new MenuItem("_Copiază selecția");
            miCopiaza.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+C"));
            cmdCopiaza.bindTo(miCopiaza);
            editareMenu.getItems().add(miCopiaza);



// Titlul ferestrei: asterisc când există modificări nesalvate
            stage.titleProperty().bind(Bindings.when(modificat)
                    .then("*MiniNotepad – notite.txt")
                    .otherwise("MiniNotepad – notite.txt"));
            CautaDialog cauta = new CautaDialog(stage, editor);
            Command cmdCauta = new Command("Găsește", cauta::arata);
            cmdCauta.activaProperty().bind(editor.textProperty().isNotEmpty());

            MenuItem miCauta = new MenuItem("_Găsește…");
            miCauta.setAccelerator(KeyCombination.keyCombination("Shortcut+F"));
            cmdCauta.bindTo(miCauta);
            editareMenu.getItems().add(miCauta);

// Meniul contextual folosește ACELEAȘI comenzi
            MenuItem ctxMajuscule = new MenuItem("Majuscule");
            MenuItem ctxCauta = new MenuItem("Găsește…");
            MenuItem ctxCopiaza = new MenuItem("Copiază selecția");
            cmdMajuscule.bindTo(ctxMajuscule);
            cmdCauta.bindTo(ctxCauta);
            cmdCopiaza.bindTo(ctxCopiaza);
            editor.setContextMenu(new ContextMenu(ctxCopiaza, ctxMajuscule, new SeparatorMenuItem(), ctxCauta));

// Bara de stare: numărul de caractere
            Label caractere = new Label();
            caractere.textProperty().bind(Bindings.concat("Caractere: ", editor.lengthProperty()));

            BorderPane root = new BorderPane(editor);
            root.setTop(new VBox(meniu, bara));
            root.setBottom(new HBox(20, stare, caractere));

            stage.setScene(new Scene(root, 640, 420));
            stage.show();
        }

        // ---------- Fabrici pentru surse ----------
        private MenuItem item(String text, String scurtatura, Runnable actiune) {
            MenuItem mi = new MenuItem(text);
            mi.setAccelerator(KeyCombination.keyCombination(scurtatura));
            mi.setOnAction(e -> actiune.run());
            return mi;
        }

        private Button buton(String text, String indiciu, Runnable actiune) {
            Button b = new Button(text);
            b.setTooltip(new Tooltip(indiciu));
            b.setOnAction(e -> actiune.run());
            b.setFocusTraversable(false);   // focusul rămâne în editor
            return b;
        }

        // ---------- Comenzile ----------
        private void nou() {
            editor.clear();
            modificat.set(false);
            stare.setText("Document nou");
        }

        private void deschide() {
            try {
                editor.setText(Files.readString(fisier));
                modificat.set(false);
                stare.setText("Deschis: " + fisier.toAbsolutePath());
            } catch (IOException ex) {
                stare.setText("Fișierul nu există încă");
            }
        }

        private void salveaza() {
            try {
                Files.writeString(fisier, editor.getText());
                modificat.set(false);
                stare.setText("Salvat la " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Nu pot salva: " + ex.getMessage()).showAndWait();
            }
        }

        private void majuscule() {
            String sel = editor.getSelectedText();
            if (sel.isEmpty()) {
                editor.setText(editor.getText().toUpperCase());
            } else {
                editor.replaceSelection(sel.toUpperCase());
            }
            stare.setText("Majuscule aplicate");
        }

        private void dataOra() {
            editor.insertText(editor.getCaretPosition(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            stare.setText("Data inserată");
        }

        private void despre() {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "MiniNotepad, laboratorul 14");
            a.initOwner(stage);
            a.showAndWait();
        }



        public static void main(String[] args) { launch(); }
    }


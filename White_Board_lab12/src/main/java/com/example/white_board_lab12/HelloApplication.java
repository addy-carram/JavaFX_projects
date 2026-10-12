package com.example.white_board_lab12;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
        private final Pane plansa = new Pane();
        private final Label stare = new Label("Gata");
        private final ToggleButton btnDreptunghi = new ToggleButton("Dreptunghi");
        private final ToggleButton btnElipsa = new ToggleButton("Elipsă");
        private final ColorPicker culoare = new ColorPicker(Color.STEELBLUE);
        private final Button btnCurata = new Button("Curăță");
        private final TextField textEticheta = new TextField("Notă");
        private final Label eticheta = new Label("Notă");   // sursa pentru drag-and-drop

        private Shape selectata;            // forma selectată (Sarcina 3)
        private boolean modificat = false;  // există modificări nesalvate (Sarcina 6)
        private double x0, y0;
        private Shape curenta;

    private double ancoraX, ancoraY, startTx, startTy;

        @Override
        public void start(Stage stage) {
            ToggleGroup grup = new ToggleGroup();
            btnDreptunghi.setToggleGroup(grup);
            btnElipsa.setToggleGroup(grup);
            btnDreptunghi.setSelected(true);
            textEticheta.setPrefColumnCount(8);
            eticheta.setStyle("-fx-border-color: gray; -fx-padding: 3 8; -fx-background-color: #fff8d6;");

            ToolBar bara = new ToolBar(btnDreptunghi, btnElipsa, new Separator(), culoare, btnCurata,
                    new Separator(), new Label("Text:"), textEticheta, eticheta);

            plansa.setStyle("-fx-background-color: white;");
            Rectangle clip = new Rectangle();                     // formele nu ies din planșă
            clip.widthProperty().bind(plansa.widthProperty());
            clip.heightProperty().bind(plansa.heightProperty());
            plansa.setClip(clip);

            stare.setPadding(new Insets(4, 8, 4, 8));
            BorderPane root = new BorderPane(plansa, bara, null, stare, null);
            Scene scene = new Scene(root, 900, 600);

            // TODO Sarcinile 1–7 se scriu aici
            textEticheta.setOnAction(e -> noteaza("Enter: " + textEticheta.getText()));
            textEticheta.textProperty().addListener((obs, vechi, nou) -> eticheta.setText(nou));

            plansa.setOnMouseMoved(e -> noteaza(String.format("x = %.0f   y = %.0f", e.getX(), e.getY())));

            plansa.setOnMousePressed(e -> {
                plansa.requestFocus();
                if (e.getButton() != MouseButton.PRIMARY) return;
                x0 = e.getX();
                y0 = e.getY();
                curenta = btnElipsa.isSelected() ? new Ellipse(x0, y0, 0, 0) : new Rectangle(x0, y0, 0, 0);
                curenta.setFill(culoare.getValue());
                plansa.getChildren().add(curenta);
            });

            plansa.setOnMouseDragged(e -> {
                if (curenta == null) return;
                double x = Math.min(x0, e.getX()), y = Math.min(y0, e.getY());
                double w = Math.abs(e.getX() - x0), h = Math.abs(e.getY() - y0);
                if (curenta instanceof Rectangle r) {
                    r.setX(x); r.setY(y); r.setWidth(w); r.setHeight(h);
                } else if (curenta instanceof Ellipse el) {
                    el.setCenterX(x + w / 2); el.setCenterY(y + h / 2);
                    el.setRadiusX(w / 2); el.setRadiusY(h / 2);
                }
                noteaza(String.format("%.0f × %.0f", w, h));
            });

            plansa.setOnMouseReleased(e -> {
                if (curenta == null) return;
                Bounds b = curenta.getBoundsInLocal();
                if (b.getWidth() < 5 || b.getHeight() < 5) {
                    plansa.getChildren().remove(curenta);      // un simplu clic nu lasă forme minuscule
                } else {
                    pregatesteForma(curenta);                  // Sarcina 3
                    modificat = true;
                }
                curenta = null;
            });
            plansa.setFocusTraversable(true);
            KeyCombination ctrlD = new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN);

            plansa.setOnKeyPressed(e -> {
                if (selectata == null) return;
                if (ctrlD.match(e)) { duplica(); e.consume(); return; }
                double pas = e.isShiftDown() ? 20 : 5;
                switch (e.getCode()) {
                    case LEFT  -> selectata.setTranslateX(selectata.getTranslateX() - pas);
                    case RIGHT -> selectata.setTranslateX(selectata.getTranslateX() + pas);
                    case UP    -> selectata.setTranslateY(selectata.getTranslateY() - pas);
                    case DOWN  -> selectata.setTranslateY(selectata.getTranslateY() + pas);
                    case DELETE, BACK_SPACE -> { plansa.getChildren().remove(selectata); selectata = null; }
                    default -> { return; }          // alte taste: nu le consumăm
                }
                modificat = true;
                e.consume();
            });

            plansa.setOnKeyTyped(e -> {
                if (selectata == null) return;
                double f = switch (e.getCharacter()) { case "+", "=" -> 1.1; case "-" -> 1 / 1.1; default -> 1; };
                selectata.setScaleX(selectata.getScaleX() * f);
                selectata.setScaleY(selectata.getScaleY() * f);
            });
            plansa.setOnScroll(e -> {
                if (selectata == null || e.getDeltaY() == 0) return;
                if (e.isControlDown()) {
                    selectata.setRotate(selectata.getRotate() + (e.getDeltaY() > 0 ? 15 : -15));
                } else {
                    double f = e.getDeltaY() > 0 ? 1.1 : 1 / 1.1;
                    selectata.setScaleX(selectata.getScaleX() * f);
                    selectata.setScaleY(selectata.getScaleY() * f);
                }
                modificat = true;
                e.consume();
            });

            stage.setOnShown(e -> noteaza("Fereastra afișată: " + (int) stage.getWidth() + " × " + (int) stage.getHeight()));

            stage.setOnCloseRequest(e -> {
                if (!modificat) return;
                Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                        "Desenul are modificări nesalvate. Închideți aplicația?", ButtonType.YES, ButtonType.NO);
                a.setHeaderText(null);
                if (a.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                    e.consume();             // închiderea este anulată
                }
            });

            KeyCombination ctrlS = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
            scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (ctrlS.match(e)) { modificat = false; noteaza("Salvat (simulare)"); e.consume(); }
            });
            eticheta.setOnDragDetected(e -> {
                Dragboard db = eticheta.startDragAndDrop(TransferMode.COPY);
                ClipboardContent c = new ClipboardContent();
                c.putString(eticheta.getText());
                db.setContent(c);
                e.consume();
            });

            plansa.setOnDragOver(e -> {
                if (e.getDragboard().hasString()) {
                    e.acceptTransferModes(TransferMode.COPY);
                }
                e.consume();
            });

            plansa.setOnDragDropped(e -> {
                boolean reusit = false;
                if (e.getDragboard().hasString()) {
                    Text t = new Text(e.getX(), e.getY(), e.getDragboard().getString());
                    t.setFont(Font.font(18));
                    t.setFill(culoare.getValue());
                    plansa.getChildren().add(t);
                    pregatesteForma(t);         // Text este și el Shape: se poate selecta și muta
                    reusit = true;
                    modificat = true;
                }
                e.setDropCompleted(reusit);
                e.consume();
            });




            stage.setTitle("Lab 12 – Tabla de desen");
            stage.setScene(scene);
            stage.show();
        }

        private void noteaza(String text) {
            stare.setText(text);
        }

    private void pregatesteForma(Shape f) {
        f.setOnMouseEntered(e -> f.setOpacity(0.75));
        f.setOnMouseExited(e -> f.setOpacity(1.0));

        f.setOnMousePressed(e -> {
            plansa.requestFocus();
            if (e.getButton() == MouseButton.SECONDARY) {
                plansa.getChildren().remove(f);
                if (f == selectata) selectata = null;
            } else {
                selecteaza(f);
                ancoraX = e.getSceneX();  ancoraY = e.getSceneY();
                startTx = f.getTranslateX(); startTy = f.getTranslateY();
            }
            modificat = true;
            e.consume();               // planșa NU trebuie să înceapă o formă nouă
        });

        f.setOnMouseDragged(e -> {
            f.setTranslateX(startTx + e.getSceneX() - ancoraX);
            f.setTranslateY(startTy + e.getSceneY() - ancoraY);
            e.consume();
        });

        f.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                f.setFill(culoare.getValue());
            }
            e.consume();
        });
    }
    private void selecteaza(Shape f) {
        if (selectata != null) selectata.setStroke(null);   // deselectează vechea formă
        selectata = f;
        f.setStroke(Color.DODGERBLUE);
        f.setStrokeWidth(2);
    }
    private void duplica() {
        if (selectata == null) return;

        Shape copie;
        if (selectata instanceof Rectangle r) {
            copie = new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        } else if (selectata instanceof Ellipse el) {
            copie = new Ellipse(el.getCenterX(), el.getCenterY(), el.getRadiusX(), el.getRadiusY());
        } else if (selectata instanceof Text t) {
            Text nou = new Text(t.getX(), t.getY(), t.getText());
            nou.setFont(t.getFont());
            copie = nou;
        } else {
            return;
        }

        copie.setFill(selectata.getFill());
        copie.setTranslateX(selectata.getTranslateX() + 20);
        copie.setTranslateY(selectata.getTranslateY() + 20);
        copie.setScaleX(selectata.getScaleX());
        copie.setScaleY(selectata.getScaleY());
        copie.setRotate(selectata.getRotate());

        plansa.getChildren().add(copie);
        pregatesteForma(copie);      // să poată fi selectată, mutată, ștearsă
        selecteaza(copie);
        modificat = true;
    }
    }



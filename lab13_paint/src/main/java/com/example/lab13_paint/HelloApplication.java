package com.example.lab13_paint;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
public class HelloApplication extends Application {
    private Color culoare = Color.BLACK;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(600, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(3);
        Label coord = new Label("x: 0, y: 0");

        // Paleta de culori: hover + clic
        HBox paleta = new HBox(8);
        paleta.setPadding(new Insets(8));
        for (Color c : new Color[]{Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE}) {
            Rectangle r = new Rectangle(30, 30, c);
            r.setStroke(Color.GRAY);
            r.setOnMouseEntered(e -> { r.setScaleX(1.2); r.setScaleY(1.2); });
            r.setOnMouseExited(e -> { r.setScaleX(1.0); r.setScaleY(1.0); });
            r.setOnMouseClicked(e -> culoare = c);
            paleta.getChildren().add(r);
        }

        // Începutul liniei
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                gc.setStroke(culoare);
                gc.beginPath();
                gc.moveTo(e.getX(), e.getY());
            }
        });

        // Desen (stânga) sau radieră (dreapta)
        canvas.setOnMouseDragged(e -> {
            if (e.isPrimaryButtonDown()) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            } else if (e.isSecondaryButtonDown()) {
                gc.clearRect(e.getX() - 10, e.getY() - 10, 20, 20);
            }
        });

        // Dublu-clic: golire
        canvas.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }
        });

        // Coordonate și hover pe pânză
        canvas.setOnMouseMoved(e -> coord.setText(String.format("x: %.0f, y: %.0f", e.getX(), e.getY())));
        canvas.setOnMouseEntered(e -> canvas.setCursor(Cursor.CROSSHAIR));
        canvas.setOnMouseExited(e -> coord.setText("în afara pânzei"));

        BorderPane root = new BorderPane(canvas, paleta, null, coord, null);
        stage.setTitle("Laborator 13 – Tabla de desen");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) { launch(); }
}

package com.example.lab13_robot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private static final int CELL = 40, N = 10;
    private int col = 0, row = 0, pasi = 0;
    private final Rectangle robot = new Rectangle(CELL - 4, CELL - 4, Color.DODGERBLUE);
    private final Label stare = new Label();

    @Override
    public void start(Stage stage) {
        Pane grila = new Pane();
        for (int i = 0; i <= N; i++) {
            grila.getChildren().add(new Line(i * CELL, 0, i * CELL, N * CELL));
            grila.getChildren().add(new Line(0, i * CELL, N * CELL, i * CELL));
        }
        grila.getChildren().add(robot);
        grila.setPrefSize(N * CELL, N * CELL);

        BorderPane root = new BorderPane(grila);
        root.setBottom(stare);
        Scene scene = new Scene(root);

        // KEY_PRESSED – taste speciale: săgeți, Space, Esc, Ctrl+R
        scene.setOnKeyPressed(e -> {
            int pas = e.isShiftDown() ? 2 : 1;
            switch (e.getCode()) {
                case UP -> row -= pas;
                case DOWN -> row += pas;
                case LEFT -> col -= pas;
                case RIGHT -> col += pas;
                case SPACE -> robot.setFill(Color.color(Math.random(), Math.random(), Math.random()));
                case ESCAPE -> stage.close();
                default -> { }
            }
            if (e.isShortcutDown() && e.getCode() == KeyCode.R) {
                col = 0; row = 0; pasi = 0;
            } else if (e.getCode().isArrowKey()) {
                pasi++;
                robot.setOpacity(0.5);
            }
            col = Math.max(0, Math.min(N - 1, col));
            row = Math.max(0, Math.min(N - 1, row));
            actualizeaza("Tasta: " + e.getCode());
        });

        // KEY_RELEASED – robotul revine la opacitate normală
        scene.setOnKeyReleased(e -> robot.setOpacity(1.0));

        // KEY_TYPED – cifra tastată = coloana țintă
        scene.setOnKeyTyped(e -> {
            String c = e.getCharacter();
            if (c.matches("[0-9]")) {
                col = Integer.parseInt(c);
                actualizeaza("Salt la coloana " + c);
            }
        });

        actualizeaza("Start");
        stage.setTitle("Laborator 13 – Robotul pe grilă");
        stage.setScene(scene);
        stage.show();
    }

    private void actualizeaza(String mesaj) {
        robot.setX(col * CELL + 2);
        robot.setY(row * CELL + 2);
        stare.setText(mesaj + "  |  Poziție: (" + col + ", " + row + ")  |  Pași: " + pasi);
    }

    public static void main(String[] args) { launch(); }
}


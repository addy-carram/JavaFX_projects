package com.example.lab23_concert_ticket;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class Ticket extends Application {

    private static final double LATIME = 440;   // lățimea biletului
    private static final double INALTIME = 250; // înălțimea biletului
    private static final double BLUR = 16;      // ~25% din raza maximă (63); schimbă după gust

    @Override
    public void start(Stage stage) {
        // ---------- textele biletului ----------
        Label eticheta = new Label("LIVE CONCERT · BILET");
        eticheta.getStyleClass().add("eticheta");

        Label artist = new Label("THE WEEKND");
        artist.getStyleClass().add("artist");

        Label piesa = new Label("What Makes You Beautiful");
        piesa.getStyleClass().add("piesa");

        Label data = new Label("1 DECEMBRIE 2026");
        data.getStyleClass().add("data");

        Label loc = new Label("Chișinău · Arena");   // schimbă locația
        loc.getStyleClass().add("loc");

        Label cod = new Label("TW-0112-2026-0042");
        cod.setId("cod");

        VBox detalii = new VBox(2, data, loc);
        detalii.getStyleClass().add("detalii");

        Region spatiuH = new Region();
        HBox.setHgrow(spatiuH, Priority.ALWAYS);
        HBox jos = new HBox(detalii, spatiuH, cod);
        jos.getStyleClass().add("jos");

        Region spatiuV = new Region();
        VBox.setVgrow(spatiuV, Priority.ALWAYS);
        VBox continut = new VBox(4, eticheta, artist, piesa, spatiuV, jos);
        continut.getStyleClass().add("continut");

        // ---------- biletul (doar text; fundalul violet vine din CSS) ----------
        StackPane bilet = new StackPane(continut);
        bilet.getStyleClass().add("bilet");
        bilet.setPrefSize(LATIME, INALTIME);
        bilet.setMaxSize(LATIME, INALTIME);

        // ---------- panoul de acțiuni (cu foaie de stil proprie) ----------
        Button btnCumpara = new Button("Cumpără biletul");
        btnCumpara.getStyleClass().add("primar");
        Button btnSalveaza = new Button("Salvează");
        HBox actiuni = new HBox(10, btnSalveaza, btnCumpara);
        actiuni.getStyleClass().add("actiuni");
        actiuni.getStylesheets().add(getClass().getResource("dashboard.css").toExternalForm());

        // ---------- setStyle() cu colecția de setteri construită dintr-un Map ----------
        Map<String, String> setteri = new LinkedHashMap<>();
        setteri.put("-fx-font-size", "34px");
        setteri.put("-fx-effect", "dropshadow(gaussian, rgba(168,85,247,0.9), 14, 0.3, 0, 0)");
        artist.setStyle(construiesteStil(setteri));

        // ---------- biletul + acțiunile, centrate ----------
        VBox centru = new VBox(18, bilet, actiuni);
        centru.setAlignment(Pos.CENTER);
        centru.setMaxSize(LATIME, Region.USE_PREF_SIZE);

        // ---------- fundalul pe toată fereastra ----------
        StackPane root = new StackPane();

        URL urlPoza = getClass().getResource("trupa.jpg");
        if (urlPoza != null) {
            ImageView poza = new ImageView(new Image(urlPoza.toExternalForm()));
            poza.setManaged(false);                       // nu influențează dimensiunea ferestrei
            poza.setX(-40);                               // poza iese 40px în afară pe fiecare parte,
            poza.setY(-40);                               // ca marginile blurului să nu se vadă
            poza.fitWidthProperty().bind(root.widthProperty().add(80));
            poza.fitHeightProperty().bind(root.heightProperty().add(80));
            poza.setPreserveRatio(false);
            poza.setEffect(new GaussianBlur(BLUR));       // blurul nu există în CSS JavaFX
            poza.getStyleClass().add("poza");
            root.getChildren().add(poza);
        }
        Region vel = new Region();
        vel.getStyleClass().add("vel");                   // văl violet peste poză
        root.getChildren().addAll(vel, centru);

        // ce iese din fereastră se taie
        Rectangle decupaj = new Rectangle();
        decupaj.widthProperty().bind(root.widthProperty());
        decupaj.heightProperty().bind(root.heightProperty());
        root.setClip(decupaj);

        Scene scene = new Scene(root, 820, 520);
        scene.getStylesheets().add(getClass().getResource("ticket.css").toExternalForm());
        stage.setTitle("Varianta 7 – Bilet la concert");
        stage.setScene(scene);
        stage.show();
    }

    private static String construiesteStil(Map<String, String> setteri) {
        StringBuilder sb = new StringBuilder();
        setteri.forEach((p, v) -> sb.append(p).append(": ").append(v).append("; "));
        return sb.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
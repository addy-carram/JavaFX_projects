package com.example.lab18;


import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {

    // ---------- paleta violet deschis (pentru tot ce nu e partea individuală) ----------
    private static final Color VIOLET_FUNDAL = Color.web("#F5F0FF");   // fundalul filelor
    private static final Color VIOLET_DESCHIS = Color.web("#B794F4");  // culoarea principală
    private static final Color VIOLET_PALID = Color.web("#D8B4FE");    // varianta mai deschisă
    private static final Color VIOLET_MEDIU = Color.web("#9F7AEA");    // linii, text
    private static final String STIL_FUNDAL = "-fx-background-color: #F5F0FF;";

    private Timeline saritura;      // animația principală, controlată în Sarcina 5
    private Timeline deplasare;     // deplasarea orizontală din Sarcina 1

    @Override
    public void start(Stage stage) {
        TabPane file = new TabPane(
                new Tab("1–2, 5. Mingea", filaMinge()),
                new Tab("3. Interpolatoare", filaInterpolatoare()),
                new Tab("4. Semafor și ceas", filaSemaforCeas()),
                new Tab("Varianta 9", creeazaVarianta()));
        file.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        stage.setTitle("Lucrarea de laborator 18 – Nume Prenume, grupa");
        stage.setScene(new Scene(file, 1000, 650));
        stage.show();
    }

    // =====================================================================
    //  SARCINILE 1, 2, 5 – Mingea
    // =====================================================================
    private BorderPane filaMinge() {
        Pane plansa = new Pane();
        plansa.setPrefSize(1000, 540);
        plansa.setStyle(STIL_FUNDAL);

        Line podea = new Line(0, 450, 1000, 450);
        podea.setStroke(VIOLET_MEDIU);
        podea.setStrokeWidth(3);

        Circle minge = new Circle(40, VIOLET_DESCHIS);
        minge.setCenterX(100);
        minge.setCenterY(410);              // stă pe podea: 450 - raza 40

        plansa.getChildren().addAll(podea, minge);

        // ---------- SARCINA 1: deplasare cu două cadre-cheie ----------
        deplasare = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(minge.translateXProperty(), 0)),
                new KeyFrame(Duration.seconds(3),
                        new KeyValue(minge.translateXProperty(), 800)));
        deplasare.setCycleCount(Animation.INDEFINITE);
        deplasare.setAutoReverse(true);
        deplasare.play();

        // ---------- SARCINA 2: săritura ----------
        saritura = new Timeline(
                // 0 ms: impact – mingea turtită pe podea
                new KeyFrame(Duration.ZERO,
                        new KeyValue(minge.translateYProperty(), 10),
                        new KeyValue(minge.scaleXProperty(), 1.25),
                        new KeyValue(minge.scaleYProperty(), 0.75),
                        new KeyValue(minge.fillProperty(), VIOLET_DESCHIS)),
                // 100 ms: și-a revenit la forma rotundă
                new KeyFrame(Duration.millis(100),
                        new KeyValue(minge.translateYProperty(), 0),
                        new KeyValue(minge.scaleXProperty(), 1),
                        new KeyValue(minge.scaleYProperty(), 1)),
                // 600 ms: punctul cel mai înalt; urcarea frânează (EASE_OUT)
                new KeyFrame(Duration.millis(600),
                        new KeyValue(minge.translateYProperty(), -300, Interpolator.EASE_OUT),
                        new KeyValue(minge.fillProperty(), VIOLET_PALID)),
                // 1100 ms: atinge podeaua; căderea accelerează (EASE_IN)
                new KeyFrame(Duration.millis(1100),
                        new KeyValue(minge.translateYProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(minge.scaleXProperty(), 1),
                        new KeyValue(minge.scaleYProperty(), 1),
                        new KeyValue(minge.fillProperty(), VIOLET_DESCHIS)),
                // 1200 ms: din nou turtită – identic cu cadrul de la 0 ms
                new KeyFrame(Duration.millis(1200),
                        new KeyValue(minge.translateYProperty(), 10),
                        new KeyValue(minge.scaleXProperty(), 1.25),
                        new KeyValue(minge.scaleYProperty(), 0.75)));
        saritura.setCycleCount(Animation.INDEFINITE);
        saritura.play();

        BorderPane radacina = new BorderPane(plansa);
        radacina.setStyle(STIL_FUNDAL);

        // ---------- SARCINA 5: panoul de control ----------
        Button bPlay = new Button("▶ play");
        Button bPause = new Button("⏸ pause");
        Button bStop = new Button("⏹ stop");
        Button bSalt = new Button("jumpTo(600 ms)");
        bPlay.setOnAction(e -> saritura.play());
        bPause.setOnAction(e -> saritura.pause());
        bStop.setOnAction(e -> saritura.stop());
        bSalt.setOnAction(e -> saritura.jumpTo(Duration.millis(600)));   // mingea sus

        // viteza de redare: 1 = normal, 2 = dublu, valoare negativă = înapoi
        Slider viteza = new Slider(-2, 3, 1);
        viteza.setShowTickLabels(true);
        viteza.setMajorTickUnit(1);
        saritura.rateProperty().bind(viteza.valueProperty());

        // deplasarea orizontală poate fi oprită separat
        CheckBox cbOrizontal = new CheckBox("deplasare orizontală");
        cbOrizontal.setSelected(true);
        cbOrizontal.selectedProperty().addListener((obs, vechi, nou) -> {
            if (nou) deplasare.play(); else deplasare.pause();
        });

        // starea și timpul curent, legate de proprietățile animației
        Label stare = new Label();
        stare.textProperty().bind(saritura.statusProperty().asString("Stare: %s"));
        Label timp = new Label();
        timp.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("t = %.2f s", saritura.getCurrentTime().toSeconds()),
                saritura.currentTimeProperty()));

        HBox panou = new HBox(12, bPlay, bPause, bStop, bSalt,
                new Label("rate:"), viteza, cbOrizontal, stare, timp);
        panou.setPadding(new Insets(10));
        panou.setAlignment(Pos.CENTER_LEFT);
        radacina.setBottom(panou);

        return radacina;
    }

    // =====================================================================
    //  SARCINA 3 – Cursa interpolatoarelor
    // =====================================================================
    private Pane filaInterpolatoare() {
        Pane plansa = new Pane();
        plansa.setStyle(STIL_FUNDAL);

        Interpolator[] interpolatoare = {
                Interpolator.LINEAR, Interpolator.EASE_IN, Interpolator.EASE_OUT,
                Interpolator.EASE_BOTH, Interpolator.DISCRETE,
                Interpolator.SPLINE(0.7, 0, 0.3, 1)};
        String[] nume = {"LINEAR", "EASE_IN", "EASE_OUT", "EASE_BOTH", "DISCRETE", "SPLINE(0.7, 0, 0.3, 1)"};

        Line start = new Line(200, 30, 200, 470);
        Line sosire = new Line(900, 30, 900, 470);
        start.setStroke(VIOLET_MEDIU);
        sosire.setStroke(VIOLET_MEDIU);
        sosire.getStrokeDashArray().addAll(6.0, 6.0);
        plansa.getChildren().addAll(start, sosire);

        Timeline cursa = new Timeline();
        for (int i = 0; i < interpolatoare.length; i++) {
            double y = 60 + i * 75;
            // nuanțe diferite de violet deschis (tot hue 270, luminozitate/saturație variate)
            Color nuanta = Color.hsb(270, 0.25 + i * 0.07, 1.0 - i * 0.05);
            Circle c = new Circle(200, y, 22, nuanta);
            c.setStroke(VIOLET_MEDIU);
            Label eticheta = new Label(nume[i]);
            eticheta.setLayoutX(20);
            eticheta.setLayoutY(y - 10);
            plansa.getChildren().addAll(c, eticheta);

            // un singur cadru, la 3 s: valoarea de pornire este cea curentă (0)
            cursa.getKeyFrames().add(new KeyFrame(Duration.seconds(3),
                    new KeyValue(c.translateXProperty(), 700, interpolatoare[i])));
        }
        cursa.setDelay(Duration.millis(500));
        cursa.setCycleCount(Animation.INDEFINITE);
        cursa.setAutoReverse(true);
        cursa.play();

        Button restart = new Button("playFromStart()");
        restart.setLayoutX(20);
        restart.setLayoutY(500);
        restart.setOnAction(e -> cursa.playFromStart());
        plansa.getChildren().add(restart);
        return plansa;
    }

    // =====================================================================
    //  SARCINA 4 – Semafor, ceas digital, contor
    // =====================================================================
    private Pane filaSemaforCeas() {
        // ---------- a) Semaforul: acțiuni la momente fixe (3 nuanțe de violet) ----------
        Circle sus = new Circle(40, Color.web("#E9D8FD"));
        Circle mijloc = new Circle(40, VIOLET_PALID);
        Circle jos = new Circle(40, VIOLET_DESCHIS);
        VBox corp = new VBox(15, sus, mijloc, jos);
        corp.setPadding(new Insets(20));
        corp.setStyle("-fx-background-color: #4A3B6B; -fx-background-radius: 20;");
        aprinde(sus, sus, mijloc, jos);              // starea inițială

        // un ciclu = 8 s: sus 3 s, mijloc 1 s, jos 3 s, mijloc 1 s
        Timeline semafor = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> aprinde(mijloc, sus, mijloc, jos)),
                new KeyFrame(Duration.seconds(4), e -> aprinde(jos, sus, mijloc, jos)),
                new KeyFrame(Duration.seconds(7), e -> aprinde(mijloc, sus, mijloc, jos)),
                new KeyFrame(Duration.seconds(8), e -> aprinde(sus, sus, mijloc, jos)));
        semafor.setCycleCount(Animation.INDEFINITE);
        semafor.play();

        // ---------- b) Ceasul digital: un cadru repetat la fiecare secundă ----------
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
        Label ceas = new Label(LocalTime.now().format(format));
        ceas.setFont(Font.font("Consolas", 56));
        ceas.setTextFill(VIOLET_MEDIU);
        Timeline tic = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> ceas.setText(LocalTime.now().format(format))));
        tic.setCycleCount(Animation.INDEFINITE);
        tic.play();

        // ---------- c) Contorul: animarea unei proprietăți care nu se desenează ----------
        IntegerProperty procente = new SimpleIntegerProperty(0);
        Label eticheta = new Label();
        eticheta.setFont(Font.font(20));
        eticheta.textProperty().bind(procente.asString("Încărcare: %d%%"));
        ProgressBar bara = new ProgressBar(0);
        bara.setPrefWidth(300);
        bara.setStyle("-fx-accent: #B794F4;");
        bara.progressProperty().bind(procente.divide(100.0));

        Timeline incarcare = new Timeline(
                new KeyFrame(Duration.seconds(5), new KeyValue(procente, 100, Interpolator.EASE_BOTH)));
        incarcare.setOnFinished(e -> {
            eticheta.textProperty().unbind();
            eticheta.setText("Gata!");
        });
        Button reia = new Button("Reia încărcarea");
        reia.setOnAction(e -> {
            eticheta.textProperty().bind(procente.asString("Încărcare: %d%%"));
            incarcare.playFromStart();
        });
        incarcare.play();

        VBox dreapta = new VBox(20, ceas, eticheta, bara, reia);
        HBox radacina = new HBox(80, corp, dreapta);
        radacina.setPadding(new Insets(40));
        radacina.setAlignment(Pos.CENTER_LEFT);
        radacina.setStyle(STIL_FUNDAL);
        return radacina;
    }

    // Lampa activă are opacitate 1, celelalte sunt „stinse” (0.2)
    private void aprinde(Circle activa, Circle... toate) {
        for (Circle c : toate) c.setOpacity(c == activa ? 1.0 : 0.2);
    }

    // =====================================================================
    //  SARCINA INDIVIDUALĂ – Varianta 9: Valuri (10 nuanțe de albastru)
    //  Un singur Timeline; fiecare val pornește cu 0,3 s după cel dinainte.
    //  Proprietăți animate: radius (EASE_OUT) și opacity (LINEAR).
    // =====================================================================
    private BorderPane creeazaVarianta() {
        Pane plansa = new Pane();
        plansa.setPrefSize(1000, 540);
        plansa.setStyle("-fx-background-color: #0B1B33;");   // fundal închis, ca nuanțele deschise să se vadă

        // 10 nuanțe de albastru, de la cea mai deschisă la cea mai închisă
        Color[] albastre = {
                Color.web("#E3F2FD"), Color.web("#BBDEFB"), Color.web("#90CAF9"),
                Color.web("#64B5F6"), Color.web("#42A5F5"), Color.web("#2196F3"),
                Color.web("#1E88E5"), Color.web("#1976D2"), Color.web("#1565C0"),
                Color.web("#0D47A1")};

        int n = albastre.length;
        double durataVal = 2.0;                       // cât durează expansiunea unui val (s)
        double decalaj = 0.3;                         // întârzierea între valuri (s)
        double ciclu = durataVal + decalaj * (n - 1); // durata unui ciclu = momentul ultimului cadru
        double razaMax = 230;

        List<KeyFrame> cadre = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Circle val = new Circle(500, 255, 0);
            val.setFill(Color.TRANSPARENT);
            val.setStroke(albastre[i]);
            val.setStrokeWidth(6);
            val.setOpacity(0);
            plansa.getChildren().add(val);

            double s = i * decalaj;                   // momentul în care începe valul i

            // cadrul de start al valului
            if (i == 0) {
                cadre.add(new KeyFrame(Duration.ZERO,
                        new KeyValue(val.radiusProperty(), 0),
                        new KeyValue(val.opacityProperty(), 0.95)));
            } else {
                cadre.add(new KeyFrame(Duration.ZERO,
                        new KeyValue(val.radiusProperty(), 0),
                        new KeyValue(val.opacityProperty(), 0)));
                // DISCRETE: opacitatea rămâne 0 până la momentul s, apoi sare la 0.95
                cadre.add(new KeyFrame(Duration.seconds(s),
                        new KeyValue(val.radiusProperty(), 0),
                        new KeyValue(val.opacityProperty(), 0.95, Interpolator.DISCRETE)));
            }
            // cadrul de final: valul se extinde (EASE_OUT) și dispare (LINEAR)
            cadre.add(new KeyFrame(Duration.seconds(s + durataVal),
                    new KeyValue(val.radiusProperty(), razaMax, Interpolator.EASE_OUT),
                    new KeyValue(val.opacityProperty(), 0, Interpolator.LINEAR)));
        }

        Timeline unda = new Timeline();
        unda.getKeyFrames().addAll(cadre);            // ultimul cadru este la `ciclu` secunde
        unda.setCycleCount(Animation.INDEFINITE);
        unda.play();

        BorderPane radacina = new BorderPane(plansa);

        // ---------- butoane play / pause / stop + starea animației ----------
        Button bPlay = new Button("▶ play");
        Button bPause = new Button("⏸ pause");
        Button bStop = new Button("⏹ stop");
        bPlay.setOnAction(e -> unda.play());
        bPause.setOnAction(e -> unda.pause());
        bStop.setOnAction(e -> unda.stop());

        Label stare = new Label();
        stare.textProperty().bind(unda.statusProperty().asString("Stare: %s"));
        Label durata = new Label(String.format("Ciclu: %.1f s", ciclu));

        HBox panou = new HBox(12, bPlay, bPause, bStop, stare, durata);
        panou.setPadding(new Insets(10));
        panou.setAlignment(Pos.CENTER_LEFT);
        radacina.setBottom(panou);
        return radacina;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
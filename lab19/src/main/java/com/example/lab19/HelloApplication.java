package com.example.lab19;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HelloApplication extends Application {

    // ---------- câmpurile aplicației principale (autobuzul) ----------
    private final Pane harta = new Pane();
    private final Rectangle cer = new Rectangle(800, 460, Color.web("#dfe9d8"));
    private Group autobuz;
    private PathTransition traseu;

    private static final String[] STATII = {"Gara", "Piața", "Parc", "Universitate", "Spital", "Terminus"};
    private static final double[][] POZITII = {
            {60, 400}, {260, 400}, {380, 280}, {700, 140}, {700, 300}, {520, 330}
    };

    private Circle rosu, galben, verde;
    private Timeline semafor;
    private Timeline ziNoapte;

    private final Label info = new Label("t = 0,00 s");
    private final Slider cronologie = new Slider(0, 12.5, 0);

    @Override
    public void start(Stage stage) {
        TabPane file = new TabPane(
                new Tab("Traseul autobuzului", creeazaAplicatiaPrincipala()),
                new Tab("Varianta 2 – Avion", creeazaVarianta()));
        file.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        stage.setScene(new Scene(file));
        stage.setTitle("Laborator 19 – Traseul autobuzului – Nume Prenume, grupa");
        stage.show();
    }

    // =====================================================================
    //  SARCINILE 1–4 – Traseul autobuzului
    // =====================================================================
    private BorderPane creeazaAplicatiaPrincipala() {
        harta.setPrefSize(800, 460);

        Path drum = new Path(elementeDrum());
        drum.setStroke(Color.web("#6b6b6b"));
        drum.setStrokeWidth(30);
        drum.setStrokeLineCap(StrokeLineCap.ROUND);
        drum.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Path marcajAx = new Path(elementeDrum());      // linia albă din mijloc
        marcajAx.setStroke(Color.WHITE);
        marcajAx.getStrokeDashArray().addAll(14.0, 12.0);

        autobuz = creeazaAutobuz();
        autobuz.setTranslateX(POZITII[0][0]);          // poziția de pornire (Gara), până pornește animația
        autobuz.setTranslateY(POZITII[0][1]);

        // Sarcina 2.4: ordinea contează – autobuzul se adaugă ultimul, deasupra stațiilor
        harta.getChildren().addAll(cer, drum, marcajAx);
        adaugaStatii();
        harta.getChildren().add(creeazaSemafor(222, 292));
        harta.getChildren().add(autobuz);

        semafor = creeazaSemaforTimeline();
        semafor.play();
        ziNoapte = creeazaZiNoapte();
        ziNoapte.play();
        System.out.println("Marcajele semaforului: " + semafor.getCuePoints());

        // Sarcina 1: autobuzul pe cale
        traseu = new PathTransition(Duration.seconds(12.5), drum, autobuz);
        traseu.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        traseu.setInterpolator(Interpolator.LINEAR);   // viteză constantă

        // Sarcina 3: marcaje + bara de derulare
        adaugaMarcaje();
        legaCronologia();
        // Sarcina 4 (avansat): opriri automate în stații
        opririInStatii();

        BorderPane root = new BorderPane(harta);
        root.setBottom(new VBox(creeazaPanou(), creeazaButoaneStatii()));
        return root;
    }

    /** Elementele căii; le creăm de două ori, pentru drum și pentru linia din mijloc. */
    private PathElement[] elementeDrum() {
        return new PathElement[]{
                new MoveTo(60, 400),                                   // Gara
                new LineTo(260, 400),                                  // Piața
                new QuadCurveTo(380, 400, 380, 280),                   // Parc
                new CubicCurveTo(380, 120, 560, 60, 700, 140),         // Universitate
                new ArcTo(80, 80, 0, 700, 300, false, true),           // Spital
                new LineTo(520, 330)                                   // Terminus
        };
    }

    private Group creeazaAutobuz() {
        Rectangle corp = new Rectangle(-32, -14, 64, 28);      // centrat în (0, 0)
        corp.setArcWidth(8);
        corp.setArcHeight(8);
        corp.setFill(Color.web("#f2b705"));
        corp.setStroke(Color.web("#7a5c00"));
        Rectangle parbriz = new Rectangle(22, -11, 7, 22);     // fața autobuzului: spre +X
        parbriz.setFill(Color.web("#9fd3ff"));
        Group g = new Group(corp, parbriz);
        for (int i = 0; i < 3; i++) {
            Rectangle geam = new Rectangle(-26 + i * 15, -11, 11, 22);
            geam.setFill(Color.web("#e8f4ff"));
            g.getChildren().add(geam);
        }
        return g;
    }

    private HBox creeazaPanou() {
        Button play = new Button("▶ Pornește");
        Button pauza = new Button("⏸ Pauză");
        Button stop = new Button("⏹ Stop");
        play.setOnAction(e -> traseu.play());
        pauza.setOnAction(e -> traseu.pause());
        stop.setOnAction(e -> traseu.stop());
        HBox panou = new HBox(8, play, pauza, stop, cronologie, info);
        panou.setPadding(new Insets(10));
        panou.setAlignment(Pos.CENTER_LEFT);
        return panou;
    }

    // ---------- Sarcina 2.1: stațiile care pulsează ----------
    private void adaugaStatii() {
        for (int i = 0; i < STATII.length; i++) {
            double x = POZITII[i][0], y = POZITII[i][1];
            Circle punct = new Circle(x, y, 9, Color.WHITE);
            punct.setStroke(Color.web("#1c7ed6"));
            punct.setStrokeWidth(4);

            Label eticheta = new Label(STATII[i]);
            eticheta.setStyle("-fx-background-color: white; -fx-padding: 1 5; -fx-background-radius: 4;");
            eticheta.relocate(x + 14, y - 34);

            Timeline puls = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(punct.radiusProperty(), 9.0)),
                    new KeyFrame(Duration.seconds(0.8),
                            new KeyValue(punct.radiusProperty(), 13.0, Interpolator.EASE_BOTH)));   // 13.0, nu 13
            puls.setCycleCount(Animation.INDEFINITE);
            puls.setAutoReverse(true);
            puls.play();

            harta.getChildren().addAll(punct, eticheta);
        }
    }

    // ---------- Sarcina 2.2: semaforul ----------
    private Group creeazaSemafor(double x, double y) {
        Rectangle cutie = new Rectangle(0, 0, 30, 84);
        cutie.setArcWidth(10);
        cutie.setArcHeight(10);
        cutie.setFill(Color.web("#2b2b2b"));
        rosu = new Circle(15, 15, 10, Color.RED);
        galben = new Circle(15, 42, 10, Color.GOLD);
        verde = new Circle(15, 69, 10, Color.LIMEGREEN);
        Group g = new Group(cutie, rosu, galben, verde);
        g.relocate(x, y);
        return g;
    }

    private void aprinde(Circle lampa) {
        for (Circle c : new Circle[]{rosu, galben, verde}) {
            c.setOpacity(c == lampa ? 1.0 : 0.2);
        }
    }

    private Timeline creeazaSemaforTimeline() {
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, "rosu", e -> aprinde(rosu)),
                new KeyFrame(Duration.seconds(4), "galben", e -> aprinde(galben)),
                new KeyFrame(Duration.seconds(5), "verde", e -> aprinde(verde)),
                new KeyFrame(Duration.seconds(9), "galben2", e -> aprinde(galben)),
                new KeyFrame(Duration.seconds(10)));          // sfârșitul ciclului
        t.setCycleCount(Animation.INDEFINITE);
        return t;
    }

    // ---------- Sarcina 2.3: ziua și noaptea ----------
    private Timeline creeazaZiNoapte() {
        Color zi = Color.web("#dfe9d8"), apus = Color.web("#f4c9a0"), noapte = Color.web("#2d3a4f");
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(cer.fillProperty(), zi)),
                new KeyFrame(Duration.seconds(8), new KeyValue(cer.fillProperty(), zi)),
                new KeyFrame(Duration.seconds(11), new KeyValue(cer.fillProperty(), apus, Interpolator.EASE_IN)),
                new KeyFrame(Duration.seconds(14), new KeyValue(cer.fillProperty(), noapte, Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(20), new KeyValue(cer.fillProperty(), noapte)),
                new KeyFrame(Duration.seconds(24), new KeyValue(cer.fillProperty(), zi, Interpolator.EASE_BOTH)));
        t.setCycleCount(Animation.INDEFINITE);
        return t;
    }

    // ---------- Sarcina 3.2: marcajele ----------
    private void adaugaMarcaje() {
        Map<String, Duration> m = traseu.getCuePoints();
        m.put("Gara", Duration.ZERO);
        m.put("Piața", Duration.seconds(2.0));
        m.put("Parc", Duration.seconds(3.9));
        m.put("Universitate", Duration.seconds(8.2));
        m.put("Spital", Duration.seconds(10.7));
        m.put("Terminus", traseu.getDuration());
    }

    // ---------- Sarcina 3.3: butoanele generate din marcaje ----------
    private FlowPane creeazaButoaneStatii() {
        FlowPane p = new FlowPane(6, 6);
        p.setPadding(new Insets(0, 10, 10, 10));
        traseu.getCuePoints().entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(m -> {
                    Button b = new Button("⇢ " + m.getKey());
                    b.setOnAction(e -> traseu.playFrom(m.getKey()));
                    p.getChildren().add(b);
                });

        Button verdeAcum = new Button("🚦 Verde acum");
        verdeAcum.setOnAction(e -> {
            semafor.jumpTo("verde");      // marcaj creat din numele cadrului-cheie
            aprinde(verde);               // starea corectă imediat, chiar și pe pauză
        });
        p.getChildren().add(verdeAcum);
        return p;
    }

    // ---------- Sarcina 3.4: bara de derulare și timpul curent ----------
    private void legaCronologia() {
        cronologie.setPrefWidth(380);
        traseu.currentTimeProperty().addListener((obs, vechi, nou) -> {
            if (!cronologie.isValueChanging()) {
                cronologie.setValue(nou.toSeconds());
            }
            info.setText(String.format("t = %.2f s · după %s", nou.toSeconds(), ultimaStatie(nou)));
        });
        cronologie.valueProperty().addListener((obs, vechi, nou) -> {
            if (cronologie.isValueChanging()) {
                if (traseu.getStatus() == Animation.Status.STOPPED) {
                    traseu.play();        // animația trebuie pornită o dată,
                    traseu.pause();       // apoi o ținem pe pauză în timpul deplasării
                }
                traseu.jumpTo(Duration.seconds(nou.doubleValue()));
            }
        });
        traseu.setOnFinished(e -> System.out.println("Autobuzul a ajuns la Terminus"));
    }

    /** Marcajul cu cel mai mare moment care nu depășește t. */
    private String ultimaStatie(Duration t) {
        String nume = "";
        Duration cel = Duration.seconds(-1);
        for (Map.Entry<String, Duration> m : traseu.getCuePoints().entrySet()) {
            Duration v = m.getValue();
            if (!v.greaterThan(t) && v.greaterThan(cel)) {
                cel = v;
                nume = m.getKey();
            }
        }
        return nume;
    }

    // ---------- Sarcina 4 (avansat): opriri automate în stații ----------
    private void opririInStatii() {
        traseu.currentTimeProperty().addListener((obs, vechi, nou) -> {
            if (traseu.getStatus() != Animation.Status.RUNNING) return;
            if (nou.subtract(vechi).toSeconds() > 0.5) return;     // salt cu buton sau bară, nu mers
            for (Map.Entry<String, Duration> m : traseu.getCuePoints().entrySet()) {
                Duration t = m.getValue();
                boolean trecut = vechi.lessThan(t) && !nou.lessThan(t);
                if (trecut && t.greaterThan(Duration.ZERO) && t.lessThan(traseu.getDuration())) {
                    String statie = m.getKey();
                    Platform.runLater(() -> opresteLa(statie));    // nu schimbăm animația în mijlocul cadrului
                    return;
                }
            }
        });
    }

    private void opresteLa(String statie) {
        traseu.pause();
        traseu.jumpTo(statie);                                     // exact în dreptul stației
        boolean rosuLaSemafor = statie.equals("Piața") && rosu.getOpacity() == 1.0;
        info.setText("Oprire: " + statie + (rosuLaSemafor ? " (roșu la semafor)" : ""));
        PauseTransition asteapta = new PauseTransition(Duration.seconds(rosuLaSemafor ? 3 : 1.5));
        asteapta.setOnFinished(e -> traseu.play());
        asteapta.play();
    }

    // =====================================================================
    //  VARIANTA INDIVIDUALĂ 2 – Avion
    //  Calea: MoveTo + două CubicCurveTo; norii: Timeline cu 3 cadre-cheie,
    //  fiecare nor cu altă viteză; marcaje: Decolare, Croazieră, Aterizare.
    // =====================================================================
    private BorderPane creeazaVarianta() {
        Pane scena = new Pane();
        scena.setPrefSize(800, 460);

        Rectangle cerAvion = new Rectangle(800, 460, Color.web("#bfe3ff"));
        Rectangle sol = new Rectangle(0, 400, 800, 60);
        sol.setFill(Color.web("#7bb661"));
        Rectangle pista = new Rectangle(20, 394, 760, 10);
        pista.setFill(Color.web("#555555"));

        // ---------- calea: MoveTo + două CubicCurveTo ----------
        Path cale = new Path(
                new MoveTo(60, 380),                                // decolare
                new CubicCurveTo(260, 380, 300, 120, 450, 120),     // urcare spre altitudinea de croazieră
                new CubicCurveTo(600, 120, 640, 380, 740, 380));    // coborâre și aterizare
        cale.setStroke(Color.WHITE);
        cale.setStrokeWidth(2);
        cale.getStrokeDashArray().addAll(8.0, 8.0);
        cale.setFill(Color.TRANSPARENT);

        scena.getChildren().addAll(cerAvion, sol, pista);

        // ---------- norii: câte un Timeline cu 3 cadre, viteze diferite ----------
        Group nor1 = creeazaNor(850, 60, 1.0);
        Group nor2 = creeazaNor(900, 190, 1.4);
        Group nor3 = creeazaNor(1000, 270, 0.8);
        scena.getChildren().addAll(nor1, nor2, nor3, cale);

        List<Timeline> nori = new ArrayList<>();
        nori.add(animaNor(nor1, 10));      // rapid
        nori.add(animaNor(nor2, 18));      // mediu
        nori.add(animaNor(nor3, 28));      // lent
        nori.forEach(Timeline::play);

        // ---------- avionul pe cale ----------
        Group avion = creeazaAvion();
        avion.setTranslateX(60);
        avion.setTranslateY(380);
        scena.getChildren().add(avion);

        PathTransition zbor = new PathTransition(Duration.seconds(9), cale, avion);
        zbor.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        zbor.setInterpolator(Interpolator.LINEAR);

        // ---------- marcaje ----------
        zbor.getCuePoints().put("Decolare", Duration.ZERO);
        zbor.getCuePoints().put("Croazieră", Duration.seconds(3.8));
        zbor.getCuePoints().put("Aterizare", Duration.seconds(6.8));

        // ---------- panou: play / pause / stop, stare, timp, butoane de salt ----------
        Button bPlay = new Button("▶ Pornește");
        Button bPauza = new Button("⏸ Pauză");
        Button bStop = new Button("⏹ Stop");
        bPlay.setOnAction(e -> zbor.play());
        bPauza.setOnAction(e -> zbor.pause());
        bStop.setOnAction(e -> zbor.stop());

        Label stare = new Label();
        stare.textProperty().bind(zbor.statusProperty().asString("Stare: %s"));
        Label timp = new Label();
        timp.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("t = %.2f s", zbor.getCurrentTime().toSeconds()),
                zbor.currentTimeProperty()));

        HBox rand1 = new HBox(8, bPlay, bPauza, bStop, stare, timp);
        rand1.setPadding(new Insets(10));
        rand1.setAlignment(Pos.CENTER_LEFT);

        FlowPane rand2 = new FlowPane(6, 6);
        rand2.setPadding(new Insets(0, 10, 10, 10));
        zbor.getCuePoints().entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(m -> {
                    Button b = new Button("⇢ " + m.getKey());
                    b.setOnAction(e -> zbor.playFrom(m.getKey()));
                    rand2.getChildren().add(b);
                });

        BorderPane radacina = new BorderPane(scena);
        radacina.setBottom(new VBox(rand1, rand2));
        return radacina;
    }

    /** Avion văzut din lateral, centrat în (0, 0), cu fața spre +X. */
    private Group creeazaAvion() {
        Ellipse corp = new Ellipse(0, 0, 30, 8);
        corp.setFill(Color.WHITE);
        corp.setStroke(Color.web("#6b6b6b"));
        Circle cabina = new Circle(16, -2, 4, Color.web("#4aa3e8"));
        Polygon aripa = new Polygon(-8, 2, 6, 2, -12, 18, -22, 18);
        aripa.setFill(Color.web("#9aa5b1"));
        Polygon coada = new Polygon(-30, 0, -20, 0, -28, -15, -34, -15);
        coada.setFill(Color.CRIMSON);
        return new Group(aripa, corp, coada, cabina);
    }

    private Group creeazaNor(double x, double y, double scara) {
        Circle a = new Circle(0, 0, 22, Color.WHITE);
        Circle b = new Circle(26, -10, 28, Color.WHITE);
        Circle c = new Circle(54, 0, 22, Color.WHITE);
        Group g = new Group(a, b, c);
        g.setOpacity(0.9);
        g.setScaleX(scara);
        g.setScaleY(scara);
        g.setLayoutX(x);
        g.setLayoutY(y);
        return g;
    }

    /** Timeline cu 3 cadre-cheie: norul traversează scena de la dreapta la stânga. */
    private Timeline animaNor(Group nor, double durata) {
        double d = nor.getLayoutX() + 200;       // distanța până dincolo de marginea din stânga
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(nor.translateXProperty(), 0),
                        new KeyValue(nor.translateYProperty(), 0)),
                new KeyFrame(Duration.seconds(durata / 2),
                        new KeyValue(nor.translateXProperty(), -d / 2),
                        new KeyValue(nor.translateYProperty(), 8, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(durata),
                        new KeyValue(nor.translateXProperty(), -d),
                        new KeyValue(nor.translateYProperty(), 0, Interpolator.EASE_BOTH)));
        t.setCycleCount(Animation.INDEFINITE);
        return t;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

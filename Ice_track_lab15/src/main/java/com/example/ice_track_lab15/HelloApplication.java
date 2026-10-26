package com.example.ice_track_lab15;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HelloApplication  extends Application {

    private final Pane plansa = new Pane();
    private final Label stare = new Label("Stare: STOPPED");
    private PathTransition drumSchior;
    private RotateTransition cadere;
    private Group corpSchior;               // copilul care se rotește la cădere
    private boolean cazut = false;

    @Override
    public void start(Stage stage) {
        plansa.setPrefSize(800, 460);
        plansa.setStyle("-fx-background-color: linear-gradient(to bottom, #8ecbff, #eaf6ff);");

        deseneazaMunte();                                   // Sarcina 2
        Path traseu = adaugaPartia();                       // Sarcina 3
        Group schior = creeazaSchior();                     // Sarcina 4
        Group brad = creeazaBradMobil();                    // Sarcina 5
        plansa.getChildren().addAll(schior, brad, creeazaPoarta());
        pornesteSchiorul(traseu, schior);                   // Sarcina 4
        pornesteBradul(brad);                               // Sarcina 5
        pornestePaza(schior, brad);                         // Sarcina 5

        BorderPane root = new BorderPane(plansa);
        root.setBottom(creeazaComenzi());
        stage.setScene(new Scene(root, 800, 520));
        stage.setTitle("Lucrarea nr. 15 – Pârtie de schi – Nume Prenume, grupa");
        stage.show();
    }

    // ---------- Sarcina 1: panoul de comenzi ----------
    private HBox creeazaComenzi() {
        Button start = new Button("Start");
        Button pauza = new Button("Pauză");
        Button stop  = new Button("Stop");
        start.setOnAction(e -> drumSchior.play());
        pauza.setOnAction(e -> drumSchior.pause());
        stop.setOnAction(e -> {
            cazut = false;
            cadere.stop();
            corpSchior.setRotate(0);
            drumSchior.stop();
        });

        Slider viteza = new Slider(0.25, 3, 1);
        viteza.valueProperty().addListener((obs, vechi, nou) -> drumSchior.setRate(nou.doubleValue()));

        HBox comenzi = new HBox(10, start, pauza, stop, new Label("Viteza:"), viteza, stare);
        comenzi.setPadding(new Insets(12));
        return comenzi;
    }

    // ---------- Sarcina 2: scena (7 clase de figuri) ----------
    private void deseneazaMunte() {
        Polygon munte = new Polygon(300, 330, 520, 60, 760, 330);
        munte.setFill(Color.web("#8a9bb5"));

        Polygon zapada = new Polygon(0, 460, 0, 95, 800, 335, 800, 460);
        zapada.setFill(Color.web("#f4f9ff"));

        Circle soare = new Circle(730, 60, 38);
        soare.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.YELLOW), new Stop(1, Color.ORANGE)));

        Arc halou = new Arc(730, 60, 52, 52, 200, 140);
        halou.setType(ArcType.OPEN);
        halou.setFill(null);
        halou.setStroke(Color.GOLD);
        halou.setStrokeWidth(3);

        Ellipse nor = new Ellipse(360, 70, 70, 22);
        nor.setFill(Color.WHITE);

        Line cablu = new Line(0, 20, 260, 70);
        cablu.setStroke(Color.DIMGRAY);
        cablu.setStrokeWidth(3);

        Text titlu = new Text(20, 35, "Pârtia 6 – slalom");
        titlu.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titlu.setFill(Color.web("#1b2231"));

        plansa.getChildren().addAll(munte, zapada, soare, halou, nor, cablu,
                creeazaBrad(90, 330), creeazaBrad(170, 400), creeazaBrad(260, 420), titlu);
    }

    private Group creeazaBrad(double x, double yBaza) {
        Polygon coroana = new Polygon(x - 25, yBaza, x, yBaza - 60, x + 25, yBaza);
        coroana.setFill(Color.FORESTGREEN);
        Rectangle trunchi = new Rectangle(x - 4, yBaza, 8, 12);
        trunchi.setFill(Color.SADDLEBROWN);
        return new Group(trunchi, coroana);
    }

    // ---------- Sarcina 3: traseul zigzag + Shape.subtract ----------
    private Path creeazaTraseu() {
        Path p = new Path(
                new MoveTo(60, 100),
                new LineTo(200, 190),
                new LineTo(280, 150),
                new LineTo(420, 260),
                new LineTo(500, 220),
                new QuadCurveTo(600, 330, 660, 340),
                new HLineTo(740));
        p.setFill(null);
        return p;
    }

    private Path adaugaPartia() {
        Path partie = creeazaTraseu();
        partie.setStroke(Color.web("#bcdcf5"));
        partie.setStrokeWidth(28);
        partie.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Path marcaj = creeazaTraseu();
        marcaj.setStroke(Color.ROYALBLUE);
        marcaj.setStrokeWidth(2);
        marcaj.getStrokeDashArray().addAll(12.0, 10.0);

        plansa.getChildren().addAll(partie, marcaj);
        return partie;
    }

    private Shape creeazaPoarta() {
        Rectangle bloc = new Rectangle(690, 290, 70, 60);
        Ellipse deschidere = new Ellipse(725, 350, 28, 50);
        Shape poarta = Shape.subtract(bloc, deschidere);
        poarta.setFill(Color.SLATEGRAY);
        poarta.setStroke(Color.DARKSLATEGRAY);
        poarta.setStrokeWidth(2);
        return poarta;
    }

    // ---------- Sarcina 4: schiorul pe traseu ----------
    private Group creeazaSchior() {
        Rectangle schiuri = new Rectangle(0, 20, 34, 4);
        schiuri.setFill(Color.CRIMSON);
        Circle cap = new Circle(18, 4, 4, Color.SIENNA);
        Line corp = new Line(18, 8, 18, 18);
        corp.setStroke(Color.DARKSLATEBLUE);
        corp.setStrokeWidth(4);
        Line bat = new Line(20, 10, 32, 22);
        bat.setStroke(Color.DIMGRAY);
        bat.setStrokeWidth(2);

        corpSchior = new Group(schiuri, cap, corp, bat);
        return new Group(corpSchior);
    }

    private void pornesteSchiorul(Path traseu, Group schior) {
        drumSchior = new PathTransition(Duration.seconds(10), traseu, schior);
        drumSchior.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        drumSchior.setInterpolator(Interpolator.LINEAR);
        drumSchior.setCycleCount(Animation.INDEFINITE);
        stare.textProperty().bind(drumSchior.statusProperty().asString("Stare: %s"));
    }

    // ---------- Sarcina 5: bradul mobil + coliziunea ----------
    private Group creeazaBradMobil() {
        return creeazaBrad(350, 330);
    }

    private void pornesteBradul(Group brad) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(3), brad);
        tt.setFromY(0);
        tt.setToY(-170);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.play();
    }

    private void pornestePaza(Group schior, Group brad) {
        cadere = new RotateTransition(Duration.millis(400), corpSchior);
        cadere.setToAngle(90);

        AnimationTimer paza = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean atinge = schior.getBoundsInParent().intersects(brad.getBoundsInParent());
                if (atinge && drumSchior.getStatus() == Animation.Status.RUNNING) {
                    drumSchior.pause();
                    cazut = true;
                    cadere.playFromStart();
                } else if (!atinge && cazut) {
                    cazut = false;
                    cadere.stop();
                    corpSchior.setRotate(0);
                    drumSchior.play();
                }
            }
        };
        paza.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

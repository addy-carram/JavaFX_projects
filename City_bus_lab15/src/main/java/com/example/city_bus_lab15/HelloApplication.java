package com.example.city_bus_lab15;

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

public class HelloApplication extends Application {

    private final Pane plansa = new Pane();
    private final Label stare = new Label("Stare: STOPPED");
    private PathTransition drumAutobuz;
    private boolean opritLaColiziune = false;

    @Override
    public void start(Stage stage) {
        plansa.setPrefSize(800, 460);
        plansa.setStyle("-fx-background-color: linear-gradient(to bottom, #9fd8ff, #e6f6ff);");

        deseneazaOrasul();                                  // Sarcina 2
        Path traseu = adaugaDrumul();                       // Sarcina 3
        Group autobuz = creeazaAutobuz();                   // Sarcina 4
        Rectangle masina = creeazaMasina();                 // Sarcina 5
        plansa.getChildren().addAll(autobuz, masina, creeazaTunel());
        pornesteAutobuzul(traseu, autobuz);                 // Sarcina 4
        pornesteMasina(masina);                             // Sarcina 5
        pornestePaza(autobuz, masina);                      // Sarcina 5

        BorderPane root = new BorderPane(plansa);
        root.setBottom(creeazaComenzi());
        stage.setScene(new Scene(root, 800, 520));
        stage.setTitle("Lucrarea nr. 15 – Autobuzul orașului – Nume Prenume, grupa");
        stage.show();
    }

    // ---------- Sarcina 1: panoul de comenzi ----------
    private HBox creeazaComenzi() {
        Button start = new Button("Start");
        Button pauza = new Button("Pauză");
        Button stop  = new Button("Stop");
        start.setOnAction(e -> drumAutobuz.play());
        pauza.setOnAction(e -> drumAutobuz.pause());
        stop.setOnAction(e -> { opritLaColiziune = false; drumAutobuz.stop(); });

        Slider viteza = new Slider(0.25, 3, 1);
        viteza.valueProperty().addListener((obs, vechi, nou) -> drumAutobuz.setRate(nou.doubleValue()));

        HBox comenzi = new HBox(10, start, pauza, stop, new Label("Viteza:"), viteza, stare);
        comenzi.setPadding(new Insets(12));
        return comenzi;
    }

    // ---------- Sarcina 2: scena din figuri ----------
    private void deseneazaOrasul() {
        Rectangle iarba = new Rectangle(0, 120, 800, 340);
        iarba.setFill(Color.web("#9ed98a"));

        Circle soare = new Circle(730, 60, 38);
        soare.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.YELLOW), new Stop(1, Color.ORANGE)));

        Ellipse nor = new Ellipse(360, 70, 70, 22);
        nor.setFill(Color.WHITE);

        Rectangle casa = new Rectangle(440, 200, 120, 100);
        casa.setFill(Color.BEIGE);
        casa.setStroke(Color.SADDLEBROWN);
        casa.setStrokeWidth(2);
        Polygon acoperis = new Polygon(430, 200, 500, 150, 570, 200);
        acoperis.setFill(Color.FIREBRICK);
        Rectangle usa = new Rectangle(485, 255, 30, 45);
        usa.setArcWidth(8);
        usa.setArcHeight(8);
        usa.setFill(Color.SIENNA);

        Rectangle trunchi = new Rectangle(242, 320, 16, 40);
        trunchi.setFill(Color.SADDLEBROWN);
        Circle coroana = new Circle(250, 310, 28, Color.FORESTGREEN);

        Arc fantana = new Arc(330, 370, 40, 22, 0, 180);
        fantana.setType(ArcType.ROUND);
        fantana.setFill(Color.LIGHTSKYBLUE);
        fantana.setStroke(Color.STEELBLUE);

        Line felinar = new Line(700, 330, 700, 370);
        felinar.setStroke(Color.DIMGRAY);
        felinar.setStrokeWidth(4);

        Text titlu = new Text(20, 35, "Orașul nostru – traseul 15");
        titlu.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titlu.setFill(Color.web("#1b2231"));

        plansa.getChildren().addAll(iarba, soare, nor, casa, acoperis, usa,
                trunchi, coroana, fantana, felinar, titlu);
    }

    // ---------- Sarcina 3: traseul Path + Shape.subtract ----------
    private Path creeazaTraseu() {
        Path p = new Path(
                new MoveTo(100, 400),
                new HLineTo(560),
                new QuadCurveTo(640, 400, 640, 320),
                new VLineTo(160),
                new CubicCurveTo(640, 80, 460, 60, 380, 160),
                new LineTo(100, 300),
                new ArcTo(50, 50, 0, 100, 400, false, false),
                new ClosePath());
        p.setFill(null);
        return p;
    }

    private Path adaugaDrumul() {
        Path asfalt = creeazaTraseu();
        asfalt.setStroke(Color.DIMGRAY);
        asfalt.setStrokeWidth(28);
        asfalt.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Path marcaj = creeazaTraseu();
        marcaj.setStroke(Color.WHITE);
        marcaj.setStrokeWidth(2);
        marcaj.getStrokeDashArray().addAll(12.0, 10.0);

        Circle statia1 = new Circle(300, 400, 8, Color.WHITE);
        Circle statia2 = new Circle(640, 240, 8, Color.WHITE);
        statia1.setStroke(Color.ROYALBLUE);
        statia2.setStroke(Color.ROYALBLUE);

        plansa.getChildren().addAll(asfalt, marcaj, statia1, statia2);
        return asfalt;
    }

    private Shape creeazaTunel() {
        Rectangle bloc = new Rectangle(140, 370, 90, 60);
        Ellipse deschidere = new Ellipse(185, 430, 34, 30);
        Shape tunel = Shape.subtract(bloc, deschidere);
        tunel.setFill(Color.SLATEGRAY);
        tunel.setStroke(Color.DARKSLATEGRAY);
        tunel.setStrokeWidth(2);
        return tunel;
    }

    // ---------- Sarcina 4: autobuzul pe traseu ----------
    private Group creeazaAutobuz() {
        Rectangle caroserie = new Rectangle(0, 0, 60, 26);
        caroserie.setArcWidth(10);
        caroserie.setArcHeight(10);
        caroserie.setFill(Color.GOLD);
        caroserie.setStroke(Color.DARKGOLDENROD);

        Rectangle parbriz = new Rectangle(48, 4, 8, 18);   // în față = spre dreapta
        parbriz.setFill(Color.LIGHTCYAN);

        Text numar = new Text(16, 18, "15");
        numar.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        return new Group(caroserie, parbriz, numar);
    }

    private void pornesteAutobuzul(Path traseu, Group autobuz) {
        drumAutobuz = new PathTransition(Duration.seconds(12), traseu, autobuz);
        drumAutobuz.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        drumAutobuz.setInterpolator(Interpolator.LINEAR);
        drumAutobuz.setCycleCount(Animation.INDEFINITE);
        stare.textProperty().bind(drumAutobuz.statusProperty().asString("Stare: %s"));
    }

    // ---------- Sarcina 5: mașina + coliziunea ----------
    private Rectangle creeazaMasina() {
        Rectangle masina = new Rectangle(390, 330, 20, 34);
        masina.setArcWidth(6);
        masina.setArcHeight(6);
        masina.setFill(Color.ROYALBLUE);
        return masina;
    }

    private void pornesteMasina(Rectangle masina) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(3), masina);
        tt.setFromY(0);
        tt.setToY(90);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.play();
    }

    private void pornestePaza(Group autobuz, Rectangle masina) {
        AnimationTimer paza = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean atinge = autobuz.getBoundsInParent().intersects(masina.getBoundsInParent());
                if (atinge && drumAutobuz.getStatus() == Animation.Status.RUNNING) {
                    drumAutobuz.pause();
                    opritLaColiziune = true;
                    masina.setFill(Color.RED);
                } else if (!atinge && opritLaColiziune) {
                    opritLaColiziune = false;
                    masina.setFill(Color.ROYALBLUE);
                    drumAutobuz.play();
                }
            }
        };
        paza.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

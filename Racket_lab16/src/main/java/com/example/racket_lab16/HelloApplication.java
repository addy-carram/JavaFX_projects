package com.example.racket_lab16;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Shear;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HelloApplication extends Application {

    private static final double SX = 620, SY = 360;    // Soarele (centrul orbitei)
    private static final double TX = 140, TY = 250;    // turela orbitală
    private final Pane plansa = new Pane();
    private final Label scor = new Label("Lovituri: 0");
    private final List<Circle> proiectile = new ArrayList<>();
    private int lovituri = 0;
    private Rotate unghiTurela;
    private Group asteroid;
    private Polygon corpAsteroid;

    @Override
    public void start(Stage stage) {
        plansa.setPrefSize(800, 500);
        plansa.setStyle("-fx-background-color: #050b1a;");
        plansa.setClip(new Rectangle(800, 500));
        deseneazaGrila();
        transformariStatice();    // Sarcina 2
        creeazaPlaneta();         // Sarcina 3
        creeazaAsteroidul();      // Sarcina 5
        creeazaTurela();          // Sarcina 4
        pornesteBucla();          // Sarcina 4

        HBox bara = new HBox(20, scor, new Label("Mișcați mouse-ul pentru a ochi, click pentru a trage"));
        bara.setPadding(new Insets(10));
        BorderPane root = new BorderPane(plansa);
        root.setBottom(bara);
        stage.setScene(new Scene(root, 800, 545));
        stage.setTitle("Lucrarea nr. 16 – Stația orbitală – Nume Prenume, grupa");
        stage.show();
    }

    // ---------- Sarcina 1: grila ----------
    private void deseneazaGrila() {
        for (int x = 0; x <= 800; x += 50) {
            Line l = new Line(x, 0, x, 500);
            l.setStroke(Color.web("#16294a"));
            plansa.getChildren().add(l);
        }
        for (int y = 0; y <= 500; y += 50) {
            Line l = new Line(0, y, 800, y);
            l.setStroke(Color.web("#16294a"));
            plansa.getChildren().add(l);
        }
    }

    // ---------- Sarcina 2: racheta ----------
    private Polygon creeazaRacheta(Color culoare) {
        Polygon p = new Polygon(0, 0, 34, 0, 54, 12, 34, 24, 0, 24, 8, 12);
        p.setFill(culoare);
        return p;
    }

    private void eticheta(double x, double y, String text) {
        Text t = new Text(x, y, text);
        t.setFill(Color.LIGHTGRAY);
        plansa.getChildren().add(t);
    }

    private void transformariStatice() {
        Polygon original = creeazaRacheta(Color.LIGHTGRAY);

        Polygon mutata = creeazaRacheta(Color.DEEPSKYBLUE);
        mutata.getTransforms().add(new Translate(30, 430));

        Polygon rotita = creeazaRacheta(Color.GOLD);
        rotita.getTransforms().addAll(new Translate(130, 430),
                new Rotate(-30, 27, 12));                       // pivot = centrul rachetei

        Polygon marita = creeazaRacheta(Color.LIMEGREEN);
        marita.getTransforms().addAll(new Translate(210, 425),
                new Scale(1.5, 1.5, 0, 12));                    // pivot = coada

        Polygon umbra = creeazaRacheta(Color.color(1, 1, 1, 0.3));
        umbra.getTransforms().addAll(new Translate(300, 446),
                new Scale(1, 0.5, 0, 24), new Shear(-1, 0, 0, 24));
        Polygon racheta = creeazaRacheta(Color.ORANGERED);
        racheta.getTransforms().add(new Translate(300, 430));

        Polygon ordine1 = creeazaRacheta(Color.VIOLET);         // T, apoi R
        ordine1.getTransforms().addAll(new Translate(380, 452), new Rotate(-15));
        Polygon ordine2 = creeazaRacheta(Color.VIOLET);         // R, apoi T
        ordine2.getTransforms().addAll(new Rotate(-15), new Translate(380, 452));

        Polygon oglindita = creeazaRacheta(Color.TEAL);         // BONUS: scalare negativă
        oglindita.getTransforms().addAll(new Translate(560, 440), new Scale(-1, 1, 27, 12));

        plansa.getChildren().addAll(original, mutata, rotita, marita, umbra, racheta,
                ordine1, ordine2, oglindita);
        eticheta(50, 18, "originalul la (0, 0)");
        eticheta(20, 490, "Translate");
        eticheta(120, 490, "Rotate");
        eticheta(200, 490, "Scale");
        eticheta(290, 490, "Shear");
        eticheta(370, 490, "T, apoi R");
        eticheta(470, 320, "R, apoi T");
        eticheta(535, 490, "Scale(-1, 1)");
    }

    // ---------- Sarcina 3: planeta care orbitează Soarele ----------
    private void creeazaPlaneta() {
        Circle soare = new Circle(SX, SY, 36);
        soare.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.YELLOW), new Stop(1, Color.ORANGE)));

        Circle orbita = new Circle(SX, SY, 120);
        orbita.setFill(null);
        orbita.setStroke(Color.web("#8899aa", 0.6));
        orbita.getStrokeDashArray().addAll(6.0, 6.0);

        Circle planeta = new Circle(SX + 120, SY, 12, Color.DODGERBLUE);
        Circle luna = new Circle(SX + 120 + 20, SY, 4, Color.LIGHTGRAY);

        Rotate rotLuna = new Rotate(0, SX + 120, SY);            // pivot = planeta
        luna.getTransforms().add(rotLuna);
        Timeline timpLuna = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rotLuna.angleProperty(), 0)),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(rotLuna.angleProperty(), 360, Interpolator.LINEAR)));
        timpLuna.setCycleCount(Animation.INDEFINITE);
        timpLuna.play();

        Group sistem = new Group(planeta, luna);
        Rotate orbitare = new Rotate(0, SX, SY);                 // pivot = Soarele
        sistem.getTransforms().add(orbitare);
        Timeline timpPlaneta = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(orbitare.angleProperty(), 0)),
                new KeyFrame(Duration.seconds(8),
                        new KeyValue(orbitare.angleProperty(), 360, Interpolator.LINEAR)));
        timpPlaneta.setCycleCount(Animation.INDEFINITE);
        timpPlaneta.play();

        plansa.getChildren().addAll(orbita, soare, sistem);
    }

    // ---------- Sarcina 4: turela orbitală ----------
    private void creeazaTurela() {
        Rectangle teava = new Rectangle(TX, TY - 5, 44, 10);
        teava.setFill(Color.SILVER);
        Circle baza = new Circle(TX, TY, 14, Color.DARKGRAY);
        unghiTurela = new Rotate(0, TX, TY);
        teava.getTransforms().add(unghiTurela);
        plansa.getChildren().addAll(teava, baza);

        plansa.setOnMouseMoved(e -> {
            double dx = e.getX() - TX, dy = e.getY() - TY;
            unghiTurela.setAngle(Math.toDegrees(Math.atan2(dy, dx)));
        });
        plansa.setOnMouseClicked(e -> trage(teava));
    }

    private void trage(Rectangle teava) {
        Point2D varf = teava.localToParent(TX + 44, TY);
        double a = Math.toRadians(unghiTurela.getAngle());
        Circle glont = new Circle(varf.getX(), varf.getY(), 4, Color.ORANGE);
        glont.setUserData(new double[]{Math.cos(a) * 400, Math.sin(a) * 400});
        proiectile.add(glont);
        plansa.getChildren().add(glont);
    }

    private void pornesteBucla() {
        new AnimationTimer() {
            private long ultim = 0;

            @Override
            public void handle(long now) {
                if (ultim == 0) { ultim = now; return; }
                double dt = (now - ultim) / 1e9;
                ultim = now;
                Iterator<Circle> it = proiectile.iterator();
                while (it.hasNext()) {
                    Circle g = it.next();
                    double[] v = (double[]) g.getUserData();
                    g.setCenterX(g.getCenterX() + v[0] * dt);
                    g.setCenterY(g.getCenterY() + v[1] * dt);
                    boolean lovit = esteLovit(g);
                    boolean iesit = g.getCenterX() < 0 || g.getCenterX() > 800
                            || g.getCenterY() < 0 || g.getCenterY() > 500;
                    if (lovit || iesit) {
                        plansa.getChildren().remove(g);
                        it.remove();
                    }
                }
            }
        }.start();
    }

    // ---------- Sarcina 5: asteroidul pe traiectorie curbă ----------
    private void creeazaAsteroidul() {
        corpAsteroid = new Polygon(0, 6, 12, 0, 26, 4, 30, 16, 20, 28, 6, 26, -2, 14);
        corpAsteroid.setFill(Color.SADDLEBROWN);
        Rectangle coada = new Rectangle(-22, 12, 20, 4);
        coada.setFill(Color.color(1, 0.27, 0, 0.6));

        RotateTransition rotire = new RotateTransition(Duration.seconds(2), corpAsteroid);
        rotire.setByAngle(360);
        rotire.setInterpolator(Interpolator.LINEAR);
        rotire.setCycleCount(Animation.INDEFINITE);
        rotire.play();

        asteroid = new Group(corpAsteroid, coada);

        Path traseu = new Path(
                new MoveTo(60, 60),
                new CubicCurveTo(250, 0, 300, 300, 450, 200),
                new QuadCurveTo(560, 120, 740, 180));
        traseu.setStroke(Color.web("#5f7d95"));
        traseu.getStrokeDashArray().addAll(6.0, 6.0);
        traseu.setFill(null);

        PathTransition drum = new PathTransition(Duration.seconds(9), traseu, asteroid);
        drum.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        drum.setInterpolator(Interpolator.LINEAR);
        drum.setCycleCount(Animation.INDEFINITE);
        drum.play();

        plansa.getChildren().addAll(traseu, asteroid);
    }

    private boolean esteLovit(Circle glont) {
        if (!asteroid.getBoundsInParent().intersects(glont.getBoundsInParent())) {
            return false;
        }
        Point2D inScena = glont.localToScene(glont.getCenterX(), glont.getCenterY());
        Point2D inCorp = corpAsteroid.sceneToLocal(inScena);
        if (!corpAsteroid.contains(inCorp)) {
            return false;
        }
        lovituri++;
        scor.setText("Lovituri: " + lovituri);
        ScaleTransition puls = new ScaleTransition(Duration.millis(150), corpAsteroid);
        puls.setFromX(1.4);
        puls.setFromY(1.4);
        puls.setToX(1);
        puls.setToY(1);
        puls.play();
        return true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

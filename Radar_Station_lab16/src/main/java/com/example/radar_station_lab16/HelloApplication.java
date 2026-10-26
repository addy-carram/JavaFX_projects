package com.example.radar_station_lab16;

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
import javafx.scene.paint.Color;
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

    private static final double CX = 640, CY = 380;    // centrul radarului și al tunului
    private final Pane plansa = new Pane();
    private final Label scor = new Label("Lovituri: 0");
    private final List<Circle> proiectile = new ArrayList<>();
    private int lovituri = 0;
    private Rotate unghiTun;
    private Group nava;
    private Polygon corpNava;

    @Override
    public void start(Stage stage) {
        plansa.setPrefSize(800, 500);
        plansa.setStyle("-fx-background-color: #0b1d2e;");
        plansa.setClip(new Rectangle(800, 500));
        deseneazaGrila();
        transformariStatice();    // Sarcina 2
        creeazaRadarul();         // Sarcina 3
        creeazaNava();            // Sarcina 5
        creeazaTunul();           // Sarcina 4
        pornesteBucla();          // Sarcina 4

        HBox bara = new HBox(20, scor, new Label("Mișcați mouse-ul pentru a ochi, click pentru a trage"));
        bara.setPadding(new Insets(10));
        BorderPane root = new BorderPane(plansa);
        root.setBottom(bara);
        stage.setScene(new Scene(root, 800, 545));
        stage.setTitle("Lucrarea nr. 16 – Stația radar – Nume Prenume, grupa");
        stage.show();
    }

    // ---------- Sarcina 1: grila ----------
    private void deseneazaGrila() {
        for (int x = 0; x <= 800; x += 50) {
            Line l = new Line(x, 0, x, 500);
            l.setStroke(Color.web("#1d3a52"));
            plansa.getChildren().add(l);
        }
        for (int y = 0; y <= 500; y += 50) {
            Line l = new Line(0, y, 800, y);
            l.setStroke(Color.web("#1d3a52"));
            plansa.getChildren().add(l);
        }
    }

    // ---------- Sarcina 2: transformări statice ----------
    private Polygon creeazaSageata(Color culoare) {
        Polygon p = new Polygon(0, 0, 40, 12, 0, 24, 10, 12);
        p.setFill(culoare);
        return p;
    }

    private void eticheta(double x, double y, String text) {
        Text t = new Text(x, y, text);
        t.setFill(Color.LIGHTGRAY);
        plansa.getChildren().add(t);
    }

    private void transformariStatice() {
        Polygon original = creeazaSageata(Color.LIGHTGRAY);

        Polygon mutata = creeazaSageata(Color.DEEPSKYBLUE);
        mutata.getTransforms().add(new Translate(30, 440));

        Polygon rotita = creeazaSageata(Color.GOLD);
        rotita.getTransforms().addAll(new Translate(110, 440),
                new Rotate(-30, 20, 12));

        Polygon marita = creeazaSageata(Color.LIMEGREEN);
        marita.getTransforms().addAll(new Translate(180, 436),
                new Scale(1.5, 1.5, 0, 12));

        Polygon umbra = creeazaSageata(Color.color(0, 0, 0, 0.6));
        umbra.getTransforms().addAll(new Translate(270, 446),
                new Scale(1, 0.5, 0, 24), new Shear(-1, 0, 0, 24));
        Polygon avion = creeazaSageata(Color.ORANGERED);
        avion.getTransforms().add(new Translate(270, 430));

        Polygon ordine1 = creeazaSageata(Color.VIOLET);
        ordine1.getTransforms().addAll(new Translate(360, 452), new Rotate(-15));
        Polygon ordine2 = creeazaSageata(Color.VIOLET);
        ordine2.getTransforms().addAll(new Rotate(-15), new Translate(360, 452));

        plansa.getChildren().addAll(original, mutata, rotita, marita, umbra, avion, ordine1, ordine2);
        eticheta(50, 18, "originalul la (0, 0)");
        eticheta(20, 490, "Translate");
        eticheta(100, 490, "Rotate");
        eticheta(180, 490, "Scale");
        eticheta(260, 490, "Shear");
        eticheta(340, 490, "T, apoi R");
        eticheta(440, 360, "R, apoi T");
    }

    // ---------- Sarcina 3: radarul ----------
    private void creeazaRadarul() {
        Circle disc = new Circle(CX, CY, 110, Color.web("#0f3d2e", 0.7));
        disc.setStroke(Color.LIMEGREEN);
        Circle inel = new Circle(CX, CY, 55);
        inel.setFill(null);
        inel.setStroke(Color.web("#32cd32", 0.5));

        Arc fascicul = new Arc(CX, CY, 110, 110, 0, 30);
        fascicul.setType(ArcType.ROUND);
        fascicul.setFill(Color.color(0.2, 1, 0.3, 0.35));

        Rotate scanare = new Rotate(0, CX, CY);
        fascicul.getTransforms().add(scanare);
        Timeline ciclu = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(scanare.angleProperty(), 0)),
                new KeyFrame(Duration.seconds(3),
                        new KeyValue(scanare.angleProperty(), 360, Interpolator.LINEAR)));
        ciclu.setCycleCount(Animation.INDEFINITE);
        ciclu.play();

        plansa.getChildren().addAll(disc, inel, fascicul);
    }

    // ---------- Sarcina 4: tunul ----------
    private void creeazaTunul() {
        Rectangle teava = new Rectangle(CX, CY - 6, 48, 12);
        teava.setFill(Color.SILVER);
        Circle baza = new Circle(CX, CY, 16, Color.DARKGRAY);
        unghiTun = new Rotate(0, CX, CY);
        teava.getTransforms().add(unghiTun);
        plansa.getChildren().addAll(teava, baza);

        plansa.setOnMouseMoved(e -> {
            double dx = e.getX() - CX, dy = e.getY() - CY;
            unghiTun.setAngle(Math.toDegrees(Math.atan2(dy, dx)));
        });
        plansa.setOnMouseClicked(e -> trage(teava));
    }

    private void trage(Rectangle teava) {
        Point2D varf = teava.localToParent(CX + 48, CY);
        double a = Math.toRadians(unghiTun.getAngle());
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
                    boolean lovit = esteLovita(g);
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

    // ---------- Sarcina 5: nava pe cale + lovirea ----------
    private void creeazaNava() {
        corpNava = new Polygon(0, 0, 50, 15, 0, 30, 10, 15);
        corpNava.setFill(Color.CRIMSON);
        Rectangle elice = new Rectangle(-8, 5, 4, 20);
        elice.setFill(Color.LIGHTGRAY);
        RotateTransition rotElice = new RotateTransition(Duration.seconds(0.3), elice);
        rotElice.setByAngle(360);
        rotElice.setInterpolator(Interpolator.LINEAR);
        rotElice.setCycleCount(Animation.INDEFINITE);
        rotElice.play();
        nava = new Group(corpNava, elice);

        Path traseu = new Path(
                new MoveTo(100, 140),
                new CubicCurveTo(200, 40, 360, 240, 480, 120),
                new QuadCurveTo(560, 40, 680, 100),
                new ArcTo(60, 60, 0, 680, 220, false, true),
                new HLineTo(160),
                new QuadCurveTo(60, 220, 100, 140),
                new ClosePath());
        traseu.setStroke(Color.web("#5f7d95"));
        traseu.getStrokeDashArray().addAll(6.0, 6.0);
        traseu.setFill(null);

        PathTransition patrulare = new PathTransition(Duration.seconds(10), traseu, nava);
        patrulare.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        patrulare.setInterpolator(Interpolator.LINEAR);
        patrulare.setCycleCount(Animation.INDEFINITE);
        patrulare.play();

        plansa.getChildren().addAll(traseu, nava);
    }

    private boolean esteLovita(Circle glont) {
        if (!nava.getBoundsInParent().intersects(glont.getBoundsInParent())) {
            return false;
        }
        Point2D inScena = glont.localToScene(glont.getCenterX(), glont.getCenterY());
        Point2D inCorp = corpNava.sceneToLocal(inScena);
        if (!corpNava.contains(inCorp)) {
            return false;
        }
        lovituri++;
        scor.setText("Lovituri: " + lovituri);
        ScaleTransition puls = new ScaleTransition(Duration.millis(150), corpNava);
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
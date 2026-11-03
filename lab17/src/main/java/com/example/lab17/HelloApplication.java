package com.example.lab17;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        TabPane file = new TabPane(
                new Tab("1. Transformări 2D", creeazaScena2D()),
                new Tab("2–5. Scena 3D", creeazaScena3D()),
                new Tab("Varianta 2", creeazaVarianta()));
        file.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        stage.setTitle("Lucrarea de laborator 17 – Nume Prenume, grupa");
        stage.setScene(new Scene(file, 1000, 650));
        stage.show();
    }

    // =====================================================================
    //  SARCINA 1 – Transformări 2D
    // =====================================================================
    private Parent creeazaScena2D() {
        Pane plansa = new Pane();

        // a) Translate: deplasare cu (60, 30)
        Rectangle r1 = dreptunghi(Color.STEELBLUE, 40, 60);
        r1.getTransforms().add(new Translate(60, 30));

        // b) Rotate cu 30° în jurul centrului (50, 30) al dreptunghiului 100x60
        Rectangle r2 = dreptunghi(Color.ORANGE, 290, 60);
        r2.getTransforms().add(new Rotate(30, 50, 30));

        // c) Scale: de 1.5 ori pe X, 0.6 pe Y, față de centru
        Rectangle r3 = dreptunghi(Color.SEAGREEN, 520, 60);
        r3.getTransforms().add(new Scale(1.5, 0.6, 50, 30));

        // d) Shear: înclinare pe X
        Rectangle r4 = dreptunghi(Color.CRIMSON, 760, 60);
        r4.getTransforms().add(new Shear(0.4, 0));

        // e) Ordinea contează: aceleași transformări, ordine inversă
        //    (culori și dimensiuni schimbate: turcoaz și roz, 130 x 40)
        Rectangle a = dreptunghi(Color.TEAL, 80, 300, 130, 40);
        a.getTransforms().addAll(new Translate(150, 0), new Rotate(45));
        Rectangle b = dreptunghi(Color.HOTPINK, 80, 300, 130, 40);
        b.getTransforms().addAll(new Rotate(45), new Translate(150, 0));

        // f) Rotire controlată de un Slider (binding)
        //    (culoare și dimensiune schimbate: verde-lime, pătrat 90 x 90, pivot în centru)
        Rectangle r5 = dreptunghi(Color.LIMEGREEN, 680, 300, 90, 90);
        Rotate rot = new Rotate(0, 45, 45);
        r5.getTransforms().add(rot);
        Slider slider = new Slider(0, 360, 0);
        slider.setShowTickLabels(true);
        slider.setLayoutX(630);
        slider.setLayoutY(470);
        slider.setPrefWidth(200);
        rot.angleProperty().bind(slider.valueProperty());

        plansa.getChildren().addAll(r1, r2, r3, r4, a, b, r5, slider,
                eticheta("Translate", 40, 30), eticheta("Rotate 30°", 290, 30),
                eticheta("Scale 1.5 × 0.6", 520, 30), eticheta("Shear 0.4", 760, 30),
                eticheta("Turcoaz: Translate→Rotate; Roz: Rotate→Translate", 80, 270),
                eticheta("Rotate legat de Slider", 680, 270));
        return plansa;
    }

    private Rectangle dreptunghi(Color culoare, double x, double y) {
        return dreptunghi(culoare, x, y, 100, 60);
    }

    private Rectangle dreptunghi(Color culoare, double x, double y, double lat, double inalt) {
        Rectangle r = new Rectangle(lat, inalt, culoare);
        r.setStroke(Color.BLACK);
        r.setLayoutX(x);
        r.setLayoutY(y);
        return r;
    }

    private Label eticheta(String text, double x, double y) {
        Label l = new Label(text);
        l.setLayoutX(x);
        l.setLayoutY(y);
        return l;
    }

    // =====================================================================
    //  SARCINILE 2–5 – Scena 3D, iluminare, textură, animație
    // =====================================================================
    private Parent creeazaScena3D() {
        // ---------- obiectele 3D ----------
        Box cutie = new Box(150, 150, 150);               // lățime, înălțime, adâncime
        cutie.setTranslateX(-260);
        cutie.getTransforms().addAll(
                new Rotate(30, Rotate.X_AXIS),
                new Rotate(45, Rotate.Y_AXIS));

        Sphere sfera = new Sphere(100, 64);              // rază, diviziuni

        Cylinder cilindru = new Cylinder(60, 200);       // rază, înălțime
        cilindru.setTranslateX(260);
        cilindru.getTransforms().add(new Rotate(20, Rotate.Z_AXIS));

        Group lume = new Group(cutie, sfera, cilindru);  // obiectele
        Group lumini = new Group();                      // luminile (Sarcina 3)

        // ---------- camera ----------
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setTranslateZ(-900);                      // retragem camera spre privitor
        camera.setFieldOfView(40);

        // ---------- SubScene cu tampon de adâncime ----------
        SubScene sub = new SubScene(new Group(lume, lumini), 1000, 540,
                true, SceneAntialiasing.BALANCED);
        sub.setFill(Color.web("#1b2030"));
        sub.setCamera(camera);

        // ---------- rotirea lumii cu mouse-ul, zoom cu rotița ----------
        Rotate rx = new Rotate(-15, Rotate.X_AXIS);
        Rotate ry = new Rotate(-20, Rotate.Y_AXIS);
        lume.getTransforms().addAll(rx, ry);
        double[] start = new double[2];
        sub.setOnMousePressed(e -> { start[0] = e.getX(); start[1] = e.getY(); });
        sub.setOnMouseDragged(e -> {
            ry.setAngle(ry.getAngle() + (e.getX() - start[0]) * 0.3);
            rx.setAngle(rx.getAngle() - (e.getY() - start[1]) * 0.3);
            start[0] = e.getX();
            start[1] = e.getY();
        });
        sub.setOnScroll(e -> camera.setTranslateZ(camera.getTranslateZ() + e.getDeltaY()));

        BorderPane radacina = new BorderPane(sub);

        // ================= SARCINA 3: materiale și lumini =================
        // ---------- materiale Phong ----------
        PhongMaterial matCutie = new PhongMaterial(Color.DARKORANGE);
        matCutie.setSpecularColor(Color.WHITE);    // fără specularColor nu există luciu
        matCutie.setSpecularPower(32);
        cutie.setMaterial(matCutie);

        PhongMaterial matSfera = new PhongMaterial(Color.CORNFLOWERBLUE);
        matSfera.setSpecularColor(Color.WHITE);
        matSfera.setSpecularPower(64);             // luciu mai concentrat
        sfera.setMaterial(matSfera);

        cilindru.setMaterial(new PhongMaterial(Color.MEDIUMSEAGREEN)); // mat, fără luciu

        // ---------- lumini ----------
        AmbientLight ambient = new AmbientLight(Color.rgb(50, 50, 60));

        PointLight bec = new PointLight(Color.WHITE);
        bec.setTranslateX(-400);
        bec.setTranslateY(-300);    // Y negativ = sus
        bec.setTranslateZ(-500);    // Z negativ = în fața obiectelor

        PointLight becRosu = new PointLight(Color.RED);
        becRosu.setTranslateX(500);
        becRosu.setTranslateZ(-200);

        lumini.getChildren().addAll(ambient, bec, becRosu);

        // ---------- panoul de control al luminilor ----------
        CheckBox cbAmbient = new CheckBox("AmbientLight");
        CheckBox cbBec = new CheckBox("Bec alb");
        CheckBox cbRosu = new CheckBox("Bec roșu");
        cbAmbient.setSelected(true);
        cbBec.setSelected(true);
        cbRosu.setSelected(true);
        ambient.lightOnProperty().bind(cbAmbient.selectedProperty());
        bec.lightOnProperty().bind(cbBec.selectedProperty());
        becRosu.lightOnProperty().bind(cbRosu.selectedProperty());

        Slider slX = new Slider(-600, 600, -400);
        Slider slZ = new Slider(-800, 800, -500);
        bec.translateXProperty().bind(slX.valueProperty());
        bec.translateZProperty().bind(slZ.valueProperty());

        ColorPicker culoareBec = new ColorPicker(Color.WHITE);
        bec.colorProperty().bind(culoareBec.valueProperty());

        HBox panou = new HBox(12, cbAmbient, cbBec, cbRosu,
                new Label("X bec"), slX, new Label("Z bec"), slZ, culoareBec);
        panou.setPadding(new Insets(10));
        radacina.setBottom(panou);

        // ================= SARCINA 4: texturi =================
        // sfera: textură generată; diffuseColor alb = culorile texturii rămân neschimbate
        matSfera.setDiffuseColor(Color.WHITE);
        matSfera.setDiffuseMap(tablaDeSah(512, 16, Color.WHITE, Color.DARKSLATEBLUE));

        // cutia: textură din fișier + relief
        matCutie.setDiffuseColor(Color.WHITE);
        matCutie.setDiffuseMap(textura("/texturi/lemn.jpg"));
        matCutie.setBumpMap(hartaNormale(256, 6));

        // cilindrul: comutator FILL / LINE
        CheckBox cbSarma = new CheckBox("Cilindru: DrawMode.LINE");
        cbSarma.selectedProperty().addListener((obs, vechi, nou) ->
                cilindru.setDrawMode(nou ? DrawMode.LINE : DrawMode.FILL));
        panou.getChildren().add(cbSarma);

        // ================= SARCINA 5: animație =================
        RotateTransition rotSfera = new RotateTransition(Duration.seconds(8), sfera);
        rotSfera.setAxis(Rotate.Y_AXIS);
        rotSfera.setByAngle(360);
        rotSfera.setInterpolator(Interpolator.LINEAR);   // viteză constantă
        rotSfera.setCycleCount(Animation.INDEFINITE);
        rotSfera.play();

        RotateTransition rotCutie = new RotateTransition(Duration.seconds(5), cutie);
        rotCutie.setAxis(Rotate.Y_AXIS);
        rotCutie.setByAngle(-360);                       // în sens invers
        rotCutie.setInterpolator(Interpolator.LINEAR);
        rotCutie.setCycleCount(Animation.INDEFINITE);
        rotCutie.play();

        sub.setOnMouseClicked(e -> {
            if (!e.isStillSincePress()) return;          // ignorăm sfârșitul unei trageri
            if (rotSfera.getStatus() == Animation.Status.RUNNING) {
                rotSfera.pause();
                rotCutie.pause();
            } else {
                rotSfera.play();
                rotCutie.play();
            }
        });

        return radacina;
    }

    // ---------- metode ajutătoare pentru texturi (Sarcina 4) ----------

    // Încarcă o imagine din src/main/resources; dacă lipsește, generează o textură de rezervă
    private Image textura(String cale) {
        URL url = getClass().getResource(cale);
        if (url != null) return new Image(url.toExternalForm());
        System.out.println("Nu găsesc " + cale + " – folosesc o textură generată");
        return tablaDeSah(256, 8, Color.BURLYWOOD, Color.SADDLEBROWN);
    }

    // Textură procedurală: tablă de șah
    private static WritableImage tablaDeSah(int dim, int celule, Color c1, Color c2) {
        WritableImage img = new WritableImage(dim, dim);
        PixelWriter pw = img.getPixelWriter();
        int pas = dim / celule;
        for (int y = 0; y < dim; y++)
            for (int x = 0; x < dim; x++)
                pw.setColor(x, y, ((x / pas) + (y / pas)) % 2 == 0 ? c1 : c2);
        return img;
    }

    // Hartă de normale procedurală: „gropițe” regulate (pentru bumpMap)
    private static WritableImage hartaNormale(int dim, int frecventa) {
        WritableImage img = new WritableImage(dim, dim);
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < dim; y++)
            for (int x = 0; x < dim; x++) {
                double u = 2 * Math.PI * frecventa * x / dim;
                double v = 2 * Math.PI * frecventa * y / dim;
                double nx = -0.5 * Math.cos(u) * Math.sin(v);   // panta suprafeței pe X
                double ny = -0.5 * Math.sin(u) * Math.cos(v);   // panta suprafeței pe Y
                double l = Math.sqrt(nx * nx + ny * ny + 1);
                // normala (nx, ny, 1) codificată în culoare: componenta -1..1 → 0..1
                pw.setColor(x, y, Color.color((nx / l + 1) / 2, (ny / l + 1) / 2, (1 / l + 1) / 2));
            }
        return img;
    }

    // Textură procedurală de zăpadă: alb-albăstrui cu pete aleatoare (pentru sol)
    private static WritableImage texturaZapada(int dim) {
        WritableImage img = new WritableImage(dim, dim);
        PixelWriter pw = img.getPixelWriter();
        Random rnd = new Random(7);
        for (int y = 0; y < dim; y++)
            for (int x = 0; x < dim; x++) {
                double v = 0.86 + rnd.nextDouble() * 0.14;
                pw.setColor(x, y, Color.color(v, v, Math.min(1.0, v + 0.05)));
            }
        return img;
    }

    // =====================================================================
    //  SARCINA INDIVIDUALĂ – Varianta 2: Om de zăpadă cu 5 nivele
    //  (jos → sus: albastru, violet, roșu, bej, alb)
    // =====================================================================
    private Parent creeazaVarianta() {
        // ---------- cele 5 nivele (sfere scalate descrescător) ----------
        Color[] culori = { Color.DODGERBLUE, Color.BLUEVIOLET, Color.CRIMSON,
                Color.BEIGE, Color.WHITE };
        double[] scari = { 1.0, 0.82, 0.66, 0.52, 0.40 };   // factori Scale
        double razaBaza = 80;

        Group om = new Group();
        double y = 0;            // centrul sferei curente (Y negativ = sus)
        double razaAnterioara = 0;
        double yCap = 0, razaCap = 0;

        for (int i = 0; i < culori.length; i++) {
            double raza = razaBaza * scari[i];
            if (i > 0) y -= 0.88 * (razaAnterioara + raza);  // sferele se suprapun puțin

            Sphere nivel = new Sphere(razaBaza, 48);
            // Translate + Scale cu pivot explicit în centrul sferei (0, 0, 0)
            nivel.getTransforms().addAll(
                    new Translate(0, y, 0),
                    new Scale(scari[i], scari[i], scari[i], 0, 0, 0));

            PhongMaterial mat = new PhongMaterial(culori[i]);
            mat.setSpecularColor(Color.WHITE);
            mat.setSpecularPower(64);                        // lucios
            nivel.setMaterial(mat);
            om.getChildren().add(nivel);

            razaAnterioara = raza;
            yCap = y;
            razaCap = raza;
        }

        // ---------- nasul: Cylinder rotit 90° pe X, pivot explicit în (0,0,0) ----------
        Cylinder nas = new Cylinder(4.5, 30);
        nas.setMaterial(new PhongMaterial(Color.ORANGE));
        nas.getTransforms().addAll(
                new Translate(0, yCap + 4, -(razaCap + 10)),
                new Rotate(90, 0, 0, 0, Rotate.X_AXIS));

        // ---------- ochii ----------
        PhongMaterial matOchi = new PhongMaterial(Color.BLACK);
        Sphere ochiS = new Sphere(4);
        Sphere ochiD = new Sphere(4);
        ochiS.setMaterial(matOchi);
        ochiD.setMaterial(matOchi);
        ochiS.getTransforms().add(new Translate(-11, yCap - 8, -(razaCap - 4)));
        ochiD.getTransforms().add(new Translate(11, yCap - 8, -(razaCap - 4)));

        // ---------- solul de zăpadă (textură procedurală proprie) ----------
        Cylinder sol = new Cylinder(260, 6);
        PhongMaterial matSol = new PhongMaterial(Color.WHITE);
        matSol.setDiffuseMap(texturaZapada(256));
        sol.setMaterial(matSol);
        sol.setTranslateY(razaBaza + 3);

        om.getChildren().addAll(nas, ochiS, ochiD);

        // grupul lume: omul + solul, centrat vertical pe ecran
        Group lume = new Group(sol, om);
        lume.setTranslateY(165);

        // ---------- lumini: ambient slab + PointLight albastru-deschis ----------
        AmbientLight ambient = new AmbientLight(Color.rgb(60, 60, 70));
        PointLight becAlbastru = new PointLight(Color.LIGHTBLUE);
        becAlbastru.setTranslateX(-400);
        becAlbastru.setTranslateY(-300);
        becAlbastru.setTranslateZ(-500);
        Group lumini = new Group(ambient, becAlbastru);

        // ---------- cameră ----------
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setTranslateZ(-1100);
        camera.setFieldOfView(40);

        // ---------- SubScene cu tampon de adâncime ----------
        SubScene sub = new SubScene(new Group(lume, lumini), 1000, 540,
                true, SceneAntialiasing.BALANCED);
        sub.setFill(Color.web("#12192b"));
        sub.setCamera(camera);

        // ---------- rotire cu mouse-ul + zoom ----------
        Rotate rx = new Rotate(-10, Rotate.X_AXIS);
        Rotate ry = new Rotate(-25, Rotate.Y_AXIS);
        lume.getTransforms().addAll(rx, ry);
        double[] start = new double[2];
        sub.setOnMousePressed(e -> { start[0] = e.getX(); start[1] = e.getY(); });
        sub.setOnMouseDragged(e -> {
            ry.setAngle(ry.getAngle() + (e.getX() - start[0]) * 0.3);
            rx.setAngle(rx.getAngle() - (e.getY() - start[1]) * 0.3);
            start[0] = e.getX();
            start[1] = e.getY();
        });
        sub.setOnScroll(e -> camera.setTranslateZ(camera.getTranslateZ() + e.getDeltaY()));

        // ---------- panou de control ----------
        CheckBox cbAmbient = new CheckBox("AmbientLight");
        CheckBox cbBec = new CheckBox("PointLight albastru");
        cbAmbient.setSelected(true);
        cbBec.setSelected(true);
        ambient.lightOnProperty().bind(cbAmbient.selectedProperty());
        becAlbastru.lightOnProperty().bind(cbBec.selectedProperty());

        Slider slX = new Slider(-600, 600, -400);
        becAlbastru.translateXProperty().bind(slX.valueProperty());

        HBox panou = new HBox(12, cbAmbient, cbBec, new Label("X bec"), slX);
        panou.setPadding(new Insets(10));

        BorderPane radacina = new BorderPane(sub);
        radacina.setBottom(panou);
        return radacina;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
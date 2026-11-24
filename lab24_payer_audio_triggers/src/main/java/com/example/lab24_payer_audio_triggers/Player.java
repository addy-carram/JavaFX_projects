package com.example.lab24_payer_audio_triggers;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Player extends Application {

    // pseudo-clase proprii (DataTrigger)
    private static final PseudoClass PORNIT = PseudoClass.getPseudoClass("pornit");
    private static final PseudoClass PAUZA = PseudoClass.getPseudoClass("pauza");
    private static final PseudoClass MUT = PseudoClass.getPseudoClass("mut");

    private static final double LATIME_RAND = 440;
    private static final double INALTIME_RAND = 56;
    private static final double VITEZA = 1;   // 1 = timp real; pune 10 ca să testezi repede

    private Scene scene;
    private final ToggleGroup grup = new ToggleGroup();
    private final List<Piesa> piese = new ArrayList<>();
    private int curent = 0;

    private Label lblTitluAcum, lblArtistAcum, lblTimpAcum;

    // ================= o piesă din playlist =================
    private class Piesa {
        final String titlu, artist;
        final int durata;                                   // în secunde
        final DoubleProperty progres = new SimpleDoubleProperty(0);   // 0..1
        final Timeline timeline;
        final ToggleButton buton = new ToggleButton();
        final Label timp = new Label();

        Piesa(String titlu, String artist, int durata) {
            this.titlu = titlu;
            this.artist = artist;
            this.durata = durata;

            Label lblTitlu = new Label(titlu);
            lblTitlu.getStyleClass().add("titlu-piesa");
            Label lblArtist = new Label(artist);
            lblArtist.getStyleClass().add("artist-piesa");
            VBox texte = new VBox(2, lblTitlu, lblArtist);

            timp.getStyleClass().add("timp-piesa");
            timp.setText(format(durata));
            Region spatiu = new Region();
            HBox.setHgrow(spatiu, Priority.ALWAYS);
            HBox continut = new HBox(10, texte, spatiu, timp);
            continut.getStyleClass().add("continut-rand");
            continut.setAlignment(Pos.CENTER_LEFT);

            // două straturi: baza (partea neajunsă, 50%) și umplerea (partea parcursă, 100%)
            Region baza = new Region();
            baza.getStyleClass().add("baza");
            Region plin = new Region();
            plin.getStyleClass().add("plin");
            plin.maxWidthProperty().bind(progres.multiply(LATIME_RAND));
            StackPane.setAlignment(plin, Pos.CENTER_LEFT);

            StackPane rand = new StackPane(baza, plin, continut);
            rand.setPrefSize(LATIME_RAND, INALTIME_RAND);
            rand.setMinSize(LATIME_RAND, INALTIME_RAND);
            rand.setMaxSize(LATIME_RAND, INALTIME_RAND);
            Rectangle decupaj = new Rectangle(LATIME_RAND, INALTIME_RAND);
            decupaj.setArcWidth(28);
            decupaj.setArcHeight(28);
            rand.setClip(decupaj);                          // colțuri rotunjite pentru umplere

            buton.setGraphic(rand);
            buton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            buton.setPrefSize(LATIME_RAND, INALTIME_RAND);
            buton.getStyleClass().add("rand-piesa");
            buton.setToggleGroup(grup);
            buton.setUserData(this);

            // linia se umple linear pe toată durata piesei
            timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(progres, 0)),
                    new KeyFrame(Duration.seconds(durata / VITEZA),
                            new KeyValue(progres, 1, Interpolator.LINEAR)));

            progres.addListener((o, v, n) -> actualizeaza());
            buton.selectedProperty().addListener((o, v, selectat) -> {
                if (selectat) {
                    if (timeline.getStatus() == Animation.Status.PAUSED) timeline.play();   // continuă
                    else timeline.playFromStart();                                          // pornește
                } else {
                    timeline.pause();
                }
                actualizeaza();
            });
        }

        /** Textul timpului + pseudo-clasele proprii :pornit și :pauza. */
        void actualizeaza() {
            double p = progres.get();
            int scurs = (int) Math.round(p * durata);
            timp.setText(p <= 0 ? format(durata) : format(scurs) + " / " + format(durata));
            boolean sel = buton.isSelected();
            buton.pseudoClassStateChanged(PORNIT, sel);
            buton.pseudoClassStateChanged(PAUZA, !sel && p > 0 && p < 1);
            actualizeazaAntet();
        }
    }

    @Override
    public void start(Stage stage) {
        // ---------- antet cu alegerea temei ----------
        Label lblTema = new Label("Tema:");
        ComboBox<String> cmbTema = new ComboBox<>();
        cmbTema.getItems().addAll("Întunecată", "Luminoasă");
        cmbTema.setValue("Întunecată");
        HBox antet = new HBox(8, lblTema, cmbTema);
        antet.getStyleClass().add("antet");

        // ---------- cardul „acum rulează” ----------
        Label eticheta = new Label("ACUM RULEAZĂ");
        eticheta.getStyleClass().add("eticheta-mica");
        lblTitluAcum = new Label();
        lblTitluAcum.setId("titlu-acum");
        lblArtistAcum = new Label();
        lblArtistAcum.getStyleClass().add("artist-acum");
        lblTimpAcum = new Label();
        lblTimpAcum.setId("timp-acum");
        VBox cardAcum = new VBox(4, eticheta, lblTitluAcum, lblArtistAcum, lblTimpAcum);
        cardAcum.getStyleClass().add("card");

        // ---------- playlistul ----------
        piese.add(new Piesa("Perfect", "Ed Sheeran", 263));            // 4:23
        piese.add(new Piesa("Blinding Lights", "The Weeknd", 200));    // 3:20
        piese.add(new Piesa("Shape of You", "Ed Sheeran", 233));       // 3:53
        piese.add(new Piesa("Starboy", "The Weeknd", 230));            // 3:50

        VBox playlist = new VBox(10);
        playlist.setAlignment(Pos.TOP_CENTER);
        for (int i = 0; i < piese.size(); i++) {
            Piesa p = piese.get(i);
            final int idx = i;
            p.timeline.setOnFinished(e -> urmatoarea(idx));
            adaugaAnimatieHover(p.buton);                              // trigger pe eveniment
            playlist.getChildren().add(p.buton);
        }

        // ---------- comenzi: Play/Pauză + volum ----------
        ToggleButton btnPlay = new ToggleButton();
        btnPlay.getStyleClass().add("play");
        btnPlay.textProperty().bind(
                Bindings.when(btnPlay.selectedProperty()).then("Pauză").otherwise("Play"));
        adaugaAnimatieHover(btnPlay);

        Label lblVol = new Label("Volum: 60");
        lblVol.getStyleClass().add("volum");
        Slider sldVol = new Slider(0, 100, 60);
        sldVol.setPrefWidth(160);
        sldVol.valueProperty().addListener((o, v, n) -> {
            boolean mut = n.intValue() == 0;
            lblVol.setText(mut ? "Volum: mut" : "Volum: " + n.intValue());
            lblVol.pseudoClassStateChanged(MUT, mut);                  // DataTrigger
        });

        Region spatiu = new Region();
        HBox.setHgrow(spatiu, Priority.ALWAYS);
        HBox comenzi = new HBox(12, btnPlay, spatiu, lblVol, sldVol);
        comenzi.setAlignment(Pos.CENTER_LEFT);

        // ---------- legătura între Play/Pauză și piese ----------
        grup.selectedToggleProperty().addListener((o, v, n) -> {
            if (n != null) curent = piese.indexOf((Piesa) n.getUserData());
            btnPlay.setSelected(n != null);
            actualizeazaAntet();
        });
        btnPlay.selectedProperty().addListener((o, v, selectat) -> {
            if (selectat && grup.getSelectedToggle() == null) {
                grup.selectToggle(piese.get(curent).buton);
            } else if (!selectat) {
                grup.selectToggle(null);                               // pauză
            }
        });

        VBox corp = new VBox(16, cardAcum, playlist, comenzi);
        corp.setPadding(new Insets(20));
        VBox root = new VBox(antet, corp);

        scene = new Scene(root, 480, 620);
        cmbTema.valueProperty().addListener((o, veche, noua) -> aplicaTema(noua));
        aplicaTema(cmbTema.getValue());
        actualizeazaAntet();

        stage.setTitle("Lucrarea 24 – Player audio simulat");
        stage.setScene(scene);
        stage.show();
    }

    /** La sfârșitul piesei pornește următoarea. */
    private void urmatoarea(int i) {
        if (i + 1 < piese.size()) {
            Piesa urm = piese.get(i + 1);
            urm.timeline.stop();
            urm.progres.set(0);                                        // pornește de la început
            grup.selectToggle(urm.buton);
        } else {
            grup.selectToggle(null);
        }
    }

    private void actualizeazaAntet() {
        if (lblTitluAcum == null || piese.isEmpty()) return;
        Piesa p = piese.get(curent);
        lblTitluAcum.setText(p.titlu);
        lblArtistAcum.setText(p.artist);
        int scurs = (int) Math.round(p.progres.get() * p.durata);
        lblTimpAcum.setText(format(scurs) + " / " + format(p.durata));
    }

    /** Trigger pe eveniment: mărire lină la hover. */
    private void adaugaAnimatieHover(Node nod) {
        ScaleTransition intra = new ScaleTransition(Duration.millis(120), nod);
        intra.setToX(1.03);
        intra.setToY(1.03);
        ScaleTransition iese = new ScaleTransition(Duration.millis(120), nod);
        iese.setToX(1.0);
        iese.setToY(1.0);
        nod.hoverProperty().addListener((o, era, este) -> {
            if (este) { iese.stop(); intra.playFromStart(); }
            else      { intra.stop(); iese.playFromStart(); }
        });
    }

    private void aplicaTema(String tema) {
        String fisier = switch (tema) {
            case "Luminoasă" -> "player-luminoasa.css";
            default -> "player-intunecata.css";
        };
        // ordinea contează: întâi regulile comune, apoi tema
        scene.getStylesheets().setAll(css("player-comun.css"), css(fisier));
    }

    private String css(String nume) {
        return getClass().getResource(nume).toExternalForm();
    }

    private static String format(int secunde) {
        return (secunde / 60) + ":" + String.format("%02d", secunde % 60);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
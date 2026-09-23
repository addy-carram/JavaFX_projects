package com.example.control_elements_lab7;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HelloController {

    @FXML
    private DatePicker datePicker;

    @FXML
    private Spinner<Integer> spinnerOra;

    @FXML
    private Spinner<Integer> spinnerMinut;

    @FXML
    private Button btnProgrameaza;

    @FXML
    private Label lblMesaj;

    @FXML
    private Label lblCeasCurent;

    // Controlere pentru Exercițiul 2
    @FXML
    private Slider volumeSlider;

    @FXML
    private Spinner<Integer> volumeSpinner;

    @FXML
    private ProgressBar volumeProgressBar;

    @FXML
    private ProgressIndicator volumeIndicator;

    @FXML
    private ScrollBar zoomScrollBar;

    @FXML
    private Label lblZoom;

    // Controlere pentru a treia sarcină (Slider + ImageView)
    @FXML
    private Slider imageSlider;

    @FXML
    private ImageView imageView;

    @FXML
    private void initialize() {
        // 1. Configurare Spinner pentru Ore (0 - 23)
        SpinnerValueFactory<Integer> oraFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12);
        spinnerOra.setValueFactory(oraFactory);

        // 2. Configurare Spinner pentru Minute (0 - 59)
        SpinnerValueFactory<Integer> minutFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        spinnerMinut.setValueFactory(minutFactory);

        // 3. Validare DatePicker: Dezactivarea zilelor din trecut
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        // 4. Pornire ceas în timp real
        initCeasInTimpReal();

        // 5. Inițializare Exercițiul 2 (Volume & Zoom)
        initExercitiul2();

        // 6. Inițializare a treia sarcină (Slider imagini) - AICI ERA PROBLEMA
        initSarcinaImagini();
        initCronometru();
    }

    private void initCeasInTimpReal() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            lblCeasCurent.setText("Ora curentă: " + LocalTime.now().format(formatter));
        });

        Timeline timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void initExercitiul2() {
        // 1. Configurare inițială Spinner Volum
        SpinnerValueFactory<Integer> volumeFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
        volumeSpinner.setValueFactory(volumeFactory);

        // 2. Sincronizare bidirecțională Slider - Spinner
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !volumeSpinner.getValue().equals(newValue.intValue())) {
                volumeSpinner.getValueFactory().setValue(newValue.intValue());
            }
        });

        volumeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && volumeSlider.getValue() != newValue.doubleValue()) {
                volumeSlider.setValue(newValue.doubleValue());
            }
        });

        // 3. Sincronizare ProgressBar și ProgressIndicator
        volumeProgressBar.progressProperty().bind(volumeSlider.valueProperty().divide(100.0));
        volumeIndicator.progressProperty().bind(volumeSlider.valueProperty().divide(100.0));

        // 4. Control ScrollBar Zoom
        zoomScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int procentZoom = (int) Math.round(newValue.doubleValue());
                lblZoom.setText("Zoom: " + procentZoom + "%");
            }
        });

        lblZoom.setText("Zoom: " + (int) zoomScrollBar.getValue() + "%");
    }
    // Adaugă aceste variabile @FXML sus, alături de celelalte controlere:
    @FXML
    private Button btnPrev;

    @FXML
    private Button btnNext;

    @FXML
    private Label lblEroareImagine;

    // Flag ca să oprim declanșarea infinită a listenerului când schimbăm programatic valoarea slider-ului
    private boolean isUpdatingSlider = false;

    // Înlocuiește metoda initSarcinaImagini() cu aceasta:
    private Image[] images;
    private int currentIndex = 0; // Ținem minte indexul imaginii curente

    private void initSarcinaImagini() {
        File file1 = new File("src/main/resources/assests/tele1.jpg");
        File file2 = new File("src/main/resources/assests/tele2.jpg");
        File file3 = new File("src/main/resources/assests/tele3.jpg");

        // Verificăm dacă există cel puțin o imagine
        if (!file1.exists() && !file2.exists() && !file3.exists()) {
            lblEroareImagine.setText("Eroare: Nu s-a găsit nici o imagine în folderul assests!");
            return;
        }

        images = new Image[] {
                file1.exists() ? new Image(file1.toURI().toString()) : null,
                file2.exists() ? new Image(file2.toURI().toString()) : null,
                file3.exists() ? new Image(file3.toURI().toString()) : null
        };

        // 1. Configurarea Slider-ului pentru Zoom (100px - 500px, valoare inițială 300)
        imageSlider.setMin(100.0);
        imageSlider.setMax(1000.0);
        imageSlider.setValue(300.0);

        // 2. Legăm slider-ul direct de lățimea imaginii (fitWidth)
        // preserveRatio="true" din FXML va face ca înălțimea să se schimbe proporțional automat
        imageView.fitWidthProperty().bind(imageSlider.valueProperty());

        // 3. Setăm prima imagine la pornire
        afiseazaImagineaCurenta();
    }

    // Afișează imaginea curentă în funcție de butonul apăsat
    private void afiseazaImagineaCurenta() {
        if (images != null && images.length > 0) {
            if (images[currentIndex] != null) {
                imageView.setImage(images[currentIndex]);
                lblEroareImagine.setText("");
            } else {
                imageView.setImage(null);
                lblEroareImagine.setText("Atenție: Imaginea " + (currentIndex + 1) + " lipsește!");
            }
        }
    }

    // Butonul Înapoi (<) - Schimbă imaginea anterioară
    @FXML
    private void handlePrevImage() {
        if (images == null) return;
        if (currentIndex > 0) {
            currentIndex--;
            afiseazaImagineaCurenta();
        }
    }

    // Butonul Următor (>) - Schimbă imaginea următoare
    @FXML
    private void handleNextImage() {
        if (images == null) return;
        if (currentIndex < images.length - 1) {
            currentIndex++;
            afiseazaImagineaCurenta();
        }
    }
    @FXML
    private void handleProgrameaza() {
        LocalDate dataSelectata = datePicker.getValue();
        if (dataSelectata == null) {
            lblMesaj.setText("Eroare: Vă rugăm să selectați o dată!");
            lblMesaj.setStyle("-fx-text-fill: red;");
            return;
        }

        int ora = spinnerOra.getValue();
        int minut = spinnerMinut.getValue();

        String minutFormatat = String.format("%02d", minut);
        String oraFormatata = String.format("%02d", ora);

        lblMesaj.setText("Întâlnire programată la data " + dataSelectata + " , ora " + oraFormatata + ":" + minutFormatat);
        lblMesaj.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    }

    ///4 exercize
    @FXML
    private Spinner<Integer> spinnerCronometruMinute;

    @FXML
    private Spinner<Integer> spinnerCronometruSecunde;

    @FXML
    private Button btnStartCronometru;

    @FXML
    private ProgressIndicator progressCronometru;

    private Timeline timelineCronometru;
    private int timpTotalSecundeRamas = 0;
    private int timpInitialSecunde = 0;
    private void initCronometru() {
        // Configurarea spinner-ului pentru minute (de la 0 la 60, valoare inițială 0 sau 1)
        SpinnerValueFactory<Integer> minFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60, 1);
        spinnerCronometruMinute.setValueFactory(minFactory);

        // Configurarea spinner-ului pentru secunde (de la 0 la 59, valoare inițială 0)
        SpinnerValueFactory<Integer> secFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        spinnerCronometruSecunde.setValueFactory(secFactory);
    }

    @FXML
    private void handleStartCronometru() {
        // Dacă un cronometru e deja pornit, îl oprim la un nou click (opțional, ca un toggle)
        if (timelineCronometru != null && timelineCronometru.getStatus() == Animation.Status.RUNNING) {
            timelineCronometru.stop();
            btnStartCronometru.setText("start");
            spinnerCronometruMinute.setDisable(false);
            spinnerCronometruSecunde.setDisable(false);
            return;
        }

        // Preluăm minutele și secundele selectate de utilizator
        int minute = spinnerCronometruMinute.getValue();
        int secunde = spinnerCronometruSecunde.getValue();

        timpInitialSecunde = (minute * 60) + secunde;
        timpTotalSecundeRamas = timpInitialSecunde;

        if (timpTotalSecundeRamas <= 0) {
            return; // Nu avem ce cronometra dacă timpul e 0
        }

        // Dezactivăm spinner-urile în timpul numărătorii
        spinnerCronometruMinute.setDisable(true);
        spinnerCronometruSecunde.setDisable(true);
        btnStartCronometru.setText("Oprește");

        // Creăm un Timeline care scade o secundă la fiecare pas
        timelineCronometru = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (timpTotalSecundeRamas > 0) {
                timpTotalSecundeRamas--;

                // Actualizăm valorile în Spinner-e în timp ce scade timpul
                spinnerCronometruMinute.getValueFactory().setValue(timpTotalSecundeRamas / 60);
                spinnerCronometruSecunde.getValueFactory().setValue(timpTotalSecundeRamas % 60);

                // Calculăm progresul pentru ProgressIndicator (de la 0.0 la 1.0)
                // Progresul crește sau scade pe măsură ce trece timpul
                double progres = 1.0 - ((double) timpTotalSecundeRamas / timpInitialSecunde);
                progressCronometru.setProgress(progres);

            } else {
                // Când s-a scurs timpul
                timelineCronometru.stop();
                btnStartCronometru.setText("start");
                spinnerCronometruMinute.setDisable(false);
                spinnerCronometruSecunde.setDisable(false);
                progressCronometru.setProgress(1.0);
            }
        }));

        timelineCronometru.setCycleCount(Animation.INDEFINITE);
        timelineCronometru.play();
    }

    }

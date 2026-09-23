package com.example.control_elements_lab7;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

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

                // Dacă data este înainte de ziua de azi, o dezactivăm și o înroșim opțional
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;"); // Fundal roz/roșiatic pentru zilele indisponibile
                }
            }
        });

// 4. Pornire ceas în timp real (Timeline + KeyFrame)
        initCeasInTimpReal();

    }

    private void initCeasInTimpReal() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
// Cream un KeyFrame care rulează la fiecare 1 secundă
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            lblCeasCurent.setText("Ora curentă: " + LocalTime.now().format(formatter));
        });

// Configurăm Timeline-ul să ruleze la nesfârșit
        Timeline timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        //exercitiul2

        // 1. Configurare inițială Spinner (Min: 0, Max: 100, Valoare inițială: 0)
        SpinnerValueFactory<Integer> volumeFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
        volumeSpinner.setValueFactory(volumeFactory);

        // 2. Sincronizare bidirecțională între valoarea Slider-ului și valoarea Spinner-ului
        // Deoarece Slider folosește Double și Spinner folosește Integer, le legăm bidirecțional prin intermediul obiectului Integer
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

        // 3. Sincronizare în timp real: ProgressBar și ProgressIndicator (progress = value / 100)
        // Folosim divide(100.0) direct pe proprietatea slider-ului
        volumeProgressBar.progressProperty().bind(volumeSlider.valueProperty().divide(100.0));
        volumeIndicator.progressProperty().bind(volumeSlider.valueProperty().divide(100.0));

        // 4. Control independent ScrollBar pentru Zoom + afișare în Label text formatat
        zoomScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Rotunjim valoarea la număr întreg pentru afișarea procentajului
                int procentZoom = (int) Math.round(newValue.doubleValue());
                lblZoom.setText("Zoom: " + procentZoom + "%");
            }
        });

        // Setăm textul inițial al zoom-ului bazat pe valoarea implicită a scrollbar-ului (100)
        lblZoom.setText("Zoom: " + (int) zoomScrollBar.getValue() + "%");
    }


    @FXML
    private void handleProgrameaza() {
        LocalDate dataSelectata = datePicker.getValue();
// Validare la apăsarea butonului (să nu lase utilizatorul fără dată selectată)
        if (dataSelectata == null) {
            lblMesaj.setText("Eroare: Vă rugăm să selectați o dată!");
            lblMesaj.setStyle("-fx-text-fill: red;");
            return;
        }

        int ora = spinnerOra.getValue();
        int minut = spinnerMinut.getValue();

// Formatăm minutul cu un zero în față dacă este mai mic de 10 (ex: 05 în loc de 5)
        String minutFormatat = String.format("%02d", minut);
        String oraFormatata = String.format("%02d", ora);

// Afișare mesaj de succes
        lblMesaj.setText("Întâlnire programată la data " + dataSelectata + " , ora " + oraFormatata + ":" + minutFormatat);
        lblMesaj.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

    }

    @FXML
    protected void onNextTaskClick(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/control_elements_lab7/second.fxml")
            );
            Parent root = loader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Next Task Window");
            newStage.setScene(new Scene(root));
            newStage.show();

            // linia care lipsea - închide fereastra veche
            Stage currentStage = (Stage) lblMesaj.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            lblMesaj.setText("Error: Could not load the new page!");
        }
    }

    //exercitiul 2
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




}
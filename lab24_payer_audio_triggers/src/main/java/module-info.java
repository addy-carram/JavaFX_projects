module com.example.lab24_payer_audio_triggers {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.lab24_payer_audio_triggers to javafx.fxml;
    exports com.example.lab24_payer_audio_triggers;
}
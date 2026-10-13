module com.example.lab13_formapp_library {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.lab13_formapp_library to javafx.fxml;
    exports com.example.lab13_formapp_library;
}
module com.example.racket_lab16 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.racket_lab16 to javafx.fxml;
    exports com.example.racket_lab16;
}
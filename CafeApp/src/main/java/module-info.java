module com.example.cafeapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.cafeapp to javafx.fxml;
    exports com.example.cafeapp;
}
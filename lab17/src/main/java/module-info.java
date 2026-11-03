module com.example.lab17 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.lab17 to javafx.fxml;
    exports com.example.lab17;
}
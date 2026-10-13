module com.example.lab13_paint {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.lab13_paint to javafx.fxml;
    exports com.example.lab13_paint;
}
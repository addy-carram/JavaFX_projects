module com.example.graphic_interface_lab1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.graphic_interface_lab1 to javafx.fxml;
    exports com.example.graphic_interface_lab1;
}
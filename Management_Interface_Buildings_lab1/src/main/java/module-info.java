module com.example.management_interface_buildings_lab1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.management_interface_buildings_lab1 to javafx.fxml;
    exports com.example.management_interface_buildings_lab1;
}
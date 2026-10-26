module com.example.city_bus_lab15 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.city_bus_lab15 to javafx.fxml;
    exports com.example.city_bus_lab15;
}
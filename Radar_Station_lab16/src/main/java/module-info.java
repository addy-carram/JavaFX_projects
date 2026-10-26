module com.example.radar_station_lab16 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.radar_station_lab16 to javafx.fxml;
    exports com.example.radar_station_lab16;
}
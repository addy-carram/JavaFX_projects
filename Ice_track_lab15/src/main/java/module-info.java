module com.example.ice_track_lab15 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ice_track_lab15 to javafx.fxml;
    exports com.example.ice_track_lab15;
}
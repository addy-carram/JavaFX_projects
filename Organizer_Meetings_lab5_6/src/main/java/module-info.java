module com.example.organizer_meetings_lab5_6 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.organizer_meetings_lab5_6 to javafx.fxml;
    exports com.example.organizer_meetings_lab5_6;
}
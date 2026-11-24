module com.example.lab23_concert_ticket {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.lab23_concert_ticket to javafx.fxml;
    exports com.example.lab23_concert_ticket;
}
module com.example.clients_row_lab14 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.clients_row_lab14 to javafx.fxml;
    exports com.example.clients_row_lab14;
}
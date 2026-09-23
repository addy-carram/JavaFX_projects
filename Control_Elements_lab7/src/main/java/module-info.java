module com.example.control_elements_lab7 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.control_elements_lab7 to javafx.fxml;
    exports com.example.control_elements_lab7;
}
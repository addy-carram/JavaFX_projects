module com.example.learning_command_lab14 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.learning_command_lab14 to javafx.fxml;
    exports com.example.learning_command_lab14;
}
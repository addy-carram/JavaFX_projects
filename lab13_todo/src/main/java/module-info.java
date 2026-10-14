module com.example.lab13_todo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.lab13_todo to javafx.fxml;
    exports com.example.lab13_todo;
}
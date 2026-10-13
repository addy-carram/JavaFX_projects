module com.example.lab13_robot {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.lab13_robot to javafx.fxml;
    exports com.example.lab13_robot;
}
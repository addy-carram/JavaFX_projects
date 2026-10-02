module com.example.creating_dependency_lab10 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.creating_dependency_lab10 to javafx.fxml;
    exports com.example.creating_dependency_lab10;
}
module com.example.cinema_app_lab25_lab26 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.microsoft.sqlserver.jdbc;

    requires java.sql;
    opens com.example.cinema_app_lab25_lab26 to javafx.fxml;
    exports com.example.cinema_app_lab25_lab26;
}
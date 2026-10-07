module com.example.game_x_and_0 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.game_x_and_0 to javafx.fxml;
    exports com.example.game_x_and_0;
}
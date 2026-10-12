module com.example.white_board_lab12 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.white_board_lab12 to javafx.fxml;
    exports com.example.white_board_lab12;
}
module com.example.calculating_the_book_cost_lab9 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.calculating_the_book_cost_lab9 to javafx.fxml;
    exports com.example.calculating_the_book_cost_lab9;
}
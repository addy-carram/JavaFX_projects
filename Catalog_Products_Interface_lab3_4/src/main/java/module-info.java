module com.example.catalog_products_interface_lab3_4 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.catalog_products_interface_lab3_4 to javafx.fxml;
    exports com.example.catalog_products_interface_lab3_4;
}
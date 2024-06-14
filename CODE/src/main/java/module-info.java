module com.example.iie_szarzy_2024 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.slf4j;
    requires java.datatransfer;
    requires java.sql;
    requires kernel;
    requires layout;

    opens com.example.iie_szarzy_2024 to javafx.fxml;
    exports com.example.iie_szarzy_2024;
}
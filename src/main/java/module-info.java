module com.example.iie_szarzy {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;


    opens com.example.iie_szarzy_2024 to javafx.fxml;
    exports com.example.iie_szarzy_2024;
}
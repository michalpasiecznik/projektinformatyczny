package com.example.iie_szarzy_2024;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RemindController {

    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

    @FXML
    private void remindPasswordActionButton() {
        String email = emailField.getText();

        // Wyświetlenie odpowiedniego komunikatu dla użytkownika
        messageLabel.setText("Przypomnienie hasła wysłane na adres: " + email);
    }

    // Metoda obsługująca akcję powrotu do logowania
    @FXML
    private void loginButtonAction() {
        Main newMainApp = new Main(); // Tworzymy nową instancję klasy Main
        Stage stage = (Stage) emailField.getScene().getWindow(); // Pobieramy aktualne okno
        newMainApp.start(stage); // Ładujemy widok logowania
    }
}

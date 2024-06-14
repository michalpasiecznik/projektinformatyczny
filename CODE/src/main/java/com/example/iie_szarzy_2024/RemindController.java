package com.example.iie_szarzy_2024;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemindController {

    public void setMainApp(Main mainApp) {
    }
    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

    private static final Logger logger = LoggerFactory.getLogger(RemindController.class);

    @FXML
    private void remindPasswordActionButton() {
        String email = emailField.getText();

        // Logowanie informacji o rozpoczęciu procesu przypominania hasła
        logger.info("Próba przypomnienia hasła dla adresu email: {}", email);

        // Wyświetlenie odpowiedniego komunikatu dla użytkownika
        messageLabel.setText("Przypomnienie hasła wysłane na adres: " + email);

        // Logowanie informacji o wysłaniu przypomnienia
        logger.info("Przypomnienie hasła wysłane na adres: {}", email);
    }

    // Metoda obsługująca akcję powrotu do logowania
    @FXML
    private void loginButtonAction() {
        Main newMainApp = new Main(); // Tworzymy nową instancję klasy Main
        Stage stage = (Stage) emailField.getScene().getWindow(); // Pobieramy aktualne okno
        newMainApp.start(stage); // Ładujemy widok logowania

        // Logowanie informacji o przejściu do ekranu logowania
        logger.info("Przejście do ekranu logowania.");
    }
}

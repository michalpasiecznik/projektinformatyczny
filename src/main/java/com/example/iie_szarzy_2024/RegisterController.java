package com.example.iie_szarzy_2024;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class RegisterController {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "sklepszary";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @FXML
    public TextField emailField;

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public PasswordField confirmPasswordField;

    @FXML
    public TextField nameField;

    @FXML
    public TextField surnameField;

    @FXML
    public TextField occupationField;

    // Metoda obsługująca akcję rejestracji
    @FXML
    public void registerButtonAction() {
        // Sprawdzenie czy wszystkie pola są wypełnione
        if (emailField.getText().isEmpty() || loginField.getText().isEmpty() ||
                passwordField.getText().isEmpty() || confirmPasswordField.getText().isEmpty() ||
                nameField.getText().isEmpty() || surnameField.getText().isEmpty() ||
                occupationField.getText().isEmpty()) {
            showAlert("Błąd rejestracji", "Wszystkie pola są wymagane.");
            return; // Przerwij proces rejestracji
        }

        String email = emailField.getText();
        String login = loginField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String name = nameField.getText();
        String surname = surnameField.getText();
        String occupation = occupationField.getText();

        // Sprawdzenie zgodności hasła z potwierdzeniem hasła
        if (!password.equals(confirmPassword)) {
            showAlert("Błąd rejestracji", "Hasło i potwierdzenie hasła nie są zgodne.");
            confirmPasswordField.clear();
            return; // Przerwij proces rejestracji
        }

        try {
            // Utworzenie połączenia
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);

            // Użycie nowej bazy danych
            String useDatabaseQuery = "USE " + DB_NAME;
            Statement statement = connection.createStatement();
            statement.executeUpdate(useDatabaseQuery);

            // Sprawdzenie, czy login lub email istnieje już w bazie danych
            String checkQuery = "SELECT * FROM pracownicy WHERE Login='" + login + "' OR Email='" + email + "'";
            ResultSet resultSet = statement.executeQuery(checkQuery);
            if (resultSet.next()) {
                // Jeśli login lub email już istnieje, wyświetl komunikat o błędnych informacjach
                showAlert("Błąd rejestracji", "Podany login lub email już istnieje w bazie danych.");
            } else {
                // Jeśli login i email nie istnieją, wykonaj rejestrację
                String query = "INSERT INTO `pracownicy` (`Imie`, `Nazwisko`, `Login`, `Haslo`, `Stanowisko`, `Email`, `DataZatrudnienia`, `IDKierownika`) VALUES ('"+ name +"', '" + surname + "', '" + login + "', '" + password + "', '"+ occupation +"', '" + email + "', '2024-01-15', 1)";
                statement.executeUpdate(query);
                showAlert("Rejestracja udana", "Konto zostało pomyślnie zarejestrowane.");
            }

            statement.close();
            connection.close();

            // Czyszczenie pól formularza
            clearFields();

        } catch (Exception e) {
            showAlert("Błąd rejestracji", "Wystąpił błąd podczas inicjalizacji rejestracji.");
        }
    }

    // Metoda obsługująca przycisk logowania
    @FXML
    private void loginButtonAction() {
        Main newMainApp = new Main();
        Stage stage = (Stage) emailField.getScene().getWindow();
        newMainApp.start(stage);
    }

    // Metoda do wyświetlania alertów
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Metoda do czyszczenia pól formularza
    private void clearFields() {
        emailField.clear();
        loginField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        nameField.clear();
        surnameField.clear();
        occupationField.clear();
    }

}

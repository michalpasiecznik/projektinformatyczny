package com.example.iie_szarzy_2024;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField; // Pole tekstowe dla nazwy użytkownika

    @FXML
    private PasswordField passwordField; // Pole tekstowe dla hasła

    @FXML
    private Label messageLabel; // Etykieta dla komunikatów

    private Main mainApp; // Referencja do głównej klasy aplikacji
    private Connection connection; // Połączenie z bazą danych


    // Metoda ustawiająca referencję do głównej klasy aplikacji
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    // Metoda inicjalizująca połączenie z bazą danych
    @FXML
    public void initialize() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sklepszary", "root", "");
        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas połączenia z bazą danych");
        }
    }

    // Metoda obsługująca zdarzenie przycisku logowania
    @FXML
    private void loginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            // Nazwa zalogowanego użytkownika
            String loggedInUsername;
            if (checkLogin("administratorzy", username, password)) {
                messageLabel.setText("Dane poprawne, wystąpił błąd aplikacji!");
                loggedInUsername = username;
                mainApp.loadTasksView("administrator", loggedInUsername);
            } else if (checkLogin("kierownicy", username, password)) {
                messageLabel.setText("Dane poprawne jako kierownik, wystąpił błąd aplikacji!");
                loggedInUsername = username;
                mainApp.loadTasksView("kierownik", loggedInUsername);
            } else if (checkLogin("pracownicy", username, password)) {
                messageLabel.setText("Dane poprawne jako pracownik, wystąpił błąd aplikacji!");
                loggedInUsername = username;
                mainApp.loadTasksView("pracownik", loggedInUsername);
            } else {
                messageLabel.setText("Błędne dane logowania.");
            }
        } catch (Exception e) {
            messageLabel.setText("Wystąpił błąd podczas logowania.");
        }
    }

    // Metoda sprawdzająca dane logowania w bazie danych
    public boolean checkLogin(String table, String username, String password) throws Exception {
        String query = "SELECT * FROM " + table + " WHERE Login = ? AND Haslo = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    // Metoda obsługująca zdarzenie przycisku rejestracji
    @FXML
    private void registerButtonAction() {
        mainApp.loadRegisterView();
    }

    // Metoda obsługująca zdarzenie przycisku przypomnienia hasła
    @FXML
    private void remindButtonAction() {
        mainApp.loadRemindView();
    }

}

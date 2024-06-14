package com.example.iie_szarzy_2024;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private int roleID; // ID roli zalogowanego użytkownika

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    // Metoda ustawiająca referencję do głównej klasy aplikacji
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    // Metoda inicjalizująca połączenie z bazą danych
    @FXML
    public void initialize() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sklepszary2", "root", "");
            logger.info("Połączenie z bazą danych zostało nawiązane");
        } catch (Exception e) {
            logger.error("Wystąpił błąd podczas połączenia z bazą danych", e);
        }
    }

    // Metoda obsługująca zdarzenie przycisku logowania
    @FXML
    private void loginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            logger.info("Próba logowania użytkownika: {}", username);

            // Nazwa zalogowanego użytkownika
            String loggedInUsername;
            if (checkLogin(username, password)) {
                String roleName = getRoleName(roleID);
                messageLabel.setText("Dane poprawne, wystąpił błąd aplikacji!");
                loggedInUsername = username;
                mainApp.loadTasksView(roleName, loggedInUsername);
                logger.info("Użytkownik {} zalogowany jako {}", username, roleName);
            } else {
                messageLabel.setText("Błędne dane logowania.");
                logger.warn("Nieudana próba logowania użytkownika: {}", username);
            }
        } catch (Exception e) {
            messageLabel.setText("Wystąpił błąd podczas logowania.");
            logger.error("Wystąpił błąd podczas logowania użytkownika: {}", username, e);
        }
    }

    // Metoda sprawdzająca dane logowania w bazie danych
    public boolean checkLogin(String username, String password) {
        String query = "SELECT * FROM pracownicy WHERE Login = ? AND Haslo = ? AND StatusKonta ='Aktywne'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    roleID = resultSet.getInt("IDRoli");
                    return true; // Zwracanie true gdy znaleziono pasujący rekord
                } else {
                    messageLabel.setText("Błędne dane logowania.");
                    logger.warn("Nieudana próba logowania użytkownika: {}", username);
                    return false; // Zwracanie false gdy nie znaleziono pasującego rekordu
                }
            }
        } catch (Exception e) {
            messageLabel.setText("Wystąpił błąd podczas logowania.");
            logger.error("Wystąpił błąd podczas logowania użytkownika: {}", username, e);
            return false; // Zwracanie false w przypadku błędu
        }
    }

    // Metoda zwracająca nazwę roli na podstawie jej ID
    private String getRoleName(int roleID) {
        String roleName = "";
        switch (roleID) {
            case 1:
                roleName = "pracownik";
                break;
            case 2:
                roleName = "kierownik";
                break;
            case 3:
                roleName = "administrator";
                break;
            default:
                logger.warn("Nieznana rola użytkownika o ID: {}", roleID);
        }
        return roleName;
    }

    // Metoda obsługująca zdarzenie przycisku rejestracji
    @FXML
    private void registerButtonAction() {
        logger.info("Przełączanie do widoku rejestracji");
        mainApp.loadRegisterView();
    }

    // Metoda obsługująca zdarzenie przycisku przypomnienia hasła
    @FXML
    private void remindButtonAction() {
        logger.info("Przełączanie do widoku przypomnienia hasła");
        mainApp.loadRemindView();
    }
}

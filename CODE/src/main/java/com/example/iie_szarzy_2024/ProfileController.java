package com.example.iie_szarzy_2024;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;

public class ProfileController {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "sklepszary2";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @FXML
    public Label nameLabel;

    @FXML
    public Label surnameLabel;

    @FXML
    public Label emailLabel;

    @FXML
    public Label loginLabel;

    @FXML
    public Label occupationLabel;
    @FXML
    public Label hiredateLabel;

    @FXML
    private Button closeButton;

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    // Zmienna przechowująca nazwę zalogowanego użytkownika
    private String loggedInUsername;

    // Metoda do inicjalizacji kontrolera z nazwą zalogowanego użytkownika
    public void initialize(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
        logger.info("Zalogowano użytkownika: {}", loggedInUsername);

        Profile profile = getProfile();
        if (profile != null) {
            nameLabel.setText(profile.name());
            surnameLabel.setText(profile.surname());
            emailLabel.setText(profile.email());
            loginLabel.setText(loggedInUsername);
            occupationLabel.setText(profile.occupation);
            hiredateLabel.setText(profile.hiredate);
            logger.info("Dane profilu załadowane pomyślnie dla użytkownika: {}", loggedInUsername);
        } else {
            logger.warn("Nie udało się pobrać danych profilu dla użytkownika: {}", loggedInUsername);
        }
    }

    // Metoda do pobierania danych profilu na podstawie zalogowanej nazwy użytkownika
    public Profile getProfile() {
        try (Connection connection = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASSWORD)) {
            logger.info("Połączenie z bazą danych zostało nawiązane");

            PreparedStatement preparedStatementEmployee= connection.prepareStatement("SELECT * FROM pracownicy WHERE login = ?");
            preparedStatementEmployee.setString(1, loggedInUsername);
            try (ResultSet resultSetEmployee = preparedStatementEmployee.executeQuery()) {
                if (resultSetEmployee.next()) {
                    String name = resultSetEmployee.getString("Imie");
                    String surname = resultSetEmployee.getString("Nazwisko");
                    String email = resultSetEmployee.getString("Email");
                    String occupation = resultSetEmployee.getString("Stanowisko");
                    String hiredate = resultSetEmployee.getString("DataZatrudnienia");
                    logger.info("Profil administratora znaleziony dla użytkownika: {}", loggedInUsername);
                    return new Profile(email, name, surname,occupation,hiredate);
                }
            }
        } catch (SQLException e) {
            logger.error("Wystąpił błąd podczas inicjalizacji bazy danych: {}", e.getMessage());
        }

        // Jeśli brak wyników w żadnej tabeli, zwróć null
        return null;
    }

    // Klasa reprezentująca profil użytkownika
    public record Profile(String email, String name, String surname, String occupation,String hiredate) {
    }

    // Metoda obsługująca zamknięcie okna
    @FXML
    private void handleCloseButtonClick() {
        // Pobranie referencji do sceny
        Stage stage = (Stage) closeButton.getScene().getWindow();
        // Zamknięcie okna
        stage.close();
        logger.info("Okno zostało zamknięte przez użytkownika");
    }
}
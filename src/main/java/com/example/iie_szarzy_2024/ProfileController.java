package com.example.iie_szarzy_2024;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Label;
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
    public Label IDLabel;

    @FXML
    private Button closeButton;


    // Zmienna przechowująca nazwę zalogowanego użytkownika
    private String loggedInUsername;

    // Metoda do inicjalizacji kontrolera z nazwą zalogowanego użytkownika
    public void initialize(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
        System.out.println(loggedInUsername);
        // Teraz można użyć loggedInUsername według potrzeb
        Profile profile = getProfile();
        if (profile != null) {
            IDLabel.setText(String.valueOf(profile.id()));
            nameLabel.setText(profile.name());
            surnameLabel.setText(profile.surname());
            emailLabel.setText(profile.email());
            loginLabel.setText(loggedInUsername);
        } else {
            System.out.println("Nie udało się pobrać danych profilu.");
        }
    }

    // Metoda do pobierania danych profilu na podstawie zalogowanej nazwy użytkownika
    public Profile getProfile() {
        try (Connection connection = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASSWORD)) {
            PreparedStatement preparedStatementAdmin = connection.prepareStatement("SELECT * FROM administratorzy WHERE login = ?");
            preparedStatementAdmin.setString(1, loggedInUsername);
            try (ResultSet resultSetAdmin = preparedStatementAdmin.executeQuery()) {
                if (resultSetAdmin.next()) {
                    int id = resultSetAdmin.getInt("IDAdministratora");
                    String name = resultSetAdmin.getString("Imie");
                    String surname = resultSetAdmin.getString("Nazwisko");
                    String email = resultSetAdmin.getString("Email");
                    return new Profile(id, name, surname, email);
                }
            }

            // Jeśli brak wyników w tabeli "administratorzy", sprawdź tabelę "pracownicy"
            PreparedStatement preparedStatementEmployee = connection.prepareStatement("SELECT * FROM pracownicy WHERE login = ?");
            preparedStatementEmployee.setString(1, loggedInUsername);
            try (ResultSet resultSetEmployee = preparedStatementEmployee.executeQuery()) {
                if (resultSetEmployee.next()) {
                    int id = resultSetEmployee.getInt("IDPracownika");
                    String name = resultSetEmployee.getString("Imie");
                    String surname = resultSetEmployee.getString("Nazwisko");
                    String email = resultSetEmployee.getString("Email");
                    return new Profile(id, name, surname, email);
                }
            }

            // Jeśli brak wyników w tabeli "pracownicy", sprawdź tabelę "kierownicy"
            PreparedStatement preparedStatementManager = connection.prepareStatement("SELECT * FROM kierownicy WHERE login = ?");
            preparedStatementManager.setString(1, loggedInUsername);
            try (ResultSet resultSetManager = preparedStatementManager.executeQuery()) {
                if (resultSetManager.next()) {
                    int id = resultSetManager.getInt("IDKierownika");
                    String name = resultSetManager.getString("Imie");
                    String surname = resultSetManager.getString("Nazwisko");
                    String email = resultSetManager.getString("Email");
                    return new Profile(id, name, surname, email);
                }
            }
        } catch (SQLException e) {
            System.out.println("Wystąpił błąd podczas inicjalizacji bazy danych.");
        }
        // Jeśli brak wyników w żadnej tabeli, zwróć null
        return null;
    }

    // Klasa reprezentująca profil użytkownika
    public record Profile(int id, String name, String surname, String email) {
    }

    // Metoda obsługująca zamknięcie okna
    @FXML
    private void handleCloseButtonClick() {
        // Pobranie referencji do sceny
        Stage stage = (Stage) closeButton.getScene().getWindow();
        // Zamknięcie okna
        stage.close();
    }
}

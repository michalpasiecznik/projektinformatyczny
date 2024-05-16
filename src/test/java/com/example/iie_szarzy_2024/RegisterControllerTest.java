package com.example.iie_szarzy_2024;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RegisterControllerTest {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/sklepszary";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection connection;
    private RegisterController registerController;

    @BeforeEach
    public void setUp() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        registerController = new RegisterController();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    public void testRegisterButtonActionWithValidData() throws SQLException {
        // Przygotowanie danych testowych
        registerController.emailField.setText("test@example.com");
        registerController.loginField.setText("test");
        registerController.passwordField.setText("password");
        registerController.confirmPasswordField.setText("password");
        registerController.nameField.setText("John");
        registerController.surnameField.setText("Doe");
        registerController.occupationField.setText("Developer");

        // Wywołanie metody testowanej
        registerController.registerButtonAction();

        // Sprawdzenie poprawności zapisu w bazie danych
        String query = "SELECT * FROM pracownicy WHERE Login='test' AND Email='test@example.com'";
        ResultSet resultSet = connection.createStatement().executeQuery(query);
        assertTrue(resultSet.next()); // Oczekujemy, że istnieje wpis w bazie danych z naszymi danymi
    }
}

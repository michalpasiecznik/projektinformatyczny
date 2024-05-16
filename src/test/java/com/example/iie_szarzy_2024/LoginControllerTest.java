package com.example.iie_szarzy_2024;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoginControllerTest {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sklepszary";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection connection;
    private LoginController loginController;

    @BeforeEach
    public void setUp() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        loginController = new LoginController();
        loginController.initialize(); // Inicjalizacja połączenia z bazą danych
    }

    @AfterEach
    public void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    public void testCheckLogin() throws Exception {
        assertTrue(loginController.checkLogin("administratorzy", "admin", "admin"));
        assertFalse(loginController.checkLogin("administratorzy", "admin", "wrongPassword"));
    }
}

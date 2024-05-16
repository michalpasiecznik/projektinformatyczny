package com.example.iie_szarzy_2024;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatabaseConnectionTest {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/sklepszary";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection connection;

    @BeforeEach
    public void setUp() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Nie udało się połączyć z bazą danych.");
        }
    }

    @AfterEach
    public void tearDown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                fail("Błąd podczas zamykania połączenia z bazą danych.");
            }
        }
    }

    @Test
    public void testDatabaseConnection() {
        assertNotNull(connection, "Połączenie z bazą danych nie zostało ustanowione.");
    }
}

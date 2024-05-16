package com.example.iie_szarzy_2024;

import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class TasksControllerTest {

    private static Connection connection;

    @BeforeAll
    static void setUp() throws SQLException {
        // Połączenie z bazą danych przed rozpoczęciem testów
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sklepszary", "root", "");
    }

    @AfterAll
    static void tearDown() throws SQLException {
        // Zamknięcie połączenia z bazą danych po zakończeniu testów
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testIfTaskWithNameNotExist() {
        try {
            // Sprawdzenie, czy istnieje zadanie o określonej nazwie w bazie danych
            String taskNameToCheck = "Nazwa_zadania_ktorego_nie_ma";
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM zadania WHERE Opis = ?");
            preparedStatement.setString(1, taskNameToCheck);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            // Sprawdzenie wyniku testu
            assertEquals(0, count, "Zadanie o podanej nazwie istnieje, a nie powinno.");
        } catch (SQLException e) {
            fail("Błąd podczas wykonania testu: " + e.getMessage());
        }
    }

    @Test
    void testIfExistingTaskWithNameExist() {
        try {
            // Sprawdzenie, czy istnieje zadanie o określonej nazwie w bazie danych
            String taskNameToCheck = "Ułożenie towaru na półkach";
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM zadania WHERE Opis = ?");
            preparedStatement.setString(1, taskNameToCheck);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            // Sprawdzenie wyniku testu
            assertTrue(count > 0, "Nie znaleziono zadania o podanej nazwie.");
        } catch (SQLException e) {
            fail("Błąd podczas wykonania testu: " + e.getMessage());
        }
    }
}

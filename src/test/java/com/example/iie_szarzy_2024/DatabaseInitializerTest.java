package com.example.iie_szarzy_2024;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseInitializerTest {

    private Connection connection;

    @BeforeEach
    public void setUp() {
        try {
            // Ustanowienie połączenia z bazą danych
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "");
        } catch (SQLException e) {
            fail("Błąd w łączeniu z bazą danych: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            // Kończenie połączenia po każdym wykonanym teście
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            fail("Błąd w zamknięciu połączenia z bazą danych: " + e.getMessage());
        }
    }

    @Test
    public void testDatabaseCreation() {
        try {
            // Sprawdzenie czy baza danych "sklepszary" istnieje
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getCatalogs();
            boolean databaseExists = false;
            while (resultSet.next()) {
                String dbName = resultSet.getString("TABLE_CAT");
                if ("sklepszary".equalsIgnoreCase(dbName)) {
                    databaseExists = true;
                    break;
                }
            }
            assertTrue(databaseExists, "Baza danych 'sklepszary' powinna istnieć.");
        } catch (SQLException e) {
            fail("Błąd w sprawdzeniu istnienia bazy danych: " + e.getMessage());
        }
    }

    @Test
    public void testTableCreation() {
        try {
            // Sprawdzenie czy utworzyły się tabele
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            boolean tablesExist = false;
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if ("administratorzy".equalsIgnoreCase(tableName)
                        || "kierownicy".equalsIgnoreCase(tableName)
                        || "pracownicy".equalsIgnoreCase(tableName)
                        || "zadania".equalsIgnoreCase(tableName)
                        || "pracownicyzadania".equalsIgnoreCase(tableName)) {
                    tablesExist = true;
                }
            }
            assertTrue(tablesExist, "Tabele powinny zostać utworzone.");
        } catch (SQLException e) {
            fail("Błąd w sprawdzeniu istnienia tabel: " + e.getMessage());
        }
    }
}

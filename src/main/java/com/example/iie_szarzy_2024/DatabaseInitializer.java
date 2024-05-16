package com.example.iie_szarzy_2024;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "sklepszary";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        try {
            // Utworzenie połączenia
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);

            // Utworzenie bazy danych
            Statement statement = connection.createStatement();
            String createDatabaseQuery = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            statement.executeUpdate(createDatabaseQuery);

            // Użycie nowej bazy danych
            String useDatabaseQuery = "USE " + DB_NAME;
            statement.executeUpdate(useDatabaseQuery);

            // Zapytania SQL z kluczami obcymi
            String[] sqlQueries = {
                    // Administratorzy
                    "CREATE TABLE `administratorzy` (`IDAdministratora` int(4) NOT NULL AUTO_INCREMENT, `Imie` varchar(16) NOT NULL, `Nazwisko` varchar(25) NOT NULL, `Login` varchar(25) NOT NULL, `Haslo` varchar(64) NOT NULL, `Email` varchar(25) NOT NULL, PRIMARY KEY (`IDAdministratora`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;",
                    "INSERT INTO `administratorzy` (`Imie`, `Nazwisko`, `Login`, `Haslo`, `Email`) VALUES ('Adam', 'Kowalski', 'admin', 'admin', 'adam@example.com'), ('Ewa', 'Nowak', 'ewan', 'password', 'ewa@example.com');",

                    // Kierownicy
                    "CREATE TABLE `kierownicy` (`IDKierownika` int(4) NOT NULL AUTO_INCREMENT, `Imie` varchar(25) NOT NULL, `Nazwisko` varchar(25) NOT NULL, `Login` varchar(16) NOT NULL, `Haslo` varchar(64) NOT NULL, `Email` varchar(25) NOT NULL, PRIMARY KEY (`IDKierownika`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;",
                    "INSERT INTO `kierownicy` (`Imie`, `Nazwisko`, `Login`, `Haslo`, `Email`) VALUES ('Jan', 'Wiśniewski', 'janw', 'pass123', 'jan@example.com'), ('Anna', 'Lewandowska', 'annal', 'pass456', 'anna@example.com');",

                    // Pracownicy
                    "CREATE TABLE `pracownicy` (`IDPracownika` int(4) NOT NULL AUTO_INCREMENT, `Imie` varchar(25) NOT NULL, `Nazwisko` varchar(25) NOT NULL, `Login` varchar(16) NOT NULL, `Haslo` varchar(64) NOT NULL, `Stanowisko` varchar(25) NOT NULL, `Email` varchar(25) NOT NULL, `DataZatrudnienia` date NOT NULL, `IDKierownika` int(4), PRIMARY KEY (`IDPracownika`), FOREIGN KEY (`IDKierownika`) REFERENCES `kierownicy`(`IDKierownika`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;",
                    "INSERT INTO `pracownicy` (`Imie`, `Nazwisko`, `Login`, `Haslo`, `Stanowisko`, `Email`, `DataZatrudnienia`, `IDKierownika`) VALUES ('Paweł', 'Nowak', 'pnowak', 'pass789', 'Magazynier', 'pawel@example.com', '2024-01-15', 1), ('Karolina', 'Kowalczyk', 'kkow', 'pass000', 'Kasjer', 'karolina@example.com', '2024-02-20', 1), ('Mateusz', 'Nowak', 'mnowak', 'pass111', 'Pracownik Sklepu', 'mateusz@example.com', '2023-11-10', 2), ('Katarzyna', 'Wójcik', 'kwoj', 'pass222', 'Księgowa', 'katarzyna@example.com', '2024-03-05', 2);",

                    // Zadania
                    "CREATE TABLE `zadania` (`IDZadania` int(4) NOT NULL AUTO_INCREMENT, `Opis` varchar(100) NOT NULL, `Status` varchar(50) NOT NULL, `TerminWykonania` datetime NOT NULL, `DataUtworzenia` datetime NOT NULL, `DataModyfikacji` datetime NOT NULL, `IDKierownika` int(4) NOT NULL, PRIMARY KEY (`IDZadania`), FOREIGN KEY (`IDKierownika`) REFERENCES `kierownicy`(`IDKierownika`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;",
                    "INSERT INTO `zadania` (`Opis`, `Status`, `TerminWykonania`, `DataUtworzenia`, `DataModyfikacji`, `IDKierownika`) VALUES ('Ułożenie towaru na półkach', 'W toku', '2024-05-01 08:00:00', '2024-04-10 10:00:00', '2024-04-15 14:30:00', 1), ('Przyjęcie zaopatrzenia sklepu', 'Zakończone', '2024-04-20 12:00:00', '2024-04-05 09:00:00', '2024-04-18 16:45:00', 1), ('Policzenie utargu', 'Oczekujące', '2024-06-01 08:00:00', '2024-03-20 11:30:00', '2024-03-25 15:20:00', 2), ('Rozliczenie podatkowe', 'W toku', '2024-04-30 17:00:00', '2024-02-15 08:00:00', '2024-04-10 12:10:00', 2);",

                    // PracownicyZadania
                    "CREATE TABLE `pracownicyzadania` (`IDPracownika` int(4) NOT NULL, `IDZadania` int(4) NOT NULL, FOREIGN KEY (`IDPracownika`) REFERENCES `pracownicy`(`IDPracownika`), FOREIGN KEY (`IDZadania`) REFERENCES `zadania`(`IDZadania`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;",
                    "INSERT INTO `pracownicyzadania` (`IDPracownika`, `IDZadania`) VALUES (1, 1), (2, 2), (3, 3), (4, 4);"
            };

            for (String query : sqlQueries) {
                statement.executeUpdate(query);
            }

            System.out.println("Baza danych została utworzona i zainicjowana pomyślnie.");

            // Zamknięcie połączenia i zasobów
            statement.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas inicjalizacji bazy danych.");
        }
    }
}

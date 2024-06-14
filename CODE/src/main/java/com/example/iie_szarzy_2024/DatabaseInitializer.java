package com.example.iie_szarzy_2024;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "sklepszary2";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void main(String[] args) {
        try {
            // Utworzenie połączenia
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            logger.info("Połączenie z bazą danych zostało nawiązane");

            // Utworzenie bazy danych
            Statement statement = connection.createStatement();
            String createDatabaseQuery = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            statement.executeUpdate(createDatabaseQuery);
            logger.info("Baza danych '{}' została utworzona lub już istnieje", DB_NAME);

            // Użycie nowej bazy danych
            String useDatabaseQuery = "USE " + DB_NAME;
            statement.executeUpdate(useDatabaseQuery);
            logger.info("Baza danych '{}' została wybrana do użycia", DB_NAME);

            // Zapytania SQL z kluczami obcymi
            String[] sqlQueries = {
                    // Pracownicy
                    "CREATE TABLE `pracownicy` (`IDPracownika` int(4) NOT NULL AUTO_INCREMENT, `Imie` varchar(25) NOT NULL, `Nazwisko` varchar(25) NOT NULL, `Login` varchar(16) NOT NULL, `Haslo` varchar(64) NOT NULL, `Stanowisko` varchar(25) NOT NULL, `Email` varchar(25) NOT NULL, `StatusKonta` varchar(25) NOT NULL, `DataZatrudnienia` date NOT NULL, `IDRoli` int(1), PRIMARY KEY (`IDPracownika`))  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;",
                    "INSERT INTO `pracownicy` (`Imie`, `Nazwisko`, `Login`, `Haslo`, `Stanowisko`, `Email`, `StatusKonta`, `DataZatrudnienia`, `IDRoli`) VALUES ('Paweł', 'Nowak', 'pnowak', 'pass789', 'Magazynier', 'pawel@example.com', 'Aktywne', '2024-01-15', 1), ('Karolina', 'Kowalczyk', 'kkow', 'pass000', 'Kasjer', 'karolina@example.com', 'Nieaktywne', '2024-02-20', 1), ('Mateusz', 'Nowak', 'mnowak', 'pass111', 'Pracownik Sklepu', 'mateusz@example.com', 'Aktywne', '2023-11-10', 1), ('Katarzyna', 'Wójcik', 'kwoj', 'pass222', 'Księgowa', 'katarzyna@example.com', 'Aktywne', '2024-03-05', 1), ('Jan', 'Wiśniewski', 'janw', 'pass123','Kierownik' ,'jan@example.com', 'Aktywne', '2024-02-20',2), ('Anna', 'Lewandowska', 'annal', 'pass456','Kierownik', 'anna@example.com', 'Aktywne', '2024-02-20',2),('Adam', 'Kowalski', 'admin', 'admin','Starszy Administrator', 'adam@example.com', 'Aktywne', '2024-02-20',3), ('Ewa', 'Nowak', 'ewan', 'password','Młodszy Administrator', 'ewa@example.com', 'Aktywne', '2024-02-20',3);",

                    // Zadania
                    "CREATE TABLE `zadania` (`IDZadania` int(4) NOT NULL AUTO_INCREMENT, `Opis` varchar(100) NOT NULL, `Status` varchar(50) NOT NULL,`Priorytet` varchar(50) NOT NULL,`Kategoria` varchar(50) NOT NULL, `DataUtworzenia` datetime NOT NULL, `DataRozpoczecia` datetime NOT NULL, `DataModyfikacji` datetime NOT NULL,`DataZakonczenia` datetime NOT NULL, `IDPracownika` int(4) NOT NULL, PRIMARY KEY (`IDZadania`), FOREIGN KEY (`IDPracownika`) REFERENCES `pracownicy`(`IDPracownika`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;",
                    "INSERT INTO `zadania` (`Opis`, `Status`,`Priorytet`, `Kategoria`, `DataUtworzenia`, `DataRozpoczecia`, `DataModyfikacji`, `DataZakonczenia`, `IDPracownika`) VALUES ('Ułożenie towaru na półkach', 'W toku','Wysoki','Sklep', '2024-05-01 8:00:00', '2024-04-10 10:00:00', '2024-04-15 14:30:00', '2024-04-10 14:00:00', 1), ('Przyjęcie zaopatrzenia sklepu', 'Zakończone','Średni','Sklep', '2024-04-20 12:00:00', '2024-04-05 09:00:00', '2024-04-18 15:30:00', '2024-04-05 12:00:00', 2), ('Policzenie utargu', 'Oczekujące','Niski','Sklep', '2024-06-01 08:00:00', '2024-03-20 11:30:00', '2024-03-25 15:15:00','2024-04-10 15:00:00', 3), ('Rozliczenie podatkowe', 'W toku','Wysoki','Sklep', '2024-04-30 15:00:00', '2024-02-15 08:00:00', '2024-04-10 12:15:00','2024-04-10 10:00:00', 4);",

                    // PracownicyZadania
                    "CREATE TABLE `pracownicyzadania` (`IDPracownika` int(4) NOT NULL, `IDZadania` int(4) NOT NULL, FOREIGN KEY (`IDPracownika`) REFERENCES `pracownicy`(`IDPracownika`), FOREIGN KEY (`IDZadania`) REFERENCES `zadania`(`IDZadania`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;",
                    "INSERT INTO `pracownicyzadania` (`IDPracownika`, `IDZadania`) VALUES (1, 1), (2, 2), (3, 3), (4, 4);",
                    // Sklep
                    "CREATE TABLE `produkty` (`IDProduktu` int(10) NOT NULL AUTO_INCREMENT, `Nazwa` varchar(25) NOT NULL, `IDProducenta` int(10) NOT NULL,`Ilosc` decimal(6,2) NOT NULL, `Jednostka` varchar(25) NOT NULL,`Kod` varchar(13) NOT NULL, PRIMARY KEY (`IDProduktu`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;",
                    "INSERT INTO `produkty` (`Nazwa`, `IDProducenta`,`Ilosc`, `Jednostka`, `Kod`) VALUES ('Lays Solone','1','20', 'Sztk.', '5900259128652'), ('Mirinda','1','24','Sztk.', '5900497331500'),('Czekolada Milka Biala','2','30','Sztk.', '7622201121068'),('Chrupki Cheetos','1','14','Sztk.', '0000000000000'),('Napoj Coca-Cola','3','40','Sztk.', '0000000000000'),('Baton Kinder Bueno','4','15','Sztk.', '0000000000000'),('Kinder Niespodzianka','4','120','Sztk.', '0000000000000'),('Mleko 3.0%','5','30','Sztk.', '0000000000000'),('Pizza Giuseppe','6','27','Sztk.', '0000000000000'),('Ser Zolty','5','35','Kg.', '0000000000000');",

                    // Producenci
                    "CREATE TABLE `producenci` (`IDProducenta` int(10) NOT NULL AUTO_INCREMENT, `Nazwa` varchar(25) NOT NULL, PRIMARY KEY (`IDProducenta`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;",
                    "INSERT INTO `producenci` (`Nazwa`) VALUES ('PEPSICO'),('Mondelez'),('The Coca-Cola Company'),('Kinder'),('Mlekovita'),('Dr.Oetker');",

                    // Tabela łącząca producentów z produktami
                    "CREATE TABLE `producenciprodukty` (`IDProducenta` int(10) NOT NULL, `IDProduktu` int(10) NOT NULL, PRIMARY KEY (`IDProducenta`, `IDProduktu`), FOREIGN KEY (`IDProducenta`) REFERENCES `producenci`(`IDProducenta`), FOREIGN KEY (`IDProduktu`) REFERENCES `produkty`(`IDProduktu`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;",
                    "INSERT INTO `producenciprodukty` (`IDProducenta`,`IDProduktu`) VALUES (1,1),(1,2),(2,3),(1,4),(3,5),(4,6),(4,7),(5,8),(6,9),(5,10);",
            };

            for (String query : sqlQueries) {
                statement.executeUpdate(query);
                logger.info("Wykonano zapytanie: {}", query);
            }

            System.out.println("Baza danych została utworzona i zainicjowana pomyślnie.");
            logger.info("Baza danych została utworzona i zainicjowana pomyślnie");

            // Zamknięcie połączenia i zasobów
            statement.close();
            connection.close();
            logger.info("Połączenie z bazą danych zostało zamknięte");

        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas inicjalizacji bazy danych.");
            logger.error("Wystąpił błąd podczas inicjalizacji bazy danych: {}", e.getMessage());
        }
    }
}

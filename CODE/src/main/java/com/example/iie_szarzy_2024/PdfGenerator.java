package com.example.iie_szarzy_2024;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.UnitValue;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PdfGenerator {
    Connection connection;

    public void manipulatePdf(String dest, Integer doctype, Integer filtertype, String filterphrase) throws Exception {
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
            Document doc = new Document(pdfDoc);
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sklepszary2", "root", "");

            if (doctype == 1) {
                generateTasksReport(doc, filtertype, filterphrase);
            } else if (doctype == 2) {
                generateUserReport(doc, filtertype, filterphrase);
            }

            doc.close();
            showAlertpdf("Plik PDF został pomyślnie utworzony: ","Plik PDF został pomyślnie utworzony:" + dest);
        } catch (Exception e) {
            e.printStackTrace(); // Wyświetlenie pełnego śladu stosu błędu
            showAlert("Wystąpił błąd podczas tworzenia dokumentu PDF: " + e.getMessage());
        }
    }

    public void manipulatePdfProduct(String dest, String filtertype, String filterphrase) throws Exception {
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
            Document doc = new Document(pdfDoc);
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sklepszary", "root", "");

            generateProductReport(doc, filtertype, filterphrase);

            doc.close();
            showAlertpdf("Plik PDF został pomyślnie utworzony: " ,"Plik PDF został pomyślnie utworzony:" + dest );
        } catch (Exception e) {
            e.printStackTrace(); // Wyświetlenie pełnego śladu stosu błędu
            showAlert("Wystąpił błąd podczas tworzenia dokumentu PDF: " + e.getMessage());
        }
    }

    private void generateTasksReport(Document doc, Integer filtertype, String filterphrase) throws Exception {
        String query = "SELECT * FROM Zadania WHERE Status != 'Archiwum'";
        if (filtertype != null && filterphrase != null && !filterphrase.isEmpty()) {
            if (filtertype == 1) {
                query += " AND Status = ?";
            } else if (filtertype == 2){
                query += " AND Priorytet = ?";
            } else if (filtertype == 3){
                query += " AND Kategoria = ?";
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            if (filtertype != null && filterphrase != null && !filterphrase.isEmpty()) {
                preparedStatement.setString(1, filterphrase);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            Table table = new Table(UnitValue.createPercentArray(new float[]{20, 20, 20, 20, 20, 20, 20, 20}));
            Style style = new Style()
                    .setFontSize(25)
                    .setFontColor(ColorConstants.RED)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY);

            Paragraph paragraph = new Paragraph()
                    .add(new Text("Spis Zadan").addStyle(style));
            doc.add(paragraph);
            table.addCell("L.P");
            table.addCell("Opis");
            table.addCell("Status");
            table.addCell("Priorytet");
            table.addCell("Kategoria");
            table.addCell("Termin Wykonania");
            table.addCell("Data Utworzenia");
            table.addCell("Data Modyfikacji");

            while (resultSet.next()) {
                table.addCell(String.valueOf(resultSet.getInt("IDZadania")));
                table.addCell(resultSet.getString("Opis"));
                if(resultSet.getString("Status").equals("Zakończone")) {
                    table.addCell("Zakonczone");
                } else if (resultSet.getString("Status").equals("Oczekujące")){
                    table.addCell("Oczekujace");
                } else {
                    table.addCell(resultSet.getString("Status"));
                }
                if(resultSet.getString("Priorytet").equals("Średni")) {
                    table.addCell("Sredni");
                } else {
                    table.addCell(resultSet.getString("Priorytet"));
                }
                table.addCell(resultSet.getString("Kategoria"));
                table.addCell(resultSet.getString("TerminWykonania"));
                table.addCell(resultSet.getString("DataUtworzenia"));
                table.addCell(resultSet.getString("DataModyfikacji"));
            }
            doc.add(table);

        } catch (Exception e) {
            e.printStackTrace(); // Wyświetlenie pełnego śladu stosu błędu
            showAlert("Wystąpił błąd podczas inicjalizacji bazy danych: " + e.getMessage());
        }
    }

    private void generateUserReport(Document doc, Integer filtertype, String filterphrase) throws Exception {
        if (filtertype == 1) {
            // Generate combined report
            addUserTable(doc, "Tabela Pracowników", null);
        } else if (filtertype == 2) {
            // Generate specific table report
            if (filterphrase.equalsIgnoreCase("administratorzy") ||
                    filterphrase.equalsIgnoreCase("kierownicy") ||
                    filterphrase.equalsIgnoreCase("pracownicy")) {
                int roleId = getRoleId(filterphrase);
                addUserTable(doc, "Tabela " + capitalize(filterphrase), roleId);
            } else {
                throw new IllegalArgumentException("Invalid table name provided.");
            }
        } else {
            throw new IllegalArgumentException("Invalid filter type provided.");
        }
    }

    private int getRoleId(String roleName) {
        switch (roleName.toLowerCase()) {
            case "pracownicy":
                return 1;
            case "kierownicy":
                return 2;
            case "administratorzy":
                return 3;
            default:
                throw new IllegalArgumentException("Invalid role name provided.");
        }
    }

    private void addUserTable(Document doc, String tableTitle, Integer roleId) throws Exception {
        String query = "SELECT IDPracownika, Imie, Nazwisko, Email, DataZatrudnienia, Stanowisko FROM pracownicy";
        if (roleId != null) {
            query += " WHERE IDRoli = " + roleId;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            Table table = new Table(UnitValue.createPercentArray(new float[]{10, 15, 15, 20, 20, 20})); // Adjust column widths
            Style style = new Style()
                    .setFontSize(25)
                    .setFontColor(ColorConstants.RED)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY);

            Paragraph paragraph = new Paragraph()
                    .add(new Text(tableTitle).addStyle(style));
            doc.add(paragraph);

            // Adding headers
            table.addCell("L.P");
            table.addCell("Imie");
            table.addCell("Nazwisko");
            table.addCell("E-Mail");
            table.addCell("Data Zatrudnienia");
            table.addCell("Stanowisko");

            int index = 1;
            while (resultSet.next()) {
                table.addCell(String.valueOf(index++)); // L.P
                table.addCell(resultSet.getString("Imie"));
                table.addCell(resultSet.getString("Nazwisko"));
                table.addCell(resultSet.getString("Email"));
                table.addCell(resultSet.getString("DataZatrudnienia"));
                table.addCell(resultSet.getString("Stanowisko"));
            }
            doc.add(table);
        } catch (Exception e) {
            e.printStackTrace(); // Wyświetlenie pełnego śladu stosu błędu
            showAlert("Wystąpił błąd podczas inicjalizacji bazy danych: " + e.getMessage());
        }
    }

    private void generateProductReport(Document doc, String filterType, String filterPhrase) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT p.IDProduktu, p.Nazwa AS NazwaProduktu, p.Ilosc, p.Jednostka, pr.Nazwa AS NazwaProducenta, p.Kod FROM produkty p");
        queryBuilder.append(" JOIN producenci pr ON p.IDProducenta = pr.IDProducenta");
        queryBuilder.append(" WHERE 1=1");

        // Sprawdzenie, czy wybrano filtr IDProducenta i czy fraza nie jest pusta
        if ("Nazwa Producenta".equalsIgnoreCase(filterType) && filterPhrase != null && !filterPhrase.isEmpty()) {
            queryBuilder.append(" AND pr.Nazwa LIKE ?");
        }

        // Sprawdzenie, czy wybrano filtr Jednostka i czy fraza nie jest pusta
        if ("Jednostka".equalsIgnoreCase(filterType) && filterPhrase != null && !filterPhrase.isEmpty()) {
            queryBuilder.append(" AND p.Jednostka = ?");
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
            int paramIndex = 1; // Indeks parametru w zapytaniu SQL

            // Ustawienie parametru dla filtru IDProducenta, jeśli został wybrany
            if ("Nazwa Producenta".equalsIgnoreCase(filterType) && filterPhrase != null && !filterPhrase.isEmpty()) {
                preparedStatement.setString(paramIndex++, "%" + filterPhrase + "%");
            }

            // Ustawienie parametru dla filtru Jednostka, jeśli został wybrany
            if ("Jednostka".equalsIgnoreCase(filterType) && filterPhrase != null && !filterPhrase.isEmpty()) {
                preparedStatement.setString(paramIndex, filterPhrase);
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            Table table = new Table(UnitValue.createPercentArray(new float[]{10, 20, 20, 10, 10, 15}));
            Style style = new Style()
                    .setFontSize(25)
                    .setFontColor(ColorConstants.RED)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY);

            Paragraph paragraph = new Paragraph()
                    .add(new Text("Raport Produktów").addStyle(style));
            doc.add(paragraph);

            table.addCell("L.P.");
            table.addCell("Nazwa Produktu");
            table.addCell("Ilosc");
            table.addCell("Jednostka");
            table.addCell("Nazwa Producenta");
            table.addCell("Kod");

            int lp = 1; // Licznik do numeracji wierszy
            while (resultSet.next()) {
                table.addCell(String.valueOf(lp++));
                table.addCell(resultSet.getString("NazwaProduktu"));
                table.addCell(resultSet.getString("Ilosc"));
                table.addCell(resultSet.getString("Jednostka"));
                table.addCell(resultSet.getString("NazwaProducenta"));
                table.addCell(resultSet.getString("Kod"));
            }
            doc.add(table);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Wystąpił błąd podczas inicjalizacji bazy danych: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        System.out.println(message);
    }

    private void showAlertpdf(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}

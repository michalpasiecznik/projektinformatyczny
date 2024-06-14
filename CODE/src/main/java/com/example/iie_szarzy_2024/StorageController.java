package com.example.iie_szarzy_2024;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.util.Pair;
import javafx.util.StringConverter;
import javafx.util.converter.LocalTimeStringConverter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StorageController {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public Button checkButton;


    @FXML
    private Label titleLabel;

    @FXML
    Connection connection;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    private String loggedInUsername;

    private Main mainApp;

    @FXML
    private TableView<Product> productTableView;
    @FXML
    private TableColumn<Product, Integer> productIdColumn;
    @FXML
    private TableColumn<Product, String> productNameColumn;
    @FXML
    private TableColumn<Product, String> productProducerColumn;
    @FXML
    private TableColumn<Product, String> productQuantityColumn;
    @FXML
    private TableColumn<Product, String> productUnitColumn;
    @FXML
    private TableColumn<Product, String> productCodeColumn;
    private String userType;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

    @FXML
    public void setLoggedInUserType(String userType) {
        this.userType = userType;
        // Logika dostosowująca wyświetlane informacje w zależności od zalogowanego użytkownika
        if ("administrator".equals(userType)) {
            titleLabel.setText("Produkty | Jesteś administratorem");
        } else if ("kierownik".equals(userType)) {
            titleLabel.setText("Produkty | Jesteś kierownikiem");
        } else if ("pracownik".equals(userType)) {
            titleLabel.setText("Produkty | Jesteś pracownikem");
        }
    }


    @FXML
    public void initialize() {
        try {
            productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            productProducerColumn.setCellValueFactory(new PropertyValueFactory<>("producer"));
            productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            productUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
            productCodeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));

            // Pozwolenie Kolumnom na rozszerzenie
            productIdColumn.setResizable(true);
            productNameColumn.setResizable(true);
            productProducerColumn.setResizable(true);
            productQuantityColumn.setResizable(true);
            productUnitColumn.setResizable(true);
            productCodeColumn.setResizable(true);

            // Włączanie równego rozkładu kolumn
            productTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            // Nawiązanie połączenia z bazą danych
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sklepszary2", "root", "");
            // Ładowanie produktów z bazy danych
            loadProductsFromDatabase();
        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas inicjalizacji bazy danych.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void loadProductsFromDatabase() {
        try {
            String query = "SELECT p.IDProduktu, p.Nazwa AS NazwaProduktu, p.IDProducenta, p.Ilosc, p.Jednostka, p.Kod, pr.Nazwa AS NazwaProducenta" +
                    " FROM produkty p " +
                    "JOIN producenci pr ON p.IDProducenta = pr.IDProducenta;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<Product> products = new ArrayList<>();

            while (resultSet.next()) {
                int productID = resultSet.getInt("IDProduktu");
                String productName = resultSet.getString("NazwaProduktu");
                String producer = resultSet.getString("NazwaProducenta");
                double quantity = resultSet.getDouble("Ilosc");
                String unit = resultSet.getString("Jednostka");
                String code = resultSet.getString("Kod");

                // Tworzenie obiektu produktu
                Product product = new Product(productID, productName, producer, quantity, unit, code);
                products.add(product);
            }

            // Ustawienie danych produktów w tabeli
            productTableView.getItems().setAll(products);
            logger.info("Załadowano produkty z bazy danych.");
            preparedStatement.close();
        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas ładowania produktów z bazy danych.");
            logger.error("Wystąpił błąd podczas ładowania produktów z bazy danych: {}", e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleAddProduct() {
        try {
            // Wyświetlenie okna dialogowego z formularzem dodawania produktu
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Dodaj nowy produkt");
            dialog.setHeaderText("Dodawanie nowego produktu");

            // Dodanie kontrolek do formularza dialogowego
            Label productNameLabel = new Label("Nazwa Produktu:");
            TextField productNameTextField = new TextField();
            productNameTextField.setPromptText("Nazwa Produktu");

            Label productProducerLabel = new Label("Producent Produktu:");
            TextField productProducerTextField = new TextField(); // Pole tekstowe dla nazwy producenta
            productProducerTextField.setPromptText("Wprowadź nazwę producenta");

            Label productQuantityLabel = new Label("Ilość Produktu:");
            TextField productQuantityTextField = new TextField();
            productQuantityTextField.setPromptText("Ilość Produktu");

            Label productUnitLabel = new Label("Jednostka Produktu:");
            ComboBox<String> productUnitComboBox = new ComboBox<>();
            productUnitComboBox.getItems().addAll("Sztk.", "Kg.", "Opakowań");
            productUnitComboBox.setPromptText("Jednostka Produktu");

            Label productCodeLabel = new Label("Kod Kreskowy Produktu:");
            TextField productCodeTextField = new TextField();
            productCodeTextField.setPromptText("Kod Kreskowy Produktu:");

            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(
                    productNameLabel, productNameTextField,
                    productProducerLabel, productProducerTextField,
                    productQuantityLabel, productQuantityTextField,
                    productUnitLabel, productUnitComboBox,
                    productCodeLabel, productCodeTextField
            );
            dialog.getDialogPane().setContent(vbox);

            // Dodanie przycisków do formularza dialogowego
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Obsługa zdarzenia zatwierdzenia formularza
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return String.join(";",
                            productNameTextField.getText(),
                            productProducerTextField.getText(),
                            productQuantityTextField.getText(),
                            productUnitComboBox.getValue(),
                            productCodeTextField.getText()
                    );
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String[] parts = result.get().split(";");
                String productName = parts[0];
                String productProducer = parts[1];
                String productQuantityString = parts[2];
                String productUnit = parts[3];
                String productCode = parts[4];

                // Walidacja danych
                if (productName.isEmpty() || productProducer.isEmpty() || productQuantityString.isEmpty() ||
                         productUnit == null || productCode.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Błąd walidacji", "Wszystkie pola muszą być wypełnione.");
                    return;
                }

                double productQuantity;
                try {
                    if ("Kg.".equals(productUnit)) {
                        productQuantity = Double.parseDouble(productQuantityString);
                    } else {
                        productQuantity = Integer.parseInt(productQuantityString);
                    }
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Błąd walidacji", "Ilość produktu musi być liczbą.");
                    return;
                }

                if (!productCode.matches("\\d{13}")) {
                    showAlert(Alert.AlertType.ERROR, "Błąd walidacji", "Kod kreskowy musi mieć dokładnie 13 cyfr.");
                    return;
                }

                // Potwierdzenie dodania produktu
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Potwierdzenie dodania");
                confirmationAlert.setHeaderText("Dodawanie nowego produktu");
                confirmationAlert.setContentText("Czy na pewno chcesz dodać nowy produkt: " + productName + "?");

                Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    // Pobranie ID Producenta na podstawie wybranej nazwy producenta
                    String getProducerIdQuery = "SELECT IDProducenta FROM producenci WHERE Nazwa = ?";
                    PreparedStatement getProducerIdStatement = connection.prepareStatement(getProducerIdQuery);
                    getProducerIdStatement.setString(1, productProducer);
                    ResultSet producerIdResultSet = getProducerIdStatement.executeQuery();
                    int productProducerId;
                    if (producerIdResultSet.next()) {
                        productProducerId = producerIdResultSet.getInt("IDProducenta");
                    } else {
                        // Jeśli producent o podanej nazwie nie istnieje, dodaj go do bazy danych
                        String addProducerQuery = "INSERT INTO producenci (Nazwa) VALUES (?)";
                        PreparedStatement addProducerStatement = connection.prepareStatement(addProducerQuery, Statement.RETURN_GENERATED_KEYS);
                        addProducerStatement.setString(1, productProducer);
                        addProducerStatement.executeUpdate();

                        ResultSet generatedKeys = addProducerStatement.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            productProducerId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Nie udało się dodać nowego producenta, brak wygenerowanego ID.");
                        }
                    }

                    // Dodanie nowego produktu do bazy danych
                    String addProductQuery = "INSERT INTO produkty (Nazwa, IDProducenta, Ilosc, Jednostka, Kod) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement addProductStatement = connection.prepareStatement(addProductQuery);
                    addProductStatement.setString(1, productName);
                    addProductStatement.setInt(2, productProducerId);
                    addProductStatement.setDouble(3, productQuantity);
                    addProductStatement.setString(4, productUnit);
                    addProductStatement.setString(5, productCode);
                    addProductStatement.executeUpdate();
                    addProductStatement.close();

                    // Aktualizacja interfejsu użytkownika (Pozostaje bez zmian)
                    loadProductsFromDatabase();

                    logger.info("Dodano nowy produkt: {}", productName);
                }
            }
        } catch (SQLException e) {
            System.out.println("Wystąpił błąd podczas dodawania produktu do bazy danych: " + e.getMessage());
            logger.error("Wystąpił błąd podczas dodawania produktu: {}", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }



    @FXML
    private void handleEditProduct() {
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            try {
                int productID = selectedProduct.getId();

                String query = "SELECT * FROM produkty WHERE IDProduktu = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, productID);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            String edtproductName = resultSet.getString("Nazwa");
                            int producerID = resultSet.getInt("IDProducenta");
                            String edtproducer = getProducerNameByID(producerID);
                            double edtquantity = resultSet.getDouble("Ilosc");
                            String edtunit = resultSet.getString("Jednostka");
                            String edtcode = resultSet.getString("Kod");

                            // Wyświetlenie okna dialogowego z formularzem edycji produktu
                            Dialog<String> dialog = new Dialog<>();
                            dialog.setTitle("Edytuj Produkt");
                            dialog.setHeaderText("Edycja Produktu o ID: " + productID);

                            // Dodanie kontrolek do formularza dialogowego
                            Label productNameLabel = new Label("Nazwa Produktu:");
                            TextField productNameTextField = new TextField(edtproductName);

                            Label productProducerLabel = new Label("Producent Produktu:");
                            ComboBox<String> productProducerComboBox = new ComboBox<>();
                            // Pobranie listy nazw producentów z bazy danych i dodanie ich do ComboBox
                            String producerQuery = "SELECT Nazwa FROM producenci";
                            PreparedStatement producerPreparedStatement = connection.prepareStatement(producerQuery);
                            ResultSet producerResultSet = producerPreparedStatement.executeQuery();
                            while (producerResultSet.next()) {
                                productProducerComboBox.getItems().add(producerResultSet.getString("Nazwa"));
                            }
                            productProducerComboBox.setValue(edtproducer); // Ustawienie wartości z bazy danych

                            Label productQuantityLabel = new Label("Ilość Produktu:");
                            Label currentQuantityLabel = new Label(String.valueOf(edtquantity));

                            Label changeQuantityLabel = new Label("Wartość zmiany ilości:");
                            TextField changeQuantityTextField = new TextField("0");

                            Button increaseButton = new Button("+");
                            Button decreaseButton = new Button("-");

                            // Używamy tablicy do przechowywania ilości, aby umożliwić jej modyfikację wewnątrz lambdy
                            final double[] quantity = {edtquantity};

                            increaseButton.setOnAction(event -> {
                                try {
                                    double changeValue = Double.parseDouble(changeQuantityTextField.getText());
                                    quantity[0] += changeValue;
                                    currentQuantityLabel.setText(String.valueOf(quantity[0]));
                                } catch (NumberFormatException e) {
                                    showAlert(Alert.AlertType.ERROR, "Błąd walidacji", "Wartość zmiany ilości musi być liczbą.");
                                }
                            });

                            decreaseButton.setOnAction(event -> {
                                try {
                                    double changeValue = Double.parseDouble(changeQuantityTextField.getText());
                                    quantity[0] -= changeValue;
                                    if (quantity[0] < 0) {
                                        quantity[0] = 0;
                                    }
                                    currentQuantityLabel.setText(String.valueOf(quantity[0]));
                                } catch (NumberFormatException e) {
                                    showAlert(Alert.AlertType.ERROR, "Błąd walidacji", "Wartość zmiany ilości musi być liczbą.");
                                }
                            });

                            // Użycie HBox do ułożenia przycisków w jednym rzędzie
                            HBox buttonBox = new HBox(10);
                            buttonBox.getChildren().addAll(increaseButton, decreaseButton);

                            Label productUnitLabel = new Label("Jednostka Produktu:");
                            ComboBox<String> productUnitComboBox = new ComboBox<>();
                            productUnitComboBox.getItems().addAll("Sztk.", "Kg.", "Opakowań");
                            productUnitComboBox.setValue(edtunit);

                            Label productCodeLabel = new Label("Kod Kreskowy Produktu:");
                            TextField productCodeTextField = new TextField(edtcode);

                            VBox vbox = new VBox(10);
                            vbox.getChildren().addAll(
                                    productNameLabel, productNameTextField,
                                    productProducerLabel, productProducerComboBox,
                                    productQuantityLabel, currentQuantityLabel,
                                    changeQuantityLabel, changeQuantityTextField,
                                    buttonBox,
                                    productUnitLabel, productUnitComboBox,
                                    productCodeLabel, productCodeTextField
                            );
                            dialog.getDialogPane().setContent(vbox);

                            // Dodanie przycisków do formularza dialogowego
                            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                            // Obsługa zdarzenia zatwierdzenia formularza
                            dialog.setResultConverter(dialogButton -> {
                                if (dialogButton == ButtonType.OK) {
                                    return String.join(";",
                                            productNameTextField.getText(),
                                            productProducerComboBox.getValue(),
                                            String.valueOf(quantity[0]),
                                            productCodeTextField.getText(),
                                            productUnitComboBox.getValue()
                                    );
                                }
                                return null;
                            });

                            Optional<String> result = dialog.showAndWait();
                            if (result.isPresent()) {
                                String[] parts = result.get().split(";");
                                String newproductName = parts[0];
                                String newproductProducer = parts[1];
                                String newproductQuantityString = parts[2];
                                String newproductCode = parts[3];
                                String newproductUnit = parts[4];

                                // Walidacja danych
                                if (newproductName.isEmpty() || newproductProducer.isEmpty() || newproductQuantityString.isEmpty() ||
                                        newproductUnit == null || newproductCode.isEmpty()) {
                                    showAlert(Alert.AlertType.ERROR, "Błąd walidacji", "Wszystkie pola muszą być wypełnione.");
                                    return;
                                }

                                double newproductQuantity;
                                try {
                                    newproductQuantity = Double.parseDouble(newproductQuantityString);
                                    if (!"Kg.".equals(newproductUnit) && newproductQuantity % 1 != 0) {
                                        showAlert(Alert.AlertType.ERROR, "Błąd walidacji", "Dla jednostek innych niż Kg. ilość musi być liczbą całkowitą.");
                                        return;
                                    }
                                } catch (NumberFormatException e) {
                                    showAlert(Alert.AlertType.ERROR, "Błąd walidacji", "Ilość produktu musi być liczbą.");
                                    return;
                                }

                                if (!newproductCode.matches("\\d{13}")) {
                                    showAlert(Alert.AlertType.ERROR, "Błąd walidacji", "Kod kreskowy musi mieć dokładnie 13 cyfr.");
                                    return;
                                }

                                // Potwierdzenie edycji produktu
                                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                                confirmationAlert.setTitle("Potwierdzenie edycji");
                                confirmationAlert.setHeaderText("Edycja Produktu");
                                confirmationAlert.setContentText("Czy na pewno chcesz edytować produkt: " + newproductName + "?");

                                Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
                                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                                    // Pobieranie ID producenta na podstawie wybranej nazwy producenta
                                    int newProductProducerID = getProducerIDByName(newproductProducer);

                                    // Aktualizacja produktu w bazie danych
                                    try (PreparedStatement updateStatement = connection.prepareStatement(
                                            "UPDATE produkty SET Nazwa = ?, IDProducenta = ?, Ilosc = ?, Jednostka = ?, Kod = ? WHERE IDProduktu = ?"
                                    )) {
                                        updateStatement.setString(1, newproductName);
                                        updateStatement.setInt(2, newProductProducerID);
                                        updateStatement.setDouble(3, newproductQuantity);
                                        updateStatement.setString(4, newproductUnit);
                                        updateStatement.setString(5, newproductCode);
                                        updateStatement.setInt(6, productID);
                                        updateStatement.executeUpdate();
                                    }

                                    // Aktualizacja produktów w interfejsie użytkownika
                                    loadProductsFromDatabase();

                                    logger.info("Zaktualizowano produkt: {}", newproductName);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Wystąpił błąd podczas edycji produktu: " + e.getMessage());
                logger.error("Wystąpił błąd podczas edycji produktu: {}", e.getMessage());
            }
        } else {
            showAlert("Nie wybrano produktu", "Proszę wybrać produkt do edycji.");
            logger.warn("Nie wybrano produktu do edycji.");
        }
    }








    private String getProducerNameByID(int producerID) throws SQLException {
        String query = "SELECT Nazwa FROM producenci WHERE IDProducenta = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, producerID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("Nazwa");
                }
            }
        }
        throw new SQLException("Nie znaleziono nazwy producenta dla ID: " + producerID);
    }

    public int getProducerIDByName(String producerName) throws SQLException {
        int producerID = 0; // Domyślna wartość, jeśli nie zostanie znaleziony odpowiedni producent
        String queryMaxID = "SELECT MAX(IDProducenta) AS MaxID FROM producenci";
        try (PreparedStatement preparedStatementMaxID = connection.prepareStatement(queryMaxID)) {
            try (ResultSet resultSetMaxID = preparedStatementMaxID.executeQuery()) {
                if (resultSetMaxID.next()) {
                    producerID = resultSetMaxID.getInt("MaxID");
                }
            }
        }

        String query = "SELECT IDProducenta FROM producenci WHERE Nazwa = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, producerName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    producerID = resultSet.getInt("IDProducenta");
                }
            }
        }
        return producerID;
    }


    @FXML
    private void handleDeleteProduct() {
        // Usunięcie wybranego produktu
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            try {
                // Potwierdzenie usunięcia produktu
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Potwierdzenie usunięcia");
                confirmationAlert.setHeaderText("Usunięcie Produktu");
                confirmationAlert.setContentText("Czy na pewno chcesz usunąć produkt: " + selectedProduct.getName() + "?");

                Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    int productID = selectedProduct.getId();

                    // Usunięcie powiązania w tabeli producenciprodukty
                    String deleteRelationsQuery = "DELETE FROM producenciprodukty WHERE IDProduktu = ?";
                    PreparedStatement deleteRelationsStatement = connection.prepareStatement(deleteRelationsQuery);
                    deleteRelationsStatement.setInt(1, productID);
                    deleteRelationsStatement.executeUpdate();
                    deleteRelationsStatement.close();

                    // Usunięcie produktu z tabeli produkty
                    String deleteProductQuery = "DELETE FROM produkty WHERE IDProduktu = ?";
                    PreparedStatement deleteProductStatement = connection.prepareStatement(deleteProductQuery);
                    deleteProductStatement.setInt(1, productID);
                    deleteProductStatement.executeUpdate();

                    // Usunięcie produktu z listy
                    loadProductsFromDatabase();

                    deleteProductStatement.close();

                    logger.info("Usunięto produkt: {}", selectedProduct.getName());
                }
            } catch (Exception e) {
                System.out.println("Wystąpił błąd podczas usuwania produktu z bazy danych: " + e.getMessage());
                logger.error("Wystąpił błąd podczas usuwania produktu: {}", e.getMessage());
            }
        } else {
            showAlert("Nie wybrano produktu", "Proszę wybrać produkt do usunięcia.");
            logger.warn("Nie wybrano produktu do usunięcia.");
        }
    }




    @FXML
    private void handleLogout() {
        // Tworzenie alertu z pytaniem
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie wylogowania");
        alert.setHeaderText("Czy na pewno chcesz się wylogować?");
        alert.setContentText("Kliknij OK, aby wylogować się, lub Anuluj, aby zostać zalogowanym.");

        // Wyświetlenie okna dialogowego i oczekiwanie na odpowiedź użytkownika
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Jeśli użytkownik kliknął OK, wykonaj wylogowanie
            Main newMainApp = new Main();
            Stage stage = (Stage) productTableView.getScene().getWindow();
            newMainApp.start(stage);
        }
    }

    private void handleOrderAll(List<String> products) {
        try {
            List<String> employees = getEmployeesList();

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Dodaj nowe zadania");
            dialog.setHeaderText("Dodawanie nowych zadań");

            // Dodanie kontrolek do wprowadzania danych
            Label priorityLabel = new Label("Priorytet zadań:");
            ChoiceBox<String> priorityChoiceBox = new ChoiceBox<>();
            priorityChoiceBox.getItems().addAll("Niski", "Średni", "Wysoki");

            Label employeeLabel = new Label("Wybierz pracownika:");
            ChoiceBox<String> employeeChoiceBox = new ChoiceBox<>();
            employeeChoiceBox.getItems().addAll(employees);

            Label deadlineLabel = new Label("Termin wykonania:");
            DatePicker deadlinePicker = new DatePicker();

            // Tworzenie TimePicker
            Label timeLabel = new Label("Czas:");
            Spinner<LocalTime> timePicker = new Spinner<>();
            timePicker.setEditable(true);

            // Ustawienie ValueFactory
            SpinnerValueFactory<LocalTime> valueFactory = new SpinnerValueFactory<LocalTime>() {
                {
                    setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"), DateTimeFormatter.ofPattern("HH:mm")));
                    setValue(LocalTime.of(8, 0));
                }

                @Override
                public void decrement(int steps) {
                    LocalTime time = getValue().minusMinutes(steps * 10);
                    if (time.isBefore(LocalTime.of(8, 0))) {
                        time = LocalTime.of(22, 0).minusMinutes((steps * 10) % 840);
                    }
                    setValue(time);
                }

                @Override
                public void increment(int steps) {
                    LocalTime time = getValue().plusMinutes(steps * 10);
                    if (time.isAfter(LocalTime.of(22, 0))) {
                        time = LocalTime.of(8, 0).plusMinutes((steps * 10) % 840);
                    }
                    setValue(time);
                }
            };
            timePicker.setValueFactory(valueFactory);

            // Uzyskaj StringConverter<LocalTime> z SpinnerValueFactory
            StringConverter<LocalTime> stringConverter = valueFactory.getConverter();

            // Utwórz TextFormatter z uzyskanego StringConverter
            TextFormatter<LocalTime> textFormatter = new TextFormatter<>(stringConverter);

            // Utwórz ObjectProperty<LocalTime> na podstawie ReadOnlyObjectProperty<LocalTime>
            ObjectProperty<LocalTime> localTimeProperty = new SimpleObjectProperty<>();
            localTimeProperty.bind(timePicker.valueProperty());

            // Bindowanie wartości TextFormatter z ObjectProperty<LocalTime>
            textFormatter.valueProperty().bindBidirectional(localTimeProperty);

            // Ustaw TextFormatter jako formatter dla edytora Spinner
            timePicker.getEditor().setTextFormatter(textFormatter);

            // Dodawanie kontrolek do layoutu
            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(
                    priorityLabel, priorityChoiceBox,
                    employeeLabel, employeeChoiceBox,
                    deadlineLabel, deadlinePicker,
                    timeLabel, timePicker
            );
            dialog.getDialogPane().setContent(vbox);

            // Dodawanie przycisków do okna dialogowego
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Konfiguracja konwertera wyników
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    if (priorityChoiceBox.getValue() == null || employeeChoiceBox.getValue() == null ||
                            deadlinePicker.getValue() == null || timePicker.getValue() == null) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Niekompletne dane");
                        alert.setHeaderText("Wypełnij wszystkie pola");
                        alert.setContentText("Proszę wypełnić wszystkie pola przed zatwierdzeniem zadania.");
                        alert.showAndWait();
                        return null; // Return null to prevent the dialog from closing
                    }

                    String deadline = deadlinePicker.getValue().toString() + " " + timePicker.getValue().toString();
                    return "Zlecenie Zamówienia Produktu;" + employeeChoiceBox.getValue() + ";" + priorityChoiceBox.getValue() + ";" + deadline;
                }
                return null;
            });

            // Wyświetl okno dialogowe i zarządzaj wynikami
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String[] parts = result.get().split(";");
                String taskDescriptionTemplate = parts[0];
                String employeeName = parts[1];
                String priority = parts[2];
                String deadline = parts[3];
                int employeeId = getEmployeeIdByName(employeeName);

                for (String product : products) {
                    String taskDescription = taskDescriptionTemplate + " " + product;

                    // Wstaw nowe zadanie do bazy danych
                    String insertQuery = "INSERT INTO zadania (Opis, Status, Priorytet, Kategoria, TerminWykonania, DataUtworzenia, DataModyfikacji, IDPracownika) VALUES (?, 'Oczekujące', ?, 'Magazyn', ?, NOW(), NOW(), ?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    insertStatement.setString(1, taskDescription);
                    insertStatement.setString(2, priority);
                    insertStatement.setString(3, deadline);
                    insertStatement.setInt(4, employeeId);
                    insertStatement.executeUpdate();

                    // Pobierz wygenerowane ID zadania
                    ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                    int taskId;
                    if (generatedKeys.next()) {
                        taskId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Nie udało się dodać nowego zadania, brak wygenerowanego ID.");
                    }

                    // Przypisz pracownika do zadania
                    String assignEmployeeQuery = "INSERT INTO pracownicyzadania (IDPracownika, IDZadania) VALUES (?, ?)";
                    PreparedStatement assignEmployeeStatement = connection.prepareStatement(assignEmployeeQuery);
                    assignEmployeeStatement.setInt(1, employeeId);
                    assignEmployeeStatement.setInt(2, taskId);
                    assignEmployeeStatement.executeUpdate();

                    logger.info("Dodano nowe zadanie: {}", taskDescription);
                }
            }
        } catch (Exception e) {
            showAlert("Błąd dodawania zadania", "Wystąpił błąd podczas dodawania zadania: " + e.getMessage());
            logger.error("Wystąpił błąd podczas dodawania zadania: {}", e.getMessage());
        }
    }




    @FXML
    private void handleCheckRunningLow() {
        try {
            // Pobranie produktów z niską ilością
            List<String> lowStockProducts = getLowStockProducts();

            // Tworzenie alertu z listą produktów i przyciskiem
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Produkty z niskim stanem");
            dialog.setHeaderText("Następujące produkty mają niską ilość:");

            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(5);

            int rowIndex = 0;
            for (String product : lowStockProducts) {
                Label productLabel = new Label(product);
                Button orderButton = createButtonForProduct(product);

                gridPane.add(productLabel, 0, rowIndex);
                gridPane.add(orderButton, 1, rowIndex);

                rowIndex++;
            }

            // Przycisk "Zleć zamówienie wszystkim"
            Button orderAllButton = new Button("Zleć zamówienie wszystkich produktów");
            orderAllButton.setOnAction(event -> {

                // Obsłuż zlecenie dla wszystkich produktów
                handleOrderAll(lowStockProducts);
            });
            gridPane.add(orderAllButton, 0, rowIndex, 2, 1); // Spanujemy przycisk na dwie kolumny

            dialog.getDialogPane().setContent(gridPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();
        } catch (SQLException e) {
            System.out.println("Wystąpił błąd podczas sprawdzania produktów z niskim stanem: " + e.getMessage());
        }
    }



    private Button createButtonForProduct(String product) {
        Button button = new Button("Zleć Zamówienie");
        button.setOnAction(event -> handleAddTaskWithProduct(product));
        return button;
    }

    private List<String> getLowStockProducts() throws SQLException {
        List<String> lowStockProducts = new ArrayList<>();

        // Pobranie produktów z niską ilością z bazy danych
        String query = "SELECT Nazwa FROM produkty WHERE Ilosc < ?"; // Załóżmy, że "niska ilość" to mniej niż 10
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, 10); // Ustawienie progu niskiej ilości
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    lowStockProducts.add(resultSet.getString("Nazwa"));
                }
            }
        }

        return lowStockProducts;
    }

    @FXML
    private void handleAddTask() {
        handleAddTaskWithProduct("");
    }

    private void handleAddTaskWithProduct(String productName) {
        try {
            List<String> employees = getEmployeesList();

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Dodaj nowe zadanie");
            dialog.setHeaderText("Dodawanie nowego zadania");

            // Utwórz kontrolki do wprowadzania danych
            Label taskLabel = new Label("Opis zadania:");
            TextArea taskTextArea = new TextArea("Zlecenie Zamówienia Produktu " + productName);
            taskTextArea.setPromptText("Opis zadania");

            Label priorityLabel = new Label("Priorytet zadania:");
            ChoiceBox<String> priorityChoiceBox = new ChoiceBox<>();
            priorityChoiceBox.getItems().addAll("Niski", "Średni", "Wysoki");

            Label employeeLabel = new Label("Wybierz pracownika:");
            ChoiceBox<String> employeeChoiceBox = new ChoiceBox<>();
            employeeChoiceBox.getItems().addAll(employees);

            Label deadlineLabel = new Label("Termin wykonania:");
            DatePicker deadlinePicker = new DatePicker();
            // Tworzenie TimePicker
            Label timeLabel = new Label("Czas:");
            Spinner<LocalTime> timePicker = new Spinner<>();
            timePicker.setEditable(true);

            // Ustawienie ValueFactory
            SpinnerValueFactory<LocalTime> valueFactory = new SpinnerValueFactory<LocalTime>() {
                {
                    setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"), DateTimeFormatter.ofPattern("HH:mm")));
                    setValue(LocalTime.of(8, 0));
                }
                @Override
                public void decrement(int steps) {
                    LocalTime time = getValue().minusMinutes(steps * 10);
                    if (time.isBefore(LocalTime.of(8, 0))) {
                        time = LocalTime.of(22, 0).minusMinutes((steps * 10) % 840);
                    }
                    setValue(time);
                }
                @Override
                public void increment(int steps) {
                    LocalTime time = getValue().plusMinutes(steps * 10);
                    if (time.isAfter(LocalTime.of(22, 0))) {
                        time = LocalTime.of(8, 0).plusMinutes((steps * 10) % 840);
                    }
                    setValue(time);
                }
            };
            timePicker.setValueFactory(valueFactory);

            // Uzyskaj StringConverter<LocalTime> z SpinnerValueFactory
            StringConverter<LocalTime> stringConverter = valueFactory.getConverter();

            // Utwórz TextFormatter z uzyskanego StringConverter
            TextFormatter<LocalTime> textFormatter = new TextFormatter<>(stringConverter);

            // Utwórz ObjectProperty<LocalTime> na podstawie ReadOnlyObjectProperty<LocalTime>
            ObjectProperty<LocalTime> localTimeProperty = new SimpleObjectProperty<>();
            localTimeProperty.bind(timePicker.valueProperty());

            // Bindowanie wartości TextFormatter z ObjectProperty<LocalTime>
            textFormatter.valueProperty().bindBidirectional(localTimeProperty);

            // Ustaw TextFormatter jako formatter dla edytora Spinner
            timePicker.getEditor().setTextFormatter(textFormatter);





            // Dodawanie kontrolek do layoutu
            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(
                    taskLabel, taskTextArea,
                    priorityLabel, priorityChoiceBox,
                    employeeLabel, employeeChoiceBox,
                    deadlineLabel, deadlinePicker,
                    timeLabel, timePicker
            );
            dialog.getDialogPane().setContent(vbox);

            // Dodawanie przycisków do okna dialogowego
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Konfiguracja konwertera wyników
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    if (taskTextArea.getText().isEmpty() || priorityChoiceBox.getValue() == null ||
                            employeeChoiceBox.getValue() == null || deadlinePicker.getValue() == null || timePicker.getValue() == null) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Niekompletne dane");
                        alert.setHeaderText("Wypełnij wszystkie pola");
                        alert.setContentText("Proszę wypełnić wszystkie pola przed zatwierdzeniem zadania.");
                        alert.showAndWait();
                        return null; // Return null to prevent the dialog from closing
                    }

                    String deadline = deadlinePicker.getValue().toString() + " " +
                            timePicker.getValue().toString();
                    return taskTextArea.getText() + ";" + employeeChoiceBox.getValue() + ";" + priorityChoiceBox.getValue() + ";" + deadline;
                }
                return null;
            });


            // Wyświetl okno dialogowe i zarządzaj wynikami
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Potwierdzenie dodania");
                confirmationAlert.setHeaderText("Dodanie Zadania");
                confirmationAlert.setContentText("Czy na pewno chcesz dodać to zadanie?");

                Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    // Rozdziel dane z wyniku
                    String[] parts = result.get().split(";");
                    String taskDescription = parts[0];
                    String employeeName = parts[1];
                    String priority = parts[2];
                    String deadline = parts[3];
                    int employeeId = getEmployeeIdByName(employeeName);

                    // Wstaw nowe zadanie do bazy danych
                    String insertQuery = "INSERT INTO zadania (Opis, Status, Priorytet, Kategoria, TerminWykonania, DataUtworzenia, DataModyfikacji, IDPracownika) VALUES (?, 'Oczekujące', ?, 'Magazyn', ?, NOW(), NOW(), ?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    insertStatement.setString(1, taskDescription);
                    insertStatement.setString(2, priority);
                    insertStatement.setString(3, deadline);
                    insertStatement.setInt(4, employeeId);
                    insertStatement.executeUpdate();

                    // Pobierz wygenerowane ID zadania
                    ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                    int taskId;
                    if (generatedKeys.next()) {
                        taskId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Nie udało się dodać nowego zadania, brak wygenerowanego ID.");
                    }

                    // Przypisz pracownika do zadania
                    String assignEmployeeQuery = "INSERT INTO pracownicyzadania (IDPracownika, IDZadania) VALUES (?, ?)";
                    PreparedStatement assignEmployeeStatement = connection.prepareStatement(assignEmployeeQuery);
                    assignEmployeeStatement.setInt(1, employeeId);
                    assignEmployeeStatement.setInt(2, taskId);
                    assignEmployeeStatement.executeUpdate();

                    logger.info("Dodano nowe zadanie: {}", taskDescription);
                }
            }
        } catch (Exception e) {
            showAlert("Błąd dodawania zadania", "Wystąpił błąd podczas dodawania zadania: " + e.getMessage());
            logger.error("Wystąpił błąd podczas dodawania zadania: {}", e.getMessage());
        }
    }

    private int getEmployeeIdByName(String employeeName) throws SQLException {
        String[] names = employeeName.split(" ");
        String firstName = names[0];
        String lastName = names[1];

        String query = "SELECT IDPracownika FROM pracownicy WHERE Imie = ? AND Nazwisko = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, firstName);
        preparedStatement.setString(2, lastName);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("IDPracownika");
        } else {
            throw new SQLException("Nie znaleziono pracownika o podanym imieniu i nazwisku.");
        }
    }

    private List<String> getEmployeesList() throws SQLException {
        List<String> employees = new ArrayList<>();
        String query = "SELECT Imie, Nazwisko FROM pracownicy WHERE IDRoli = 1";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            employees.add(resultSet.getString("Imie") + " " + resultSet.getString("Nazwisko"));
        }
        logger.info("Pobrano listę pracowników.");
        return employees;
    }

    @FXML
    private void handleSearch() {
        // Stworzenie okna dialogowego do wyszukiwania
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Wybierz Kryterium Wyszukiwania");
        dialog.setHeaderText("Wybierz kryterium wyszukiwania i wpisz fraze:");

        // Dodawanie comboboxa do wyboru kryterium
        ComboBox<String> searchCriteriaComboBox = new ComboBox<>();
        searchCriteriaComboBox.getItems().addAll("Nazwa Produktu", "Nazwa Producenta");
        searchCriteriaComboBox.getSelectionModel().selectFirst(); //Wybranie pierwszej opcji jako domyślna

        // Dodawanie TextField do wpisania szukanego kryterium
        TextField searchFieldDialog = new TextField();
        searchFieldDialog.setPromptText("Wpisz szukaną frazę");

        //Dodanie layoutu do zawartosci okna dialogowego
        VBox dialogContent = new VBox(10);
        dialogContent.getChildren().addAll(
                new Label("Kryterium Wyszukiwania:"),
                searchCriteriaComboBox,
                new Label("Szukana Fraza:"),
                searchFieldDialog
        );

        dialog.getDialogPane().setContent(dialogContent);

        // Dodanie przycisku ok i anuluj
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Czekanie aż użytkownik wybierze coś lub anuluje
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Wyszukiwanie na podstawie wybranego kryterium i frazy wyszukiwania
                String searchTerm = searchFieldDialog.getText().trim();
                String searchCategory = searchCriteriaComboBox.getValue();
                if (!searchTerm.isEmpty() && searchCategory != null) {
                    try {
                        String query;
                        if (searchCategory.equals("Nazwa Produktu")) {
                            query = "SELECT * FROM produkty WHERE Nazwa LIKE ?";
                        } else if (searchCategory.equals("Nazwa Producenta")) {
                            query = "SELECT p.* FROM produkty p JOIN producenci pr ON p.IDProducenta = pr.IDProducenta WHERE pr.Nazwa LIKE ?";
                        } else {
                            System.out.println("Nie wybrano kategorii wyszukiwania.");
                            return; // Wyjscie z metody w przypadku nie wybrania kategorii
                        }

                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, "%" + searchTerm + "%");
                        ResultSet resultSet = preparedStatement.executeQuery();

                        List<Product> products = new ArrayList<>();
                        while (resultSet.next()) {
                            int productID = resultSet.getInt("IDProduktu");
                            String productName = resultSet.getString("Nazwa");
                            int producerID = resultSet.getInt("IDProducenta");
                            String producerName = getProducerNameByID(producerID);
                            int quantity = resultSet.getInt("Ilosc");
                            String unit = resultSet.getString("Jednostka");
                            String code = resultSet.getString("Kod");

                            // Tworzenie obiektu produktu
                            Product product = new Product(productID, productName, producerName, quantity, unit, code);
                            products.add(product);
                        }

                        // Aktualizacja danych w tabeli productTableView
                        productTableView.getItems().setAll(products);

                        preparedStatement.close();
                    } catch (Exception e) {
                        System.out.println("Wystąpił błąd podczas wyszukiwania produktów.");
                    }
                } else {
                    // Jeśli pole wyszukiwania jest puste lub nie wybrano kategorii, załaduj wszystkie produkty
                    loadProductsFromDatabase();
                }
            }
        });
    }
    @FXML
    private void handleTask() {
        // Powrót do widoku zadań
        mainApp.loadTasksView(userType,loggedInUsername);
    }

    @FXML
    private void handleRaport() {
        // Utwórz niestandardowe okno dialogowe.
        Dialog<Pair<Pair<String, String>, String>> dialog = new Dialog<>();
        dialog.setTitle("Generuj raport produktów");
        dialog.setHeaderText("Wybierz filtry dla raportu produktów");

        // Ustaw typy przycisków.
        ButtonType generateButtonType = new ButtonType("Generuj", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);

        // Utwórz pola tekstowe i ComboBox dla filtrów.
        TextField filterPhraseTextField = new TextField();
        filterPhraseTextField.setPromptText("Fraza szukana");

        ComboBox<String> filterTypeComboBox = new ComboBox<>();
        filterTypeComboBox.getItems().addAll("Nazwa Producenta", "Jednostka", "Brak");
        filterTypeComboBox.setValue("Brak");

        // Utwórz GridPane i dodaj pola tekstowe oraz ComboBox.
        GridPane grid = new GridPane();
        grid.add(new Label("Filtr:"), 0, 0);
        grid.add(filterTypeComboBox, 1, 0);
        grid.add(filterPhraseTextField, 2, 0);

        dialog.getDialogPane().setContent(grid);

        // Konwertuj wynik na parę filtrów i frazy po kliknięciu przycisku OK.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generateButtonType) {
                String filterType = filterTypeComboBox.getValue();
                String filterPhrase = filterPhraseTextField.getText();
                return new Pair<>(new Pair<>(filterType, ""), filterPhrase);
            }
            return null;
        });

        // Pokaż okno dialogowe i pobierz wynik.
        dialog.showAndWait().ifPresent(filters -> {
            String filterType = filters.getKey().getKey();
            String filterPhrase = filters.getValue();

            PdfGenerator pdfGenerator = new PdfGenerator();
            try {
                int filterTypeValue;
                switch (filterType) {
                    case "Nazwa Producenta":
                        filterType = "Nazwa Producenta";
                        break;
                    case "Jednostka":
                        filterType= "Jednostka";
                        break;
                    default:
                        filterType = ""; // Brak filtra
                        break;
                }
                String desktopPath = System.getProperty("user.home") + "\\Desktop";
                String filePath = desktopPath + "\\storagepdf.pdf";
                pdfGenerator.manipulatePdfProduct(filePath ,filterType, filterPhrase);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Błąd");
                alert.setHeaderText("Wystąpił błąd");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }




}

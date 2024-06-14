package com.example.iie_szarzy_2024;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @FXML
    private Label titleLabel;

    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, Integer> userIdColumn;
    @FXML
    private TableColumn<User, String> userNameColumn;
    @FXML
    private TableColumn<User, String> userSurnameColumn;
    @FXML
    private TableColumn<User, String> userLoginColumn;
    @FXML
    private TableColumn<User, String> userPasswordColumn;
    @FXML
    private TableColumn<User, String> userOccupationColumn;
    @FXML
    private TableColumn<User, String> userEmailColumn;
    @FXML
    private TableColumn<User, String> userHireDateColumn;
    @FXML
    private TableColumn<User, String> userAccountStatusColumn;
    @FXML
    private TableColumn<User, Integer> userRoleIDColumn;

    @FXML
    Connection connection;

    private String loggedInUsername;
    private String userType;

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }
    @FXML
    public void setLoggedInUserType(String userType) {
        this.userType = userType;
        // Logika dostosowująca wyświetlane informacje w zależności od zalogowanego użytkownika
        if ("administrator".equals(userType));
    }
    @FXML
    public void initialize() {
        try {
            userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            userSurnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
            userLoginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
            userPasswordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
            userOccupationColumn.setCellValueFactory(new PropertyValueFactory<>("occupation"));
            userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            userHireDateColumn.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
            userAccountStatusColumn.setCellValueFactory(new PropertyValueFactory<>("accountStatus"));
            userRoleIDColumn.setCellValueFactory(new PropertyValueFactory<>("roleId"));

            // Pozwolenie Kolumnom na rozszerzenie
            userIdColumn.setResizable(true);
            userNameColumn.setResizable(true);
            userSurnameColumn.setResizable(true);
            userLoginColumn.setResizable(true);
            userPasswordColumn.setResizable(true);
            userOccupationColumn.setResizable(true);
            userEmailColumn.setResizable(true);
            userHireDateColumn.setResizable(true);
            userAccountStatusColumn.setResizable(true);
            userRoleIDColumn.setResizable(true);

            // Włączanie równego rozkładu kolumn
            userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // Nawiązanie połączenia z bazą danych
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sklepszary2", "root", "");
            // Ładowanie użytkowników z bazy danych
            loadUsersFromDatabase();
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
    public void loadUsersFromDatabase() {
        try {
            String query = "SELECT * FROM Pracownicy";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<User> users = new ArrayList<>();

            while (resultSet.next()) {
                int userId = resultSet.getInt("IDPracownika");
                String userName = resultSet.getString("Imie");
                String userSurname = resultSet.getString("Nazwisko");
                String userLogin = resultSet.getString("Login");
                String userPassword = resultSet.getString("Haslo");
                String userOccupation = resultSet.getString("Stanowisko");
                String userEmail = resultSet.getString("Email");
                String userHireDate = resultSet.getString("DataZatrudnienia");
                String userAccountStatus = resultSet.getString("StatusKonta");
                int userRoleId = resultSet.getInt("IDRoli");

                // Tworzenie obiektu użytkownika
                User user = new User(userId, userName, userSurname, userLogin, userPassword, userOccupation, userEmail, userHireDate, userAccountStatus,userRoleId);
                users.add(user);
            }

            // Ustawienie danych użytkowników w tabeli
            userTableView.getItems().setAll(users);
            logger.info("Załadowano użytkowników z bazy danych.");
            preparedStatement.close();
        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas ładowania użytkowników z bazy danych.");
            logger.error("Wystąpił błąd podczas ładowania użytkowników z bazy danych: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddUser() {
        try {
            // Wyświetlenie okna dialogowego z formularzem dodawania użytkownika
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Dodaj nowego użytkownika");
            dialog.setHeaderText("Dodawanie nowego użytkownika");

            // Dodawanie kontrolek do formularza okna dialogowego
            Label nameLabel = new Label("Imię:");
            TextField nameTextField = new TextField();
            nameTextField.setPromptText("Imię");

            Label surnameLabel = new Label("Nazwisko:");
            TextField surnameTextField = new TextField();
            surnameTextField.setPromptText("Nazwisko");

            Label loginLabel = new Label("Login:");
            TextField loginTextField = new TextField();
            loginTextField.setPromptText("Login");

            Label passwordLabel = new Label("Hasło:");
            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Hasło");

            Label occupationLabel = new Label("Stanowisko:");
            TextField occupationTextField = new TextField();
            occupationTextField.setPromptText("Stanowisko");

            Label emailLabel = new Label("Email:");
            TextField emailTextField = new TextField();
            emailTextField.setPromptText("Email");

            Label roleIdLabel = new Label("ID Roli:");
            TextField roleIdTextField = new TextField();
            roleIdTextField.setPromptText("ID Roli");

            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(
                    nameLabel, nameTextField,
                    surnameLabel, surnameTextField,
                    loginLabel, loginTextField,
                    passwordLabel, passwordField,
                    occupationLabel, occupationTextField,
                    emailLabel, emailTextField,
                    roleIdLabel, roleIdTextField
            );
            dialog.getDialogPane().setContent(vbox);

            // Dodawanie przycisków do formularza dialogowego
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Obsługa zdarzeń dla przesyłania formularzy
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    // Sprawdź znaki numeryczne w imieniu lub nazwisku
                    if (nameTextField.getText().matches(".*\\d.*") || surnameTextField.getText().matches(".*\\d.*")) {
                        showAlert("Błąd", "Imię lub nazwisko nie może zawierać cyfr.");
                        return null;
                    }
                    // Sprawdź poprawność formatu wiadomości e-mail
                    if (!emailTextField.getText().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
                        showAlert("Błąd", "Niepoprawny format adresu email.");
                        return null;
                    }
                    // Sprawdzanie pustych pól
                    if (nameTextField.getText().isEmpty() || surnameTextField.getText().isEmpty() ||
                            loginTextField.getText().isEmpty() || passwordField.getText().isEmpty() ||
                            occupationTextField.getText().isEmpty() || emailTextField.getText().isEmpty() ||
                            roleIdTextField.getText().isEmpty()) {
                        showAlert("Błąd", "Wypełnij wszystkie pola.");
                        return null;
                    }
                    return String.join(";",
                            nameTextField.getText(),
                            surnameTextField.getText(),
                            loginTextField.getText(),
                            passwordField.getText(),
                            occupationTextField.getText(),
                            emailTextField.getText(),
                            roleIdTextField.getText()
                    );
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String[] parts = result.get().split(";");
                String name = parts[0];
                String surname = parts[1];
                String login = parts[2];
                String password = parts[3];
                String occupation = parts[4];
                String email = parts[5];
                int roleId = Integer.parseInt(parts[6]);

                // Potwierdzenie dodania użytkownika
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Potwierdzenie dodania");
                confirmationAlert.setHeaderText("Dodawanie nowego użytkownika");
                confirmationAlert.setContentText("Czy na pewno chcesz dodać nowego użytkownika: " + name + " " + surname + "?");

                Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    // Dodanie nowego użytkownika do bazy danych
                    String addProductQuery = "INSERT INTO pracownicy (Imie, Nazwisko, Login, Haslo, Stanowisko, Email,StatusKonta , DataZatrudnienia, IDRoli) VALUES (?, ?, ?, ?, ?, ?,StatusKonta = 'Aktywne' ,NOW(), ?)";
                    PreparedStatement addProductStatement = connection.prepareStatement(addProductQuery);
                    addProductStatement.setString(1, name);
                    addProductStatement.setString(2, surname);
                    addProductStatement.setString(3, login);
                    addProductStatement.setString(4, password);
                    addProductStatement.setString(5, occupation);
                    addProductStatement.setString(6, email);
                    addProductStatement.setInt(7, roleId);
                    addProductStatement.executeUpdate();
                    addProductStatement.close();

                    // Aktualizacja interfejsu użytkownika
                    loadUsersFromDatabase();

                    logger.info("Dodano nowego użytkownika: {}", login);
                }
            }
        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas dodawania nowego użytkownika: " + e.getMessage());
            logger.error("Wystąpił błąd podczas dodawania nowego użytkownika: {}", e.getMessage());
        }
    }




    @FXML
    private void handleEditUser() {
        User selectedUser = userTableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Potwierdzenie edycji użytkownika
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Potwierdzenie edycji");
            confirmationAlert.setHeaderText("Edycja użytkownika");
            confirmationAlert.setContentText("Czy na pewno chcesz edytować użytkownika: " + selectedUser.getName() + "?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    int userId = selectedUser.getId();

                    String query = "SELECT * FROM pracownicy WHERE IDPracownika = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setInt(1, userId);
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                String edtName = resultSet.getString("Imie");
                                String edtSurname = resultSet.getString("Nazwisko");
                                String edtLogin = resultSet.getString("Login");
                                String edtPassword = resultSet.getString("Haslo");
                                String edtOccupation = resultSet.getString("Stanowisko");
                                String edtAccountStatus = resultSet.getString("StatusKonta");
                                String edtEmail = resultSet.getString("Email");
                                LocalDate edtHireDate = resultSet.getDate("DataZatrudnienia").toLocalDate();
                                int edtRoleId = resultSet.getInt("IDRoli");

                                // Wyświetlenie okna dialogowego z formularzem edycji użytkownika
                                Dialog<String> dialog = new Dialog<>();
                                dialog.setTitle("Edytuj Użytkownika");
                                dialog.setHeaderText("Edycja Użytkownika o ID: " + userId);

                                // Dodanie kontrolek do formularza dialogowego
                                Label nameLabel = new Label("Imię:");
                                TextField nameTextField = new TextField(edtName);

                                Label surnameLabel = new Label("Nazwisko:");
                                TextField surnameTextField = new TextField(edtSurname);

                                Label loginLabel = new Label("Login:");
                                TextField loginTextField = new TextField(edtLogin);

                                Label passwordLabel = new Label("Hasło:");
                                PasswordField passwordField = new PasswordField();
                                passwordField.setText(edtPassword);

                                Label occupationLabel = new Label("Stanowisko:");
                                TextField occupationTextField = new TextField(edtOccupation);

                                Label emailLabel = new Label("Email:");
                                TextField emailTextField = new TextField(edtEmail);

                                Label accountStatusLabel = new Label("Status Konta:");
                                ChoiceBox<String> accountStatusChoiceBox = new ChoiceBox<>();
                                accountStatusChoiceBox.getItems().addAll("Aktywne", "Nieaktywne");
                                accountStatusChoiceBox.setValue(edtAccountStatus);


                                Label hireDateLabel = new Label("Data Zatrudnienia:");
                                DatePicker hireDatePicker = new DatePicker(edtHireDate);

                                Label roleIdLabel = new Label("ID Roli:");
                                TextField roleIdTextField = new TextField(Integer.toString(edtRoleId));

                                VBox vbox = new VBox(10);
                                vbox.getChildren().addAll(
                                        nameLabel, nameTextField,
                                        surnameLabel, surnameTextField,
                                        loginLabel, loginTextField,
                                        passwordLabel, passwordField,
                                        occupationLabel, occupationTextField,
                                        emailLabel, emailTextField,
                                        accountStatusLabel, accountStatusChoiceBox,
                                        hireDateLabel, hireDatePicker,
                                        roleIdLabel, roleIdTextField
                                );
                                dialog.getDialogPane().setContent(vbox);

                                // Dodanie przycisków do formularza dialogowego
                                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                                // Obsługa zdarzenia zatwierdzenia formularza
                                dialog.setResultConverter(dialogButton -> {
                                    if (dialogButton == ButtonType.OK) {
                                        // Walidacja danych
                                        if (!nameTextField.getText().matches("[a-zA-Z]+")) {
                                            showAlert("Błąd", "Niepoprawne imię.");
                                            return null;
                                        }
                                        if (!surnameTextField.getText().matches("[a-zA-Z]+")) {
                                            showAlert("Błąd", "Niepoprawne nazwisko.");
                                            return null;
                                        }
                                        if (!isValidEmail(emailTextField.getText())) {
                                            showAlert("Błąd", "Niepoprawny adres email.");
                                            return null;
                                        }

                                        // Jeśli wszystkie dane są poprawne, zwracamy je
                                        return String.join(";",
                                                nameTextField.getText(),
                                                surnameTextField.getText(),
                                                loginTextField.getText(),
                                                passwordField.getText(),
                                                occupationTextField.getText(),
                                                emailTextField.getText(),
                                                accountStatusChoiceBox.getValue(),
                                                hireDatePicker.getValue().toString(),
                                                roleIdTextField.getText()
                                        );
                                    }
                                    return null;
                                });

                                Optional<String> dialogResult = dialog.showAndWait();
                                if (dialogResult.isPresent()) {
                                    String[] parts = dialogResult.get().split(";");
                                    String newName = parts[0];
                                    String newSurname = parts[1];
                                    String newLogin = parts[2];
                                    String newPassword = parts[3];
                                    String newOccupation = parts[4];
                                    String newEmail = parts[5];
                                    String newAccountStatus = parts[6];
                                    LocalDate newHireDate = LocalDate.parse(parts[7]);
                                    int newRoleId = Integer.parseInt(parts[8]);

                                    // Aktualizacja użytkownika w bazie danych
                                    try (PreparedStatement updateStatement = connection.prepareStatement(
                                            "UPDATE pracownicy SET Imie = ?, Nazwisko = ?, Login = ?, Haslo = ?, Stanowisko = ?, Email = ?,StatusKonta = ?, DataZatrudnienia = ?, IDRoli = ? WHERE IDPracownika = ?"
                                    )) {
                                        updateStatement.setString(1, newName);
                                        updateStatement.setString(2, newSurname);
                                        updateStatement.setString(3, newLogin);
                                        updateStatement.setString(4, newPassword);
                                        updateStatement.setString(5, newOccupation);
                                        updateStatement.setString(6, newEmail);
                                        updateStatement.setString(7, newAccountStatus);
                                        updateStatement.setDate(8, Date.valueOf(newHireDate));
                                        updateStatement.setInt(9, newRoleId);
                                        updateStatement.setInt(10, userId);
                                        updateStatement.executeUpdate();
                                    }

                                    // Aktualizacja użytkowników w interfejsie użytkownika
                                    loadUsersFromDatabase();

                                    logger.info("Zaktualizowano użytkownika: {}", newLogin);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Wystąpił błąd podczas edycji użytkownika: " + e.getMessage());
                    logger.error("Wystąpił błąd podczas edycji użytkownika: {}", e.getMessage());
                }
            }
        } else {
            showAlert("Nie wybrano użytkownika", "Proszę wybrać użytkownika do edycji.");
            logger.warn("Nie wybrano użytkownika do edycji.");
        }
    }
    private boolean isValidEmail(String email) {
        // Prosta walidacja adresu email przy pomocy wyrażenia regularnego
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    @FXML
    private void handleDeleteUser() {
        // Usunięcie wybranego użytkownika
        User selectedUser = userTableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Potwierdzenie usunięcia użytkownika
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Potwierdzenie usunięcia");
            confirmationAlert.setHeaderText("Usuwanie użytkownika");
            confirmationAlert.setContentText("Czy na pewno chcesz usunąć użytkownika: " + selectedUser.getName() + "?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    int userID = selectedUser.getId();

                    // Usunięcie użytkownika z tabeli pracownicy
                    String deleteUserQuery = "DELETE FROM pracownicy WHERE IDPracownika = ?";
                    PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserQuery);
                    deleteUserStatement.setInt(1, userID);
                    deleteUserStatement.executeUpdate();

                    // Aktualizacja listy użytkowników
                    loadUsersFromDatabase();

                    deleteUserStatement.close();
                    logger.info("Usunięto użytkownika: {}", selectedUser.getName());

                } catch (Exception e) {
                    System.out.println("Wystąpił błąd podczas usuwania użytkownika: " + e.getMessage());
                    logger.error("Wystąpił błąd podczas usuwania użytkownika: {}", e.getMessage());
                }
            }
        } else {
            showAlert("Nie wybrano użytkownika", "Proszę wybrać użytkownika do usunięcia.");
            logger.warn("Nie wybrano użytkownika do usunięcia.");
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
            Stage stage = (Stage) userTableView.getScene().getWindow();
            newMainApp.start(stage);
        }
    }

    @FXML
    private void handleUserSearch() {
        // Stworzenie okna dialogowego do wyszukiwania
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Wybierz Kryterium Wyszukiwania");
        dialog.setHeaderText("Wybierz kryterium wyszukiwania i wpisz fraze:");

        // Dodawanie comboboxa do wyboru kryterium
        ComboBox<String> searchCriteriaComboBox = new ComboBox<>();
        searchCriteriaComboBox.getItems().addAll("Imię Pracownika", "Nazwisko Pracownika","IDRoli");
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
                        if (searchCategory.equals("Imię Pracownika")) {
                            query = "SELECT * FROM pracownicy WHERE Imie LIKE ?";
                        } else if (searchCategory.equals("Nazwisko Pracownika")) {
                            query = "SELECT * FROM pracownicy WHERE Nazwisko LIKE ?";
                        } else if (searchCategory.equals("IDRoli")) {
                            query = "SELECT * FROM pracownicy WHERE IDRoli LIKE ?";
                        }else {
                            System.out.println("Nie wybrano kategorii wyszukiwania.");
                            return; // Wyjscie z metody w przypadku nie wybrania kategorii
                        }

                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, "%" + searchTerm + "%");
                        ResultSet resultSet = preparedStatement.executeQuery();

                        List<User> users = new ArrayList<>();
                        while (resultSet.next()) {
                            int userId = resultSet.getInt("IDPracownika");
                            String userName = resultSet.getString("Imie");
                            String userSurname = resultSet.getString("Nazwisko");
                            String userLogin = resultSet.getString("Login");
                            String userPassword = resultSet.getString("Haslo");
                            String userOccupation = resultSet.getString("Stanowisko");
                            String userHireDate = resultSet.getString("DataZatrudnienia");
                            String userAccountStatus = resultSet.getString("StatusKonta");
                            String userEmail = resultSet.getString("Email");
                            int userRoleId = resultSet.getInt("IDRoli");

                            // Tworzenie obiektu produktu
                            User user = new User(userId, userName, userSurname, userLogin, userPassword, userOccupation, userEmail, userHireDate, userAccountStatus ,userRoleId);
                            users.add(user);
                        }

                        // Aktualizacja danych w tabeli productTableView
                        userTableView.getItems().setAll(users);

                        preparedStatement.close();
                    } catch (Exception e) {
                        System.out.println("Wystąpił błąd podczas wyszukiwania produktów.");
                    }
                } else {
                    // Jeśli pole wyszukiwania jest puste lub nie wybrano kategorii, załaduj wszystkie produkty
                    loadUsersFromDatabase();
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
        Dialog<Pair<Integer, String>> dialog = new Dialog<>();
        dialog.setTitle("Wybierz filtr i wpisz frazę");
        dialog.setHeaderText("Wybierz filtr i wpisz frazę");

        // Ustaw typy przycisków.
        ButtonType okButtonType = new ButtonType("OK", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Utwórz filtr ComboBox i pole tekstowe frazy.
        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("Wszyscy Użytkownicy", "Wybór grupy użytkowników");
        filterComboBox.setValue("Wszyscy Użytkownicy");

        TextField phraseTextField = new TextField();
        phraseTextField.setPromptText("Wprowadź frazę");

        // Utwórz GridPane i dodaj do niego ComboBox i TextField.
        GridPane grid = new GridPane();
        grid.add(filterComboBox, 0, 0);
        grid.add(phraseTextField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Konwertowanie wyniku na parę filtr i wyrażenie po kliknięciu przycisku OK.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                int filterValue = getFilterValue(filterComboBox.getValue());
                String phrase = phraseTextField.getText();
                return new Pair<>(filterValue, phrase);
            }
            return null;
        });

        Platform.runLater(() -> filterComboBox.requestFocus());

        // Wyświetl okno dialogowe i uzyskaj wynik.
        dialog.showAndWait().ifPresent(filterAndPhrase -> {
            int filter = filterAndPhrase.getKey();
            String phrase = filterAndPhrase.getValue();

            PdfGenerator pdfGenerator = new PdfGenerator();
            String desktopPath = System.getProperty("user.home") + "\\Desktop";
            String filePath = desktopPath + "\\user.pdf";
            try {
                pdfGenerator.manipulatePdf(filePath, 2, filter, phrase);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Błąd");
                alert.setHeaderText("Wystąpił Błąd");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }

    private int getFilterValue(String filter) {
        switch (filter) {
            case "Wszyscy Użytkownicy":
                return 1;
            case "Wybór grupy użytkowników":
                return 2;
            default:
                throw new IllegalArgumentException("Zły filtr.");
        }
    }
}


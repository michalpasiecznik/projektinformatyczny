package com.example.iie_szarzy_2024;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class TasksController {

    public Label additionalLabel;
    @FXML
    private TableView<String> additionalTableView;

    @FXML
    private Label titleLabel;

    @FXML
    public ListView<String> tasksListView;

    @FXML
    Connection connection;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    public Button changeStatusButton;

    private String loggedInUsername;

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

    @FXML
    public void setLoggedInUserType(String userType) {
        // Logika dostosowująca wyświetlane informacje w zależności od zalogowanego użytkownika
        if ("administrator".equals(userType)) {
            titleLabel.setText("Zadania | Jesteś administratorem");
        } else if ("kierownik".equals(userType)) {
            titleLabel.setText("Zadania | Jesteś kierownikiem");
        } else if ("pracownik".equals(userType)) {
            titleLabel.setText("Zadania | Jesteś pracownikem");
            addButton.setVisible(false);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }
    }

    public void setMainApp() {
    }

    @FXML
    public void initialize() {
        try {
            // Nawiązanie połączenia z bazą danych
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sklepszary", "root", "");
            // Ładowanie zadań z bazy danych
            loadTasksFromDatabase();

            // Dodanie nazw zadań do pierwszej kolumny w tabeli
            loadTaskNamesToTableView();

            // Dodaj kolumny dla godzin od 8:00 do 16:00
            for (int i = 8; i <= 16; i++) {
                TableColumn<String, String> hourColumn = new TableColumn<>(String.format("%02d:00", i));
                hourColumn.setCellValueFactory(data -> new SimpleStringProperty(""));
                additionalTableView.getColumns().add(hourColumn);
            }
        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas inicjalizacji bazy danych.");
        }
    }


    @FXML
    public void loadTaskNamesToTableView() {
        try {
            String query = "SELECT Opis FROM zadania";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> taskDescriptions = new ArrayList<>();
            while (resultSet.next()) {
                String taskDescription = resultSet.getString("Opis");
                taskDescriptions.add(taskDescription);
            }

            TableColumn<String, String> descriptionColumn = new TableColumn<>("Opis");
            descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));

            additionalTableView.getColumns().clear(); // Wyczyszczenie kolumn przed dodaniem nowej
            additionalTableView.getColumns().add(descriptionColumn);

            additionalTableView.getItems().setAll(taskDescriptions);

            preparedStatement.close();
        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas ładowania opisów zadań do widoku tabeli: " + e.getMessage());
        }
    }



    @FXML
    public void loadTasksFromDatabase() {
        try {
            String query = "SELECT * FROM zadania";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> tasks = new ArrayList<>();
            while (resultSet.next()) {
                int taskId = resultSet.getInt("IDZadania");
                String taskDescription = resultSet.getString("Opis");
                String status = resultSet.getString("Status");
                String managerName = getManagerNameByTaskId(taskId);
                String employeeName = getEmployeeNameByTaskId(taskId);
                // Pobranie dat rozpoczęcia i zakończenia zadania
                Time startTime = resultSet.getTime("DataRozpoczecia");
                Time endTime = resultSet.getTime("DataZakonczenia");

                // Formatowanie dat do wyświetlenia tylko godziny
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                String startDate = timeFormat.format(startTime);
                String endDate = timeFormat.format(endTime);

                // Tworzenie formatowanego łańcucha dla zadania z godzinami rozpoczęcia i zakończenia
                String formattedTask = String.format("%d | %s | Status: %s | Kierownik: %s | Pracownik: %s | Rozpoczęcie: %s | Zakończenie: %s |",
                        taskId, taskDescription, status, managerName, employeeName, startDate, endDate);
                tasks.add(formattedTask);
            }

            tasksListView.getItems().setAll(tasks);

            preparedStatement.close();
        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas ładowania zadań z bazy danych: " + e.getMessage());
        }
    }


    @FXML
    private void handleAddTask() {
        try {
            // Pobranie listy kierowników i pracowników
            List<String> managers = getManagersList();
            List<String> employees = getEmployeesList();
            // Lista godzin od 8:00 do 16:00
            List<String> hours = new ArrayList<>();
            for (int i = 8; i <= 16; i++) {
                hours.add(String.format("%02d:00", i));
            }

            // Pobranie dzisiejszej daty
            LocalDate currentDate = LocalDate.now();
            String today = currentDate.toString();

            // Wyświetlenie okna dialogowego z formularzem dodawania zadania
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Dodaj nowe zadanie");
            dialog.setHeaderText("Dodawanie nowego zadania");

            // Dodanie kontrolek do formularza dialogowego
            Label taskLabel = new Label("Opis zadania:");
            TextArea taskTextArea = new TextArea();
            taskTextArea.setPromptText("Opis zadania");

            Label managerLabel = new Label("Wybierz kierownika:");
            ChoiceBox<String> managerChoiceBox = new ChoiceBox<>();
            managerChoiceBox.getItems().addAll(managers);

            Label employeeLabel = new Label("Wybierz pracownika:");
            ChoiceBox<String> employeeChoiceBox = new ChoiceBox<>();
            employeeChoiceBox.getItems().addAll(employees);

            Label startTimeLabel = new Label("Wybierz godzinę rozpoczęcia:");
            ChoiceBox<String> startTimeChoiceBox = new ChoiceBox<>();
            startTimeChoiceBox.getItems().addAll(hours);

            Label endTimeLabel = new Label("Wybierz godzinę zakończenia:");
            ChoiceBox<String> endTimeChoiceBox = new ChoiceBox<>();
            endTimeChoiceBox.getItems().addAll(hours);

            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(taskLabel, taskTextArea, managerLabel, managerChoiceBox, employeeLabel, employeeChoiceBox, startTimeLabel, startTimeChoiceBox, endTimeLabel, endTimeChoiceBox);
            dialog.getDialogPane().setContent(vbox);

            // Dodanie przycisków do formularza dialogowego
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Obsługa zdarzenia zatwierdzenia formularza
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return taskTextArea.getText() + ";" + managerChoiceBox.getValue() + ";" + employeeChoiceBox.getValue() + ";" +
                            today + " " + startTimeChoiceBox.getValue() + ":00" + ";" + today + " " + endTimeChoiceBox.getValue() + ":00"; // Pełna data i godzina
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String[] parts = result.get().split(";");
                String taskDescription = parts[0];
                String managerName = parts[1];
                String employeeName = parts[2];
                String startDateTime = parts[3];
                String endDateTime = parts[4];

                int managerId = getManagerIdByName(managerName);
                int employeeId = getEmployeeIdByName(employeeName);

                // Dodanie nowego zadania do bazy danych
                String insertQuery = "INSERT INTO zadania (Opis, Status, DataUtworzenia, DataRozpoczecia, DataZakonczenia, IDKierownika) VALUES (?, 'Oczekujące', NOW(), ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                insertStatement.setString(1, taskDescription);
                insertStatement.setString(2, startDateTime);
                insertStatement.setString(3, endDateTime);
                insertStatement.setInt(4, managerId);
                insertStatement.executeUpdate();

                ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                int taskId;
                if (generatedKeys.next()) {
                    taskId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Nie udało się dodać nowego zadania, brak wygenerowanego ID.");
                }

                // Przydzielenie pracownika do zadania w bazie danych
                String assignEmployeeQuery = "INSERT INTO pracownicyzadania (IDPracownika, IDZadania) VALUES (?, ?)";
                PreparedStatement assignEmployeeStatement = connection.prepareStatement(assignEmployeeQuery);
                assignEmployeeStatement.setInt(1, employeeId);
                assignEmployeeStatement.setInt(2, taskId);
                assignEmployeeStatement.executeUpdate();

                // Dodanie nowego zadania do listy w interfejsie użytkownika
                String newTask = taskId + " | " + taskDescription + " | Status: Oczekujące | Kierownik: " + managerName + " | Pracownik: " + employeeName + " | Rozpoczęcie: " + startDateTime + " | Zakończenie: " + endDateTime + " |";
                tasksListView.getItems().add(newTask);
            }
        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas dodawania zadania: " + e.getMessage());
        }
    }



    // Metody pomocnicze:
    private List<String> getManagersList() throws SQLException {
        // Pobieranie listy kierowników
        List<String> managers = new ArrayList<>();
        String query = "SELECT Imie, Nazwisko FROM kierownicy";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            managers.add(resultSet.getString("Imie") + " " + resultSet.getString("Nazwisko"));
        }
        return managers;
    }

    private List<String> getEmployeesList() throws SQLException {
        // Pobieranie listy pracowników
        List<String> employees = new ArrayList<>();
        String query = "SELECT Imie, Nazwisko FROM pracownicy";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            employees.add(resultSet.getString("Imie") + " " + resultSet.getString("Nazwisko"));
        }
        return employees;
    }

    private int getEmployeeIdByName(String employeeName) throws SQLException {
        // Pobieranie ID pracownika na podstawie imienia i nazwiska
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


    private int getManagerIdByName(String managerName) throws SQLException {
        // Pobieranie ID kierownika na podstawie imienia i nazwiska
        String[] names = managerName.split(" ");
        String firstName = names[0];
        String lastName = names[1];
        String query = "SELECT IDKierownika FROM kierownicy WHERE Imie = ? AND Nazwisko = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, firstName);
        preparedStatement.setString(2, lastName);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("IDKierownika");
        }
        return -1;
    }


    @FXML
    private void handleEditTask() {
        int selectedIndex = tasksListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            try {
                // Pobranie wybranego zadania z listy
                String selectedTask = tasksListView.getItems().get(selectedIndex);
                int taskId = Integer.parseInt(selectedTask.split(" \\| ")[0].trim()); // Poprawienie formatu stringa
                String taskDescription = selectedTask.split(" \\| ")[1].split("Status:")[0].trim(); // Poprawienie formatu stringa
                String currentManager = selectedTask.split("Kierownik:")[1].split("\\|")[0].trim(); // Poprawienie formatu stringa
                String currentEmployee = selectedTask.split("Pracownik:")[1].split("\\|")[0].trim(); // Poprawienie formatu stringa

                // Wyświetlenie okna dialogowego z formularzem edycji zadania
                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Edytuj zadanie");
                dialog.setHeaderText("Edycja zadania ID: " + taskId);

                // Dodanie kontrolek do formularza dialogowego
                Label taskLabel = new Label("Opis zadania:");
                TextArea taskTextArea = new TextArea(taskDescription);
                taskTextArea.setPromptText("Opis zadania");

                Label managerLabel = new Label("Wybierz nowego kierownika:");
                ChoiceBox<String> managerChoiceBox = new ChoiceBox<>();
                managerChoiceBox.getItems().addAll(getManagersList());
                managerChoiceBox.setValue(currentManager);

                Label employeeLabel = new Label("Wybierz nowego pracownika:");
                ChoiceBox<String> employeeChoiceBox = new ChoiceBox<>();
                employeeChoiceBox.getItems().addAll(getEmployeesList());
                employeeChoiceBox.setValue(currentEmployee);

                VBox vbox = new VBox(10);
                vbox.getChildren().addAll(taskLabel, taskTextArea, managerLabel, managerChoiceBox, employeeLabel, employeeChoiceBox);
                dialog.getDialogPane().setContent(vbox);

                // Dodanie przycisków do formularza dialogowego
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                // Obsługa zdarzenia zatwierdzenia formularza
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK) {
                        return taskTextArea.getText() + ";" + managerChoiceBox.getValue() + ";" + employeeChoiceBox.getValue();
                    }
                    return null;
                });

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String[] parts = result.get().split(";");
                    String newTaskDescription = parts[0];
                    String newManagerName = parts[1];
                    String newEmployeeName = parts[2];

                    int newManagerId = getManagerIdByName(newManagerName);
                    int newEmployeeId = getEmployeeIdByName(newEmployeeName);

                    // Aktualizacja opisu zadania w bazie danych
                    String updateTaskQuery = "UPDATE zadania SET Opis = ? WHERE IDZadania = ?";
                    PreparedStatement updateTaskStatement = connection.prepareStatement(updateTaskQuery);
                    updateTaskStatement.setString(1, newTaskDescription);
                    updateTaskStatement.setInt(2, taskId);
                    updateTaskStatement.executeUpdate();
                    updateTaskStatement.close();

                    // Aktualizacja przypisanego kierownika do zadania w bazie danych
                    String updateManagerQuery = "UPDATE zadania SET IDKierownika = ? WHERE IDZadania = ?";
                    PreparedStatement updateManagerStatement = connection.prepareStatement(updateManagerQuery);
                    updateManagerStatement.setInt(1, newManagerId);
                    updateManagerStatement.setInt(2, taskId);
                    updateManagerStatement.executeUpdate();
                    updateManagerStatement.close();

                    // Aktualizacja przypisanego pracownika do zadania w bazie danych
                    String updateEmployeeQuery = "UPDATE pracownicyzadania SET IDPracownika = ? WHERE IDZadania = ?";
                    PreparedStatement updateEmployeeStatement = connection.prepareStatement(updateEmployeeQuery);
                    updateEmployeeStatement.setInt(1, newEmployeeId);
                    updateEmployeeStatement.setInt(2, taskId);
                    updateEmployeeStatement.executeUpdate();
                    updateEmployeeStatement.close();

                    // Aktualizacja listy zadań w interfejsie użytkownika
                    String updatedTask = taskId + " | " + newTaskDescription + " | Status: " + getTaskStatusById(taskId) + " | Kierownik: " + newManagerName + " | Pracownik: " + newEmployeeName + " |";
                    tasksListView.getItems().set(selectedIndex, updatedTask);
                }

            } catch (Exception e) {
                System.out.println("Wystąpił błąd podczas edycji zadania: " + e.getMessage());
            }
        }
    }

    private String getTaskStatusById(int taskId) throws SQLException {
        // Pobieranie statusu zadania na podstawie ID zadania
        String query = "SELECT Status FROM zadania WHERE IDZadania = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, taskId);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("Status");
        }
        return "";
    }

    @FXML
    void handleChangeStatus() {
        int selectedIndex = tasksListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            try {
                // Pobranie wybranego zadania z listy
                String selectedTask = tasksListView.getItems().get(selectedIndex);
                int taskId = Integer.parseInt(selectedTask.split(" \\|")[0].trim());

                // Wyświetlenie okna dialogowego z formularzem zmiany statusu zadania
                ChoiceDialog<String> dialog = new ChoiceDialog<>("Oczekujące", "W toku", "Zakończone");
                dialog.setTitle("Zmień status zadania");
                dialog.setHeaderText("Zmiana statusu zadania ID: " + taskId);
                dialog.setContentText("Nowy status zadania:");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String newStatus = result.get();

                    // Aktualizacja statusu zadania w bazie danych
                    String updateQuery = "UPDATE zadania SET Status = ? WHERE IDZadania = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setString(1, newStatus);
                    updateStatement.setInt(2, taskId);
                    updateStatement.executeUpdate();
                    updateStatement.close();

                    // Aktualizacja listy zadań w interfejsie użytkownika
                    String[] parts = selectedTask.split("\\|");
                    parts[2] = " Status: " + newStatus;
                    tasksListView.getItems().set(selectedIndex, String.join(" |", parts).trim());
                }

            } catch (Exception e) {
                System.out.println("Wystąpił błąd podczas zmiany statusu zadania: " + e.getMessage());
            }
        }
    }

    // Metoda pomocnicza do pobierania imienia i nazwiska kierownika na podstawie ID zadania
    private String getManagerNameByTaskId(int taskId) throws SQLException {
        String query = "SELECT k.Imie, k.Nazwisko FROM kierownicy k JOIN zadania z ON k.IDKierownika = z.IDKierownika WHERE z.IDZadania = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, taskId);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("Imie") + " " + resultSet.getString("Nazwisko");
        }
        return "";
    }

    // Metoda pomocnicza do pobierania imienia i nazwiska pracownika na podstawie ID zadania
    private String getEmployeeNameByTaskId(int taskId) throws SQLException {
        String query = "SELECT p.Imie, p.Nazwisko FROM pracownicy p JOIN pracownicyzadania pz ON p.IDPracownika = pz.IDPracownika JOIN zadania z ON pz.IDZadania = z.IDZadania WHERE z.IDZadania = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, taskId);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("Imie") + " " + resultSet.getString("Nazwisko");
        }
        return "";
    }


    @FXML
    private void handleDeleteTask() {
        // Usunięcie wybranego zadania
        int selectedIndex = tasksListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            try {
                // Pobranie IDZadania z wybranego zadania w liście
                String selectedTask = tasksListView.getItems().get(selectedIndex);
                int taskId = Integer.parseInt(selectedTask.split(" |")[0].trim());

                // Usunięcie wszystkich powiązań z zadaniem w tabeli pracownicyzadania
                String deleteRelationsQuery = "DELETE FROM pracownicyzadania WHERE IDZadania = ?";
                PreparedStatement deleteRelationsStatement = connection.prepareStatement(deleteRelationsQuery);
                deleteRelationsStatement.setInt(1, taskId);
                deleteRelationsStatement.executeUpdate();
                deleteRelationsStatement.close();

                // Usunięcie zadania z tabeli zadania
                String deleteTaskQuery = "DELETE FROM zadania WHERE IDZadania = ?";
                PreparedStatement deleteTaskStatement = connection.prepareStatement(deleteTaskQuery);
                deleteTaskStatement.setInt(1, taskId);
                deleteTaskStatement.executeUpdate();

                // Usunięcie zadania z listy
                tasksListView.getItems().remove(selectedIndex);

                deleteTaskStatement.close();

            } catch (Exception e) {
                System.out.println("Wystąpił błąd podczas inicjalizacji bazy danych.");
            }
        }
    }


    @FXML
    private void handleLogout() {
        // Wylogowanie użytkownika
        Main newMainApp = new Main();
        Stage stage = (Stage) tasksListView.getScene().getWindow();
        newMainApp.start(stage);
    }


    @FXML
    private void handleProfile() {
        // Wyświetlenie profilu użytkownika
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("profile-view.fxml"));
            Parent root1 = loader.load();
            ProfileController profileController = loader.getController();
            profileController.initialize(loggedInUsername); // Pass the logged-in username
            Stage stageProfile = new Stage();
            stageProfile.setScene(new Scene(root1));
            stageProfile.setTitle("Moduł Profil");
            stageProfile.show();
        } catch (IOException e) {
            System.out.println("Wystąpił błąd podczas ładowania modułu profilu: " + e.getMessage());
        }
    }
}

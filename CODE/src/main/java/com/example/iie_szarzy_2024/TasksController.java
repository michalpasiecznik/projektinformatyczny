package com.example.iie_szarzy_2024;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import javafx.util.converter.LocalTimeStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class TasksController {

    @FXML
    public Label additionalLabel;

    @FXML
    private TableColumn<?, ?> taskNameColumn;

    @FXML
    public Button StorageButton;

    @FXML
    private Label titleLabel;

    @FXML
    Connection connection;

    @FXML
    private Button addButton;

    @FXML
    private Button adminPanelButton;

    @FXML
    private Button editTaskButton;

    @FXML
    private TableView<String> additionalTableView;

    @FXML
    private Button deleteButton;

    @FXML
    public Button changeStatusButton;

    private String loggedInUsername;

    private String userType; // Add this line to store the user type

    private Main mainApp;

    @FXML
    private TableView<Task> tasksTableView;
    @FXML
    private TableColumn<Task, Integer> taskIdColumn;
    @FXML
    private TableColumn<Task, String> taskDescriptionColumn;
    @FXML
    private TableColumn<Task, String> taskStatusColumn;
    @FXML
    private TableColumn<Task, String> taskPriorityColumn;
    @FXML
    private TableColumn<Task, String> taskCategoryColumn;
    @FXML
    private TableColumn<Task, String> taskEmployeeColumn;
    @FXML
    private TableColumn<Task, String> taskStartColumn;
    @FXML
    private TableColumn<Task, String> taskDeadlineColumn;
    @FXML
    private TableColumn<Task, String> taskCreationDateColumn;
    @FXML
    private TableColumn<Task, String> taskModificationDateColumn;

    public String username;
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    private static final Logger logger = LoggerFactory.getLogger(TasksController.class);

    public void setLoggedInUsername(String loggedInUsername) {
        this.username = loggedInUsername;
        this.loggedInUsername = loggedInUsername;
        logger.info("Ustawiono nazwę użytkownika na '{}'", loggedInUsername);
        loadTasksFromDatabase(loggedInUsername);

    }

    @FXML
    public void setLoggedInUserType(String userType) {
        this.userType = userType; // Przechowywanie userType
        if ("administrator".equals(userType)) {
            titleLabel.setText("Zadania | Jesteś administratorem");
            logger.info("Zalogowany użytkownik to administrator.");
        } else if ("kierownik".equals(userType)) {
            titleLabel.setText("Zadania | Jesteś kierownikiem");
            adminPanelButton.setVisible(false);
            logger.info("Zalogowany użytkownik to kierownik.");
        } else if ("pracownik".equals(userType)) {
            titleLabel.setText("Zadania | Jesteś pracownikiem");
            addButton.setVisible(false);
            editTaskButton.setVisible(false);
            deleteButton.setVisible(false);
            adminPanelButton.setVisible(false);

            logger.info("Zalogowany użytkownik to pracownik.");
        }
        loadTasksFromDatabase(loggedInUsername);
    }

    @FXML
    public void initialize() {
        taskIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        taskDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        taskStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        taskPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        taskCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        taskEmployeeColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        taskStartColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        taskDeadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        taskCreationDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        taskModificationDateColumn.setCellValueFactory(new PropertyValueFactory<>("modificationDate"));

        // Pozwolenie Kolumnom na rozszerzenie
        taskIdColumn.setResizable(true);
        taskDescriptionColumn.setResizable(true);
        taskStatusColumn.setResizable(true);
        taskPriorityColumn.setResizable(true);
        taskEmployeeColumn.setResizable(true);
        taskStartColumn.setResizable(true);
        taskDeadlineColumn.setResizable(true);
        taskCreationDateColumn.setResizable(true);
        taskModificationDateColumn.setResizable(true);

        // Włączanie równego rozkładu kolumn
        tasksTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        // Konfiguracja niestandardowego wiersza dla TableView
        tasksTableView.setRowFactory(new Callback<TableView<Task>, TableRow<Task>>() {
            @Override
            public TableRow<Task> call(TableView<Task> tableView) {
                return new TableRow<Task>() {
                    @Override
                    protected void updateItem(Task task, boolean empty) {
                        super.updateItem(task, empty);
                        if (task == null || empty) {
                            setStyle("");
                        } else {
                            switch (task.getPriority().toLowerCase()) {
                                case "niski":
                                    setStyle("-fx-background-color: #8ee233");
                                    break;
                                case "średni":
                                    setStyle("-fx-background-color: #eaf679");
                                    break;
                                case "wysoki":
                                    setStyle("-fx-background-color: #c72e33");
                                    break;
                                default:
                                    setStyle("");
                                    break;
                            }
                        }
                    }
                };
            }
        });

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sklepszary2", "root", "");
            logger.info("Połączenie z bazą danych zostało nawiązane.");
            loadTasksFromDatabase(loggedInUsername);
            loadTaskNamesToTableView();

            // Set initial table height based on the number of tasks
            setInitialTableHeight();
        } catch (Exception e) {
            showAlert("Błąd inicjalizacji", "Wystąpił błąd podczas inicjalizacji bazy danych.");
            logger.error("Wystąpił błąd podczas inicjalizacji bazy danych: {}", e.getMessage());
        }
    }

    private void setInitialTableHeight() {
        int taskCount = tasksTableView.getItems().size();
        double initialHeight = taskCount * 37.5 + 37.5;
        initialHeight = Math.max(50, Math.min(300, initialHeight)); // Ensure the height is within 50 to 300

        tasksTableView.setPrefHeight(initialHeight);
        additionalTableView.setPrefHeight(initialHeight); // Assuming you want the same height for additionalTableView
    }

    @FXML
    public void loadTaskNamesToTableView() {
        try {
            String query = "SELECT Opis, DataRozpoczecia, DataZakonczenia, Priorytet FROM zadania";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> taskDescriptions = new ArrayList<>();
            Map<String, Pair<LocalDateTime, LocalDateTime>> taskTimes = new HashMap<>();
            Map<String, String> taskPriorities = new HashMap<>();
            while (resultSet.next()) {
                String taskDescription = resultSet.getString("Opis");
                LocalDateTime startTime = resultSet.getTimestamp("DataRozpoczecia").toLocalDateTime();
                LocalDateTime endTime = resultSet.getTimestamp("DataZakonczenia").toLocalDateTime();
                String priority = resultSet.getString("Priorytet");
                taskDescriptions.add(taskDescription);
                taskTimes.put(taskDescription, new Pair<>(startTime, endTime));
                taskPriorities.put(taskDescription, priority);
            }

            TableColumn<String, String> descriptionColumn = new TableColumn<>("Opis");
            descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
            descriptionColumn.setPrefWidth(200); // Ustaw preferowaną szerokość

            additionalTableView.getColumns().clear();
            additionalTableView.getColumns().add(descriptionColumn);

            LocalTime startTime = LocalTime.of(8, 0);
            LocalTime endTime = LocalTime.of(16, 0);
            List<String> quarterHourIntervals = new ArrayList<>();
            while (startTime.isBefore(endTime) || startTime.equals(endTime)) {
                quarterHourIntervals.add(startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                startTime = startTime.plusMinutes(15);
            }

            for (String interval : quarterHourIntervals) {
                TableColumn<String, String> hourColumn = new TableColumn<>(interval);
                hourColumn.setCellValueFactory(cellData -> {
                    String taskDescription = cellData.getValue();
                    Pair<LocalDateTime, LocalDateTime> taskTime = taskTimes.get(taskDescription);
                    if (taskTime != null) {
                        LocalDateTime startTimeTask = taskTime.getKey();
                        LocalDateTime endTimeTask = taskTime.getValue();
                        LocalTime cellTime = LocalTime.parse(interval);
                        if ((cellTime.equals(startTimeTask.toLocalTime()) || cellTime.isAfter(startTimeTask.toLocalTime())) &&
                                (cellTime.equals(endTimeTask.toLocalTime()) || cellTime.isBefore(endTimeTask.toLocalTime()))) {
                            return new SimpleStringProperty("  ");
                        }
                    }
                    return new SimpleStringProperty("");
                });

                hourColumn.setCellFactory(column -> new TableCell<String, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            String taskDescription = getTableView().getItems().get(getIndex());
                            String priority = taskPriorities.get(taskDescription);
                            if ("  ".equals(item)) {
                                switch (priority.toLowerCase()) {
                                    case "niski":
                                        setStyle("-fx-background-color: #8ee233");
                                        break;
                                    case "średni":
                                        setStyle("-fx-background-color: #eaf679");
                                        break;
                                    case "wysoki":
                                        setStyle("-fx-background-color: #c72e33");
                                        break;
                                    default:
                                        setStyle("");
                                        break;
                                }
                            } else {
                                setStyle("");
                            }
                        }
                    }
                });
                descriptionColumn.setPrefWidth(300);
                hourColumn.setPrefWidth(60); // Ustawienie stałej szerokości dla kolumny z godzinami
                additionalTableView.getColumns().add(hourColumn);
            }

            additionalTableView.getItems().setAll(taskDescriptions);

            // Wyłącz sortowanie dla wszystkich kolumn w additionalTableView
            for (TableColumn<?, ?> column : additionalTableView.getColumns()) {
                column.setSortable(false);
            }
            additionalTableView.setSelectionModel(null);
            descriptionColumn.setReorderable(false);
            for (TableColumn<?, ?> column : additionalTableView.getColumns()) {
                column.setReorderable(false);
            }


            preparedStatement.close();
        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas ładowania opisów zadań do widoku tabeli: " + e.getMessage());
        }
    }

    @FXML
    public void loadTasksFromDatabase(String loggedInUsername) {
        try {
            String query;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;

            if ("pracownik".equals(this.userType)) { // Upewnienie się, że userType nie zawiera dodatkowych białych znaków
                query = "SELECT z.* FROM zadania z " +
                        "JOIN pracownicyzadania pz ON z.IDZadania = pz.IDZadania " +
                        "JOIN pracownicy p ON pz.IDPracownika = p.IDPracownika " +
                        "WHERE z.Status != 'Archiwum' AND p.Login = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, loggedInUsername);
            } else {
                // Jeśli użytkownik ma roleId równy 1, użyj zapytania z warunkiem WHERE
                query = "SELECT * FROM zadania";
                preparedStatement = connection.prepareStatement(query);
            }

            resultSet = preparedStatement.executeQuery();

            List<Task> tasks = new ArrayList<>();
            while (resultSet.next()) {
                int taskId = resultSet.getInt("IDZadania");
                String taskDescription = resultSet.getString("Opis");
                String status = resultSet.getString("Status");
                String priority = resultSet.getString("Priorytet");
                String category = resultSet.getString("Kategoria");
                String employeeName = getEmployeeNameByTaskId(taskId);
                String startDate = resultSet.getString("DataRozpoczecia");
                String deadline = resultSet.getString("DataZakonczenia");
                String creationDate = resultSet.getString("DataUtworzenia");
                String modificationDate = resultSet.getString("DataModyfikacji");

                tasks.add(new Task(taskId, taskDescription, status, priority, category, employeeName, startDate, deadline, creationDate, modificationDate));
            }

            tasksTableView.getItems().setAll(tasks);
            preparedStatement.close();
            logger.info("Załadowano zadania z bazy danych.");
        } catch (Exception e) {
            showAlert("Błąd ładowania zadań", "Wystąpił błąd podczas ładowania zadań z bazy danych.");
            logger.error("Wystąpił błąd podczas ładowania zadań z bazy danych: {}", e.getMessage());
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
    private void handleAddTask() {
        try {
            List<String> employees = getEmployeesList();

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Dodaj nowe zadanie");
            dialog.setHeaderText("Dodawanie nowego zadania");

            // Create controls for input
            Label taskLabel = new Label("Opis zadania:");
            TextArea taskTextArea = new TextArea();
            taskTextArea.setPromptText("Opis zadania");

            Label priorityLabel = new Label("Priorytet zadania:");
            ChoiceBox<String> priorityChoiceBox = new ChoiceBox<>();
            priorityChoiceBox.getItems().addAll("Niski", "Średni", "Wysoki");

            Label employeeLabel = new Label("Wybierz pracownika:");
            ChoiceBox<String> employeeChoiceBox = new ChoiceBox<>();
            employeeChoiceBox.getItems().addAll(employees);

            // Create ComboBoxes for time selection
            Label startTimeLabel = new Label("Godzina rozpoczęcia:");
            ComboBox<LocalTime> startTimeComboBox = new ComboBox<>();
            startTimeComboBox.getItems().addAll(getTimeOptions());

            Label endTimeLabel = new Label("Godzina zakończenia:");
            ComboBox<LocalTime> endTimeComboBox = new ComboBox<>();
            endTimeComboBox.getItems().addAll(getTimeOptions());

            // Set default values
            startTimeComboBox.setValue(LocalTime.of(8, 0));
            endTimeComboBox.setValue(LocalTime.of(16, 0));

            // Add controls to layout
            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(
                    taskLabel, taskTextArea,
                    priorityLabel, priorityChoiceBox,
                    employeeLabel, employeeChoiceBox,
                    startTimeLabel, startTimeComboBox,
                    endTimeLabel, endTimeComboBox
            );
            dialog.getDialogPane().setContent(vbox);

            // Add buttons to dialog
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Handle result conversion
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    if (taskTextArea.getText().isEmpty() || priorityChoiceBox.getValue() == null ||
                            employeeChoiceBox.getValue() == null || startTimeComboBox.getValue() == null ||
                            endTimeComboBox.getValue() == null) {
                        showAlert("Niekompletne dane", "Wypełnij wszystkie pola przed zatwierdzeniem zadania.");
                        return null; // Return null to prevent the dialog from closing
                    }

                    LocalDate currentDate = LocalDate.now();
                    String startDate = currentDate.toString() + " " + startTimeComboBox.getValue().toString();
                    String endDate = currentDate.toString() + " " + endTimeComboBox.getValue().toString();

                    return taskTextArea.getText() + ";" + employeeChoiceBox.getValue() + ";" +
                            priorityChoiceBox.getValue() + ";" + startDate + ";" + endDate;
                }
                return null;
            });

            // Show dialog and manage results
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Potwierdzenie dodania");
                confirmationAlert.setHeaderText("Dodanie Zadania");
                confirmationAlert.setContentText("Czy na pewno chcesz dodać to zadanie?");

                Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    String[] parts = result.get().split(";");
                    String taskDescription = parts[0];
                    String employeeName = parts[1];
                    String priority = parts[2];
                    String startDate = parts[3];
                    String endDate = parts[4];
                    int employeeId = getEmployeeIdByName(employeeName);

                    // Insert new task into database
                    String insertQuery = "INSERT INTO zadania (Opis, Status, Priorytet, Kategoria, DataRozpoczecia, DataZakonczenia, DataUtworzenia, DataModyfikacji, IDPracownika) " +
                            "VALUES (?, 'Oczekujące', ?, 'Sklep', ?, ?, NOW(), NOW(), ?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    insertStatement.setString(1, taskDescription);
                    insertStatement.setString(2, priority);
                    insertStatement.setString(3, startDate);
                    insertStatement.setString(4, endDate);
                    insertStatement.setInt(5, employeeId);
                    insertStatement.executeUpdate();

                    // Get generated task ID
                    ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                    int taskId;
                    if (generatedKeys.next()) {
                        taskId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Nie udało się dodać nowego zadania, brak wygenerowanego ID.");
                    }

                    // Assign employee to task
                    String assignEmployeeQuery = "INSERT INTO pracownicyzadania (IDPracownika, IDZadania) VALUES (?, ?)";
                    PreparedStatement assignEmployeeStatement = connection.prepareStatement(assignEmployeeQuery);
                    assignEmployeeStatement.setInt(1, employeeId);
                    assignEmployeeStatement.setInt(2, taskId);
                    assignEmployeeStatement.executeUpdate();

                    loadTasksFromDatabase(loggedInUsername);
                    loadTaskNamesToTableView();
                    setInitialTableHeight();
                    logger.info("Dodano nowe zadanie: {}", taskDescription);
                }
            }
        } catch (Exception e) {
            showAlert("Błąd dodawania zadania", "Wystąpił błąd podczas dodawania zadania: " + e.getMessage());
            logger.error("Wystąpił błąd podczas dodawania zadania: {}", e.getMessage());
        }
    }


    // Method to generate time options from 8:00 to 16:00 in 15-minute intervals
    private List<LocalTime> getTimeOptions() {
        List<LocalTime> timeOptions = new ArrayList<>();
        LocalTime time = LocalTime.of(8, 0);
        while (time.isBefore(LocalTime.of(16, 0).plusMinutes(15))) {
            timeOptions.add(time);
            time = time.plusMinutes(15);
        }
        return timeOptions;
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = tasksTableView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            try {
                int taskId = selectedTask.getId();

                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Potwierdzenie usunięcia");
                confirmationAlert.setHeaderText("Usunięcie Zadania");
                confirmationAlert.setContentText("Czy na pewno chcesz usunąć zadanie?");

                Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    String deleteRelationsQuery = "DELETE FROM pracownicyzadania WHERE IDZadania = ?";
                    try (PreparedStatement deleteRelationsStatement = connection.prepareStatement(deleteRelationsQuery)) {
                        deleteRelationsStatement.setInt(1, taskId);
                        deleteRelationsStatement.executeUpdate();
                    }

                    String deleteTaskQuery = "DELETE FROM zadania WHERE IDZadania = ?";
                    try (PreparedStatement deleteTaskStatement = connection.prepareStatement(deleteTaskQuery)) {
                        deleteTaskStatement.setInt(1, taskId);
                        deleteTaskStatement.executeUpdate();
                    }

                    tasksTableView.getItems().remove(selectedTask);
                    loadTasksFromDatabase(loggedInUsername);
                    loadTaskNamesToTableView();
                    setInitialTableHeight();
                    logger.info("Usunięto zadanie ID {}", taskId);
                }
            } catch (Exception e) {
                showAlert("Błąd usuwania zadania", "Wystąpił błąd podczas usuwania zadania: " + e.getMessage());
                logger.error("Wystąpił błąd podczas usuwania zadania: {}", e.getMessage());
            }
        } else {
            showAlert("Nie wybrano zadania", "Proszę wybrać zadanie do usunięcia.");
            logger.warn("Nie wybrano zadania do usunięcia.");
        }
    }

    private List<String> getEmployeesList() throws SQLException {
        List<String> employees = new ArrayList<>();
        String query = "SELECT Imie, Nazwisko FROM pracownicy WHERE IDRoli = 1 AND StatusKonta = 'Aktywne'";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            employees.add(resultSet.getString("Imie") + " " + resultSet.getString("Nazwisko"));
        }
        logger.info("Pobrano listę pracowników.");
        return employees;
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

    @FXML
    private void handleEditTask() {
        Task selectedTask = tasksTableView.getSelectionModel().getSelectedItem();

        if (selectedTask == null) {
            showAlert("Brak zaznaczonego zadania", "Proszę zaznaczyć zadanie do edycji.");
            return;
        }

        try {
            List<String> employees = getEmployeesList();

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Edytuj zadanie");
            dialog.setHeaderText("Edycja zadania");

            Label taskLabel = new Label("Opis zadania:");
            TextArea taskTextArea = new TextArea(selectedTask.getDescription());
            taskTextArea.setPromptText("Opis zadania");

            Label priorityLabel = new Label("Priorytet zadania:");
            ChoiceBox<String> priorityChoiceBox = new ChoiceBox<>();
            priorityChoiceBox.getItems().addAll("Niski", "Średni", "Wysoki");
            priorityChoiceBox.setValue(selectedTask.getPriority());

            Label employeeLabel = new Label("Wybierz pracownika:");
            ChoiceBox<String> employeeChoiceBox = new ChoiceBox<>();
            employeeChoiceBox.getItems().addAll(employees);
            employeeChoiceBox.setValue(selectedTask.getEmployeeName());

            // Time selection for start and end times
            Label startTimeLabel = new Label("Godzina rozpoczęcia:");
            ComboBox<LocalTime> startTimeComboBox = createTimeComboBox();
            LocalTime startTime = LocalTime.parse(selectedTask.getStartDate().split(" ")[1]); // Assuming time is in format "yyyy-MM-dd HH:mm:ss"
            startTimeComboBox.setValue(startTime);

            Label endTimeLabel = new Label("Godzina zakończenia:");
            ComboBox<LocalTime> endTimeComboBox = createTimeComboBox();
            LocalTime endTime = LocalTime.parse(selectedTask.getDeadline().split(" ")[1]); // Assuming time is in format "yyyy-MM-dd HH:mm:ss"
            endTimeComboBox.setValue(endTime);

            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(
                    taskLabel, taskTextArea,
                    priorityLabel, priorityChoiceBox,
                    employeeLabel, employeeChoiceBox,
                    startTimeLabel, startTimeComboBox,
                    endTimeLabel, endTimeComboBox
            );
            dialog.getDialogPane().setContent(vbox);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    if (taskTextArea.getText().isEmpty() || priorityChoiceBox.getValue() == null ||
                            employeeChoiceBox.getValue() == null) {
                        showAlert("Niekompletne dane", "Proszę wypełnić wszystkie pola.");
                        return null;
                    }

                    LocalDateTime startDate = LocalDateTime.now()
                            .withHour(startTimeComboBox.getValue().getHour())
                            .withMinute(startTimeComboBox.getValue().getMinute())
                            .withSecond(0); // Ensure seconds are set to 0

                    LocalDateTime endDate = LocalDateTime.now()
                            .withHour(endTimeComboBox.getValue().getHour())
                            .withMinute(endTimeComboBox.getValue().getMinute())
                            .withSecond(0); // Ensure seconds are set to 0

                    String startDateTime = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    String endDateTime = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    return taskTextArea.getText() + ";" + employeeChoiceBox.getValue() + ";" + priorityChoiceBox.getValue() + ";" + startDateTime + ";" + endDateTime;
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Potwierdzenie edycji");
                confirmationAlert.setHeaderText("Edycja Zadania");
                confirmationAlert.setContentText("Czy na pewno chcesz edytować to zadanie?");

                Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    String[] parts = result.get().split(";");
                    String taskDescription = parts[0];
                    String employeeName = parts[1];
                    String priority = parts[2];
                    String startDateTime = parts[3];
                    String endDateTime = parts[4];
                    int employeeId = getEmployeeIdByName(employeeName);

                    // Update database with the new information
                    String updateQuery = "UPDATE zadania SET Opis = ?, Priorytet = ?, IDPracownika = ?, DataRozpoczecia = ?, DataZakonczenia = ?, DataModyfikacji = NOW() WHERE IDZadania = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setString(1, taskDescription);
                    updateStatement.setString(2, priority);
                    updateStatement.setInt(3, employeeId);
                    updateStatement.setString(4, startDateTime);
                    updateStatement.setString(5, endDateTime);
                    updateStatement.setInt(6, selectedTask.getId());
                    updateStatement.executeUpdate();

                    // Update selectedTask with new information
                    selectedTask.setDescription(taskDescription);
                    selectedTask.setPriority(priority);
                    selectedTask.setEmployeeName(employeeName);
                    selectedTask.setStartDate(startDateTime);
                    selectedTask.setDeadline(endDateTime);

                    // Refresh the table view to display updated data
                    loadTasksFromDatabase(loggedInUsername);
                    loadTaskNamesToTableView(); // Refresh additionalTableView to display updated data
                    logger.info("Zaktualizowano zadanie: {}", selectedTask);
                }
            }
        } catch (Exception e) {
            showAlert("Błąd edycji zadania", "Wystąpił błąd podczas edycji zadania: " + e.getMessage());
            logger.error("Wystąpił błąd podczas edycji zadania: {}", e.getMessage());
        }
    }


    private ComboBox<LocalTime> createTimeComboBox() {
        ComboBox<LocalTime> comboBox = new ComboBox<>();
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(16, 0);

        while (startTime.isBefore(endTime) || startTime.equals(endTime)) {
            comboBox.getItems().add(startTime);
            startTime = startTime.plusMinutes(15);
        }
        return comboBox;
    }




    @FXML
    private void handleChangeStatus() {
        if (tasksTableView != null) {
            Task selectedTask = tasksTableView.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                int taskId = selectedTask.getId();
                String currentStatus = selectedTask.getStatus();

                // Tworzenie dialogu wyboru nowego statusu
                ChoiceDialog<String> dialog;
                if (userType != "pracownik") {
                    dialog = new ChoiceDialog<>(currentStatus, "Oczekujące", "W toku", "Zakończone", "Archiwum");
                } else {
                    dialog = new ChoiceDialog<>(currentStatus, "Oczekujące", "W toku", "Zakończone");
                }
                dialog.setTitle("Zmień status zadania");
                dialog.setHeaderText("Zmiana statusu zadania o ID: " + taskId);
                dialog.setContentText("Nowy status zadania:");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String newStatus = result.get();

                    // Sprawdzenie, czy zmiana statusu jest dozwolona
                    if (isStatusChangeAllowed(currentStatus, newStatus,userType)) {
                        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmationAlert.setTitle("Potwierdzenie zmiany statusu");
                        confirmationAlert.setHeaderText("Zmiana Statusu Zadania");
                        confirmationAlert.setContentText("Czy na pewno chcesz zmienić status zadania?");

                        Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
                        if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                            try {
                                String updateQuery = "UPDATE zadania SET Status = ?, DataModyfikacji = NOW() WHERE IDZadania = ?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setString(1, newStatus);
                                    updateStatement.setInt(2, taskId);
                                    updateStatement.executeUpdate();
                                }

                                selectedTask.setStatus(newStatus);
                                logger.info("Zmieniono status zadania ID {} na {}", taskId, newStatus);

                                // Odświeżanie widoku tabeli
                                loadTasksFromDatabase(loggedInUsername);

                                // Jeśli zadanie ma kategorię "Magazyn" i zmienia się na "Zakończone", wywołaj metodę edycji produktu
                                if ("Magazyn".equals(selectedTask.getCategory()) && "Zakończone".equals(newStatus)) {
                                    String productName = extractProductNameFromDescription(selectedTask.getDescription());
                                    handleEditProduct(productName);
                                }

                            } catch (SQLException e) {
                                showAlert("Błąd zmiany statusu", "Wystąpił błąd podczas zmiany statusu zadania: " + e.getMessage());
                                logger.error("Wystąpił błąd podczas zmiany statusu zadania: {}", e.getMessage());
                            }
                        }
                    } else {
                        showAlert("Nieprawidłowa zmiana statusu", "Nie można zmienić statusu zadania z \"" + currentStatus + "\" na \"" + newStatus + "\".");
                        logger.warn("Nieprawidłowa zmiana statusu z {} na {}", currentStatus, newStatus);
                    }
                }
            } else {
                showAlert("Błąd", "Proszę wybrać zadanie.");
                logger.error("Nie wybrano zadania.");
            }
        } else {
            showAlert("Błąd", "tasksTableView nie został zainicjalizowany.");
            logger.error("tasksTableView nie został zainicjalizowany.");
        }
    }

    private boolean isStatusChangeAllowed(String currentStatus, String newStatus, String usertype) {
        if ("administrator".equalsIgnoreCase(usertype)) {
            return true; // Administrator może zmieniać status niezależnie od bieżącego i nowego statusu
        }

        switch (currentStatus) {
            case "Oczekujące":
                return !newStatus.equals("Oczekujące") && (newStatus.equals("W toku") || newStatus.equals("Zakończone") || newStatus.equals("Archiwum"));
            case "W toku":
                return !newStatus.equals("Oczekujące") && !newStatus.equals("W toku") && (newStatus.equals("Zakończone") || newStatus.equals("Archiwum"));
            case "Zakończone":
                return !newStatus.equals("Oczekujące") && !newStatus.equals("W toku") && newStatus.equals("Archiwum");
            case "Archiwum":
                return false; // Nie można zmienić statusu z "Archiwum"
            default:
                return false;
        }
    }




    private String extractProductNameFromDescription(String description) {
        return description.replace("Zlecenie Zamówienia Produktu", "").trim();
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

    public int getProducerIdByName(String producerName) throws SQLException {
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
    private void handleEditProduct(String productName) {
        // Wybór produktu do edycji na podstawie nazwy produktu
        try {
            String query = "SELECT * FROM produkty WHERE Nazwa = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, productName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int productID = resultSet.getInt("IDProduktu");
                        String edtproductName = resultSet.getString("Nazwa");
                        int producerID = resultSet.getInt("IDProducenta");
                        String edtproducer = getProducerNameByID(producerID);
                        double edtquantity = resultSet.getDouble("Ilosc");
                        String edtunit = resultSet.getString("Jednostka");
                        String edtcode = resultSet.getString("Kod");

                        // Wyświetlenie okna dialogowego z formularzem edycji produktu
                        Dialog<String> dialog = new Dialog<>();
                        dialog.setTitle("Edytuj Produkt");
                        dialog.setHeaderText("Edycja Produktu: " + edtproductName);

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
                            double changeValue = Double.parseDouble(changeQuantityTextField.getText());
                            if (!edtunit.equals("Kg.") && changeValue % 1 != 0) {
                                showAlert("Błąd", "Wartość zmiany ilości musi być liczbą całkowitą dla jednostek innych niż Kg.");
                                return;
                            }
                            quantity[0] += changeValue;
                            currentQuantityLabel.setText(String.valueOf(quantity[0]));
                        });

                        decreaseButton.setOnAction(event -> {
                            double changeValue = Double.parseDouble(changeQuantityTextField.getText());
                            if (!edtunit.equals("Kg.") && changeValue % 1 != 0) {
                                showAlert("Błąd", "Wartość zmiany ilości musi być liczbą całkowitą dla jednostek innych niż Kg.");
                                return;
                            }
                            quantity[0] -= changeValue;
                            if (quantity[0] < 0) {
                                quantity[0] = 0;
                            }
                            currentQuantityLabel.setText(String.valueOf(quantity[0]));
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
                                        productUnitComboBox.getValue(),
                                        productCodeTextField.getText()
                                );
                            }
                            return null;
                        });

                        Optional<String> dialogResult = dialog.showAndWait();
                        if (dialogResult.isPresent()) {
                                    String[] resultData = dialogResult.get().split(";");
                                    String newProductName = resultData[0];
                                    String newProducerName = resultData[1];
                                    double newQuantity = Double.parseDouble(resultData[2]);
                                    String newUnit = resultData[3];
                                    String newCode = resultData[4];

                                    // Zaktualizowanie danych w bazie danych
                                    String updateProductQuery = "UPDATE produkty SET Nazwa = ?, IDProducenta = ?, Ilosc = ?, Jednostka = ?, Kod = ? WHERE IDProduktu = ?";
                                    PreparedStatement updateProductStatement = connection.prepareStatement(updateProductQuery);
                                    updateProductStatement.setString(1, newProductName);
                                    updateProductStatement.setInt(2, getProducerIdByName(newProducerName));
                                    updateProductStatement.setDouble(3, newQuantity);
                                    updateProductStatement.setString(4, newUnit);
                                    updateProductStatement.setString(5, newCode);
                                    updateProductStatement.setInt(6, productID);
                                    updateProductStatement.executeUpdate();

                                    logger.info("Zaktualizowano produkt o ID: {}", productID);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    showAlert("Błąd", "Wystąpił błąd podczas edycji produktu: " + e.getMessage());
                    logger.error("Wystąpił błąd podczas edycji produktu: {}", e.getMessage());
                }
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
            Stage stage = (Stage) tasksTableView.getScene().getWindow();
            newMainApp.start(stage);
        }
    }



    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("profile-view.fxml"));
            Parent root1 = loader.load();
            ProfileController profileController = loader.getController();
            profileController.initialize(loggedInUsername);
            Stage stageProfile = new Stage();
            stageProfile.setScene(new Scene(root1));
            stageProfile.setTitle("Moduł Profil");

            // Ustawienie okna profilu na zawsze na wierzchu
            stageProfile.setAlwaysOnTop(true);

            // Blokowanie interakcji z resztą aplikacji
            stageProfile.initModality(Modality.APPLICATION_MODAL);

            stageProfile.showAndWait();
            logger.info("Wyświetlono profil użytkownika '{}'", loggedInUsername);
        } catch (IOException e) {
            showAlert("Błąd ładowania profilu", "Wystąpił błąd podczas ładowania modułu profilu: " + e.getMessage());
            logger.error("Wystąpił błąd podczas ładowania modułu profilu: {}", e.getMessage());
        }
    }


    @FXML
    private void storageButtonAction() {
        mainApp.loadStorageView(userType,loggedInUsername);
    }

    @FXML
    private void handleAdminPanel() {
        mainApp.loadAdminView(userType, loggedInUsername);
    }

    @FXML
    private void handleRaport() {
        // Stworzenie okna dialogowego.
        Dialog<Pair<Integer, String>> dialog = new Dialog<>();
        dialog.setTitle("Wybierz filter i wpisz frazę");
        dialog.setHeaderText("Wybierz filter i wpisz frazę");

        // Ustawienie przycisków.
        ButtonType loginButtonType = new ButtonType("OK", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Stworzenie Comboboxa dla filtru i textfield dla frazy
        ComboBox<String> filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("Brak","Status", "Priorytet", "Kategoria");
        filterComboBox.setValue("Brak");

        TextField phraseTextField = new TextField();
        phraseTextField.setPromptText("Wprowadź Frazę");

        // Stworzenie gridpane i dodanie kontrolek.
        GridPane grid = new GridPane();
        grid.add(filterComboBox, 0, 0);
        grid.add(phraseTextField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Konwertowanie wyników.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                int filterValue = (int) getFilterValue(filterComboBox.getValue());
                String phrase = phraseTextField.getText();
                return new Pair<>(filterValue, phrase);
            }
            return null;
        });

        Platform.runLater(() -> filterComboBox.requestFocus());

        // Pokazanie dialogu i pobranie wyników
        dialog.showAndWait().ifPresent(filterAndPhrase -> {
            int filter = filterAndPhrase.getKey();
            String phrase = filterAndPhrase.getValue();

            PdfGenerator pdfGenerator = new PdfGenerator();
            String desktopPath = System.getProperty("user.home") + "\\Desktop";
            String filePath = desktopPath + "\\task.pdf";
            try {
                pdfGenerator.manipulatePdf(filePath, 1, filter, phrase);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("An error occurred");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }

    private Object getFilterValue(String filter) {
        switch (filter) {
            case "Status":
                return 1;
            case "Priorytet":
                return 2;
            case "Kategoria":
                return 3;
            case "Brak":
                return 4;
            default:
                return null;
        }
    }


}

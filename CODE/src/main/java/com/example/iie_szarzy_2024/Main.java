package com.example.iie_szarzy_2024;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main extends Application {

    private StackPane root; // Główny kontener GUI
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        root = new StackPane(); // Inicjalizacja głównego kontenera GUI
        loadLoginView(); // Ładowanie widoku logowania
        logger.info("Aplikacja zostala uruchomiona");
        primaryStage.setTitle("Szary Sklep Spożywczy");
        primaryStage.setScene(new Scene(root, 1900, 1000)); // Ustawienie sceny
        primaryStage.show(); // Wyświetlenie głównego okna
    }

    // Metoda do ładowania widoku logowania
    public void loadLoginView() {
        try {
            logger.info("Ladowanie widoku logowania");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent loginView = loader.load();
            LoginController loginController = loader.getController();
            loginController.setMainApp(this); // Ustawienie referencji do głównej klasy Main
            root.getChildren().clear(); // Wyczyszczenie kontenera
            root.getChildren().add(loginView); // Dodanie widoku logowania do kontenera
            logger.info("Widok logowania załadowany pomyślnie");
        } catch (IOException e) {
            logger.error("Wystąpił błąd podczas ładowania widoku logowania", e);
        }
    }


    // Metoda do ładowania widoku zadań
    public void loadTasksView(String userType, String loggedInUsername) {
        try {
            logger.info("Ładowanie widoku zadań dla użytkownika: {}, typ: {}", loggedInUsername, userType);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tasks-view.fxml"));
            Parent tasksView = loader.load();
            TasksController tasksController = loader.getController();

            tasksController.setMainApp(this); // Ustawienie mainapp w kontrolerze
            tasksController.setLoggedInUserType(userType);
            tasksController.setLoggedInUsername(loggedInUsername); // Przekazanie nazwy zalogowanego użytkownika
            root.getChildren().clear();
            root.getChildren().add(tasksView);
            logger.info("Widok zadań załadowany pomyślnie");
        } catch (IOException e) {
            logger.error("Wystąpił błąd podczas ładowania widoku zadań", e);
        }
    }

    // Metoda do ładowania widoku rejestracji
    public void loadRegisterView() {
        try {
            logger.info("Ładowanie widoku rejestracji");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register-view.fxml"));
            Parent registerView = loader.load();
            RegisterController registerController = loader.getController();
            registerController.setMainApp(this); // Ustawienie mainapp w kontrolerze
            root.getChildren().clear(); // Wyczyszczenie kontenera
            root.getChildren().add(registerView); // Dodanie widoku rejestracji do kontenera
            logger.info("Widok rejestracji załadowany pomyślnie");
        } catch (IOException e) {
            logger.error("Wystąpił błąd podczas ładowania widoku rejestracji", e);
        }
    }

    // Metoda do ładowania widoku przypomnienia hasła
    public void loadRemindView() {
        try {
            logger.info("Ładowanie widoku przypomnienia hasła");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("remind-view.fxml"));
            Parent remindView = loader.load();
            RemindController remindController = loader.getController();
            remindController.setMainApp(this); // Ustawienie mainapp w kontrolerze
            root.getChildren().clear(); // Wyczyszczenie kontenera
            root.getChildren().add(remindView); // Dodanie widoku przypomnienia hasła do kontenera
            logger.info("Widok przypomnienia hasła załadowany pomyślnie");
        } catch (IOException e) {
            logger.error("Wystąpił błąd podczas ładowania widoku przypomnienia hasła", e);
        }
    }

    // Metoda do ładowania widoku magazynu
    public void loadStorageView(String userType, String loggedInUsername) {
        try {
            logger.info("Ładowanie widoku magazynu");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("storage-view.fxml"));
            Parent storageView = loader.load();
            StorageController storageController = loader.getController();
            storageController.setMainApp(this); // Ustawienie mainapp w kontrolerze
            storageController.setLoggedInUserType(userType);
            storageController.setLoggedInUsername(loggedInUsername); // Przekazanie nazwy zalogowanego użytkownika
            root.getChildren().clear();
            root.getChildren().add(storageView);
            logger.info("Widok magazynu załadowany pomyślnie");
        } catch (IOException e) {
            logger.error("Wystąpił błąd podczas ładowania widoku magazynu", e);
        }
    }

    public void loadAdminView(String userType, String loggedInUsername) {
        try {
            logger.info("Ładowanie widoku Panelu Administratora");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-view.fxml"));
            Parent adminView = loader.load();
            AdminController adminController = loader.getController();
            adminController.setMainApp(this); // Ustawienie mainapp w kontrolerze
            adminController.setLoggedInUserType(userType);
            adminController.setLoggedInUsername(loggedInUsername); // Przekazanie nazwy zalogowanego użytkownika
            root.getChildren().clear();
            root.getChildren().add(adminView);
            logger.info("Widok Panelu Administratora załadowany pomyślnie");
        } catch (IOException e) {
            logger.error("Wystąpił błąd podczas ładowania widoku Panelu Administratora", e);
        }
    }
    public static void main(String[] args) {
        launch(args); // Uruchomienie aplikacji
    }

}

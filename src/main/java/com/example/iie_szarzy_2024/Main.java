package com.example.iie_szarzy_2024;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    private StackPane root; // Główny kontener GUI

    @Override
    public void start(Stage primaryStage) {
        root = new StackPane(); // Inicjalizacja głównego kontenera GUI
        loadLoginView(); // Ładowanie widoku logowania
        primaryStage.setTitle("Szary Sklep Spożywczy");
        primaryStage.setScene(new Scene(root, 1900, 1000)); // Ustawienie sceny
        primaryStage.show(); // Wyświetlenie głównego okna
    }

    // Metoda do ładowania widoku logowania
    public void loadLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent loginView = loader.load();
            LoginController loginController = loader.getController();
            loginController.setMainApp(this); // Ustawienie referencji do głównej klasy Main
            root.getChildren().clear(); // Wyczyszczenie kontenera
            root.getChildren().add(loginView); // Dodanie widoku logowania do kontenera
        } catch (IOException e) {
            System.out.println("Wystąpił błąd podczas ładowania widoku logowania");
        }
    }

    // Metoda do ładowania widoku zadań
    public void loadTasksView(String userType, String loggedInUsername) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tasks-view.fxml"));
            Parent tasksView = loader.load();
            TasksController tasksController = loader.getController();
            tasksController.setMainApp();
            tasksController.setLoggedInUserType(userType);
            tasksController.setLoggedInUsername(loggedInUsername); // Przekazanie nazwy zalogowanego użytkownika
            root.getChildren().clear();
            root.getChildren().add(tasksView);
        } catch (IOException e) {
            System.out.println("Wystąpił błąd podczas ładowania widoku zadań");
        }
    }

    // Metoda do ładowania widoku rejestracji
    public void loadRegisterView() {
        try {
            Parent registerView = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("register-view.fxml")));
            root.getChildren().clear(); // Wyczyszczenie kontenera
            root.getChildren().add(registerView); // Dodanie widoku rejestracji do kontenera
        } catch (IOException e) {
            System.out.println("Wystąpił błąd podczas ładowania widoku rejestracji");
        }
    }

    // Metoda do ładowania widoku przypomnienia hasła
    public void loadRemindView() {
        try {
            Parent remindView = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("remind-view.fxml")));
            root.getChildren().clear(); // Wyczyszczenie kontenera
            root.getChildren().add(remindView); // Dodanie widoku przypomnienia hasła do kontenera
        } catch (IOException e) {
            System.out.println("Wystąpił błąd podczas ładowania widoku przypomnienia hasła");
        }
    }

    public static void main(String[] args) {
        launch(args); // Uruchomienie aplikacji
    }

}


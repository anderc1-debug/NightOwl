package com.nightowl;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        showSplashScreen(primaryStage);
    }

    private void showSplashScreen(Stage primaryStage) {
        VBox splashContent = new VBox(10);
        splashContent.setAlignment(Pos.CENTER);

        Label appName = new Label("NightOwl");
        appName.setStyle("-fx-font-size: 42px; -fx-text-fill: #F4A227; -fx-font-weight: bold;");

        Label tagline = new Label("Your campus. Anytime.");
        tagline.setStyle("-fx-font-size: 16px; -fx-text-fill: #8BAFC4;");

        splashContent.getChildren().addAll(appName, tagline);

        StackPane splashRoot = new StackPane(splashContent);
        splashRoot.setStyle("-fx-background-color: #0D1B2A;");

        Scene splashScene = new Scene(splashRoot, 600, 400);
        splashScene.setFill(Color.web("#0D1B2A"));

        primaryStage.setScene(splashScene);
        primaryStage.setTitle("NightOwl");
        primaryStage.show();

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1200), splashRoot);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeIn.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.millis(1500));
            pause.setOnFinished(p -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(800), splashRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(f -> {
                    try {
                        loadMainWindow(primaryStage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                fadeOut.play();
            });
            pause.play();
        });

        fadeIn.play();
    }

    private void loadMainWindow(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/nightowl/main.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
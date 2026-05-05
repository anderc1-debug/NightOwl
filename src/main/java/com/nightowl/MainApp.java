package com.nightowl;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    private Stage primaryStage;
    private UserProfile currentUser;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showSplashScreen();
    }

    // ── Splash ────────────────────────────────────────────────────────────────

    private void showSplashScreen() {
        VBox splashContent = new VBox(16);
        splashContent.setAlignment(Pos.CENTER);

        try {
            Image owlImage = new Image(getClass().getResourceAsStream("/com/nightowl/owl.png"));
            ImageView owlView = new ImageView(owlImage);
            owlView.setFitWidth(180);
            owlView.setFitHeight(180);
            owlView.setPreserveRatio(true);
            splashContent.getChildren().add(owlView);
        } catch (Exception e) {
            Label owlFallback = new Label("🦉");
            owlFallback.setStyle("-fx-font-size: 80px;");
            splashContent.getChildren().add(owlFallback);
        }

        Label appName = new Label("NightOwl");
        appName.setStyle("-fx-font-size: 42px; -fx-text-fill: #C084FC; -fx-font-weight: bold;");

        Label tagline = new Label("Your campus. Anytime.");
        tagline.setStyle("-fx-font-size: 16px; -fx-text-fill: #A78BCA;");

        splashContent.getChildren().addAll(appName, tagline);

        StackPane splashRoot = new StackPane(splashContent);
        splashRoot.setStyle("-fx-background-color: #1A0A2E;");

        Scene splashScene = new Scene(splashRoot, 600, 450);
        splashScene.setFill(Color.web("#1A0A2E"));

        primaryStage.setScene(splashScene);
        primaryStage.setTitle("NightOwl");
        primaryStage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1200), splashRoot);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.millis(1400));
            pause.setOnFinished(p -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(700), splashRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(f -> showLoginScreen());
                fadeOut.play();
            });
            pause.play();
        });
        fadeIn.play();
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    private void showLoginScreen() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #1A0A2E;");
        root.setAlignment(Pos.CENTER);

        // Card
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);
        card.setStyle("""
            -fx-background-color: #120720;
            -fx-border-color: #3D1F6B;
            -fx-border-width: 1;
            -fx-border-radius: 14;
            -fx-background-radius: 14;
            """);
        card.setPadding(new Insets(36, 40, 36, 40));

        Label owlLabel = new Label("🦉");
        owlLabel.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Welcome to NightOwl");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #C084FC;");

        Label subtitle = new Label("Sign in or create an account");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #A78BCA;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #3D1F6B;");

        // Fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        styleTextField(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleTextField(passwordField);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #E05555; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        // Buttons
        Button loginBtn = new Button("Sign In");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        stylePrimaryButton(loginBtn);

        Button registerBtn = new Button("Create Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        styleSecondaryButton(registerBtn);

        loginBtn.setOnAction(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Please enter your username and password.");
                return;
            }
            UserProfile profile = DatabaseManager.getInstance().login(user, pass);
            if (profile == null) {
                errorLabel.setText("Invalid username or password.");
            } else {
                currentUser = profile;
                if (DatabaseManager.getInstance().isProfileComplete(profile)) {
                    loadMainWindow();
                } else {
                    showOnboarding();
                }
            }
        });

        registerBtn.setOnAction(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Please enter a username and password.");
                return;
            }
            if (pass.length() < 4) {
                errorLabel.setText("Password must be at least 4 characters.");
                return;
            }
            if (DatabaseManager.getInstance().usernameExists(user)) {
                errorLabel.setText("That username is already taken.");
                return;
            }
            currentUser = DatabaseManager.getInstance().createUser(user, pass);
            if (currentUser != null) {
                showOnboarding();
            } else {
                errorLabel.setText("Could not create account. Try again.");
            }
        });

        card.getChildren().addAll(owlLabel, title, subtitle, sep, usernameField, passwordField, errorLabel, loginBtn, registerBtn);

        StackPane wrapper = new StackPane(card);
        wrapper.setStyle("-fx-background-color: #1A0A2E;");

        Scene scene = new Scene(wrapper, 600, 500);
        scene.setFill(Color.web("#1A0A2E"));
        primaryStage.setScene(scene);
    }

    // ── Onboarding ────────────────────────────────────────────────────────────

    private void showOnboarding() {
        // Step tracker
        int[] step = {1};
        String[] school = {""};
        String[] major = {""};
        String[] classYear = {""};
        String[] prefs = {""};

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1A0A2E;");

        showOnboardingStep(root, step, school, major, classYear, prefs);

        Scene scene = new Scene(root, 640, 520);
        scene.setFill(Color.web("#1A0A2E"));
        primaryStage.setScene(scene);
    }

    private void showOnboardingStep(StackPane root, int[] step,
                                     String[] school, String[] major,
                                     String[] classYear, String[] prefs) {
        root.getChildren().clear();

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(480);
        card.setStyle("""
            -fx-background-color: #120720;
            -fx-border-color: #3D1F6B;
            -fx-border-width: 1;
            -fx-border-radius: 14;
            -fx-background-radius: 14;
            """);
        card.setPadding(new Insets(36, 44, 36, 44));

        // Step indicator
        Label stepLabel = new Label("Step " + step[0] + " of 3");
        stepLabel.setStyle("-fx-text-fill: #5B3A8A; -fx-font-size: 12px;");

        // Progress dots
        HBox dots = new HBox(8);
        dots.setAlignment(Pos.CENTER);
        for (int i = 1; i <= 3; i++) {
            Label dot = new Label("●");
            dot.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (i <= step[0] ? "#C084FC" : "#3D1F6B") + ";");
            dots.getChildren().add(dot);
        }

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #E05555; -fx-font-size: 12px;");

        VBox stepContent = new VBox(14);
        stepContent.setAlignment(Pos.CENTER_LEFT);

        Button nextBtn = new Button(step[0] == 3 ? "Get Started →" : "Next →");
        nextBtn.setMaxWidth(Double.MAX_VALUE);
        stylePrimaryButton(nextBtn);

        if (step[0] == 1) {
            Label title = new Label("What's your school?");
            title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #C084FC;");
            Label sub = new Label("We'll personalize resources for your campus.");
            sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #A78BCA;");

            ComboBox<String> schoolBox = new ComboBox<>();
            schoolBox.getItems().addAll(
                "Farmingdale State College",
                "Stony Brook University",
                "SUNY Old Westbury",
                "Nassau Community College",
                "Hofstra University",
                "Adelphi University",
                "Other"
            );
            schoolBox.setPromptText("Select your school");
            schoolBox.setMaxWidth(Double.MAX_VALUE);
            styleComboBox(schoolBox);
            if (!school[0].isEmpty()) schoolBox.setValue(school[0]);

            nextBtn.setOnAction(e -> {
                if (schoolBox.getValue() == null) {
                    errorLabel.setText("Please select your school.");
                    return;
                }
                school[0] = schoolBox.getValue();
                step[0]++;
                showOnboardingStep(root, step, school, major, classYear, prefs);
            });

            stepContent.getChildren().addAll(title, sub, schoolBox);

        } else if (step[0] == 2) {
            Label title = new Label("Tell us about yourself");
            title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #C084FC;");
            Label sub = new Label("Your major and year help us show what's relevant.");
            sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #A78BCA;");

            TextField majorField = new TextField(major[0]);
            majorField.setPromptText("Your major (e.g. Computer Science)");
            styleTextField(majorField);

            ComboBox<String> yearBox = new ComboBox<>();
            yearBox.getItems().addAll("Freshman", "Sophomore", "Junior", "Senior", "Graduate");
            yearBox.setPromptText("Class year");
            yearBox.setMaxWidth(Double.MAX_VALUE);
            styleComboBox(yearBox);
            if (!classYear[0].isEmpty()) yearBox.setValue(classYear[0]);

            nextBtn.setOnAction(e -> {
                if (majorField.getText().trim().isEmpty()) {
                    errorLabel.setText("Please enter your major.");
                    return;
                }
                if (yearBox.getValue() == null) {
                    errorLabel.setText("Please select your class year.");
                    return;
                }
                major[0] = majorField.getText().trim();
                classYear[0] = yearBox.getValue();
                step[0]++;
                showOnboardingStep(root, step, school, major, classYear, prefs);
            });

            stepContent.getChildren().addAll(title, sub, majorField, yearBox);

        } else {
            Label title = new Label("What do you need most?");
            title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #C084FC;");
            Label sub = new Label("Pick all that apply. We'll highlight these for you.");
            sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #A78BCA;");

            String[] options = {"Academic Resources", "Mental Health", "Emergency Info", "Campus Map & Shuttle", "Study Spaces", "Wellness Tracking"};
            VBox checkboxes = new VBox(10);
            CheckBox[] boxes = new CheckBox[options.length];
            for (int i = 0; i < options.length; i++) {
                boxes[i] = new CheckBox(options[i]);
                boxes[i].setStyle("-fx-text-fill: #E0C3FC; -fx-font-size: 13px;");
                checkboxes.getChildren().add(boxes[i]);
            }

            nextBtn.setOnAction(e -> {
                StringBuilder sb = new StringBuilder();
                for (CheckBox cb : boxes) {
                    if (cb.isSelected()) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(cb.getText());
                    }
                }
                prefs[0] = sb.toString();
                DatabaseManager.getInstance().updateUserProfile(
                    currentUser.getId(), school[0], major[0], classYear[0], prefs[0]
                );
                currentUser = new UserProfile(currentUser.getId(), currentUser.getUsername(),
                    school[0], major[0], classYear[0], prefs[0]);
                loadMainWindow();
            });

            stepContent.getChildren().addAll(title, sub, checkboxes);
        }

        card.getChildren().addAll(stepLabel, dots, stepContent, errorLabel, nextBtn);
        StackPane.setAlignment(card, Pos.CENTER);
        root.getChildren().add(card);
    }

    // ── Main Window ───────────────────────────────────────────────────────────

    private void loadMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/nightowl/main.fxml"));
            Parent fxmlRoot = loader.load();

            MainController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Scene scene = new Scene(fxmlRoot, 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Style Helpers ─────────────────────────────────────────────────────────

    private void styleTextField(TextField field) {
        field.setStyle("""
            -fx-background-color: #1A0A2E;
            -fx-text-fill: #E0C3FC;
            -fx-prompt-text-fill: #5B3A8A;
            -fx-border-color: #3D1F6B;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 10 14 10 14;
            -fx-font-size: 13px;
            """);
    }

    private void stylePrimaryButton(Button btn) {
        btn.setStyle("""
            -fx-background-color: #7C3AED;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-background-radius: 8;
            -fx-padding: 11 20 11 20;
            -fx-cursor: hand;
            """);
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("#7C3AED", "#6D28D9")));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("#6D28D9", "#7C3AED")));
    }

    private void styleSecondaryButton(Button btn) {
        btn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #A78BCA;
            -fx-font-size: 13px;
            -fx-border-color: #3D1F6B;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 10 20 10 20;
            -fx-cursor: hand;
            """);
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("#3D1F6B", "#5B3A8A")));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("#5B3A8A", "#3D1F6B")));
    }

    private void styleComboBox(ComboBox<?> box) {
        box.setStyle("""
            -fx-background-color: #1A0A2E;
            -fx-border-color: #3D1F6B;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-font-size: 13px;
            """);
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

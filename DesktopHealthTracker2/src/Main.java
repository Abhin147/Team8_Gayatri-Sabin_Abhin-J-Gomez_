import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

public class Main extends Application {

    int waterCount = 0, stretchCount = 0, eyeRestCount = 0;
    int waterInterval, stretchInterval, eyeRestInterval;

    Label waterLabel, stretchLabel, eyeRestLabel, heading;
    Button pauseButton;
    boolean paused = false;
    boolean darkMode = false;

    Timer timer;
    Preferences prefs;

    Scene mainScene;
    BorderPane root;
    VBox contentBox;

    @Override
    public void start(Stage primaryStage) {
        prefs = Preferences.userNodeForPackage(Main.class);

        waterInterval = prefs.getInt("waterInterval", 20);
        stretchInterval = prefs.getInt("stretchInterval", 40);
        eyeRestInterval = prefs.getInt("eyeRestInterval", 60);

        // Labels
        waterLabel = new Label("Water reminders completed: 0");
        stretchLabel = new Label("Stretch reminders completed: 0");
        eyeRestLabel = new Label("Eye rest reminders completed: 0");

        // Heading (now class-level)
        heading = new Label("💻 Desktop Health Tracker");
        heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        HBox headingBox = new HBox(heading);
        headingBox.setAlignment(Pos.CENTER);
        headingBox.setPadding(new Insets(0, 0, 10, 0));

        // Buttons
        pauseButton = new Button("Pause ⏸");
        pauseButton.setOnAction(e -> togglePause());

        Button settingsButton = new Button("⚙ Settings");
        settingsButton.setOnAction(e -> openSettingsWindow());

        Button historyButton = new Button("📊 History");
        historyButton.setOnAction(e -> openHistoryWindow());

        Button resetButton = new Button("🔁 Reset Counts");
        resetButton.setOnAction(e -> resetCounts());

        Button darkToggle = new Button("🌙");
        darkToggle.setOnAction(e -> toggleTheme());
        darkToggle.setStyle(
                "-fx-background-color: #dddddd; -fx-font-size: 16px; -fx-padding: 5 10 5 10; " +
                        "-fx-background-radius: 20; -fx-cursor: hand;"
        );
        HBox darkBox = new HBox(darkToggle);
        darkBox.setAlignment(Pos.TOP_RIGHT);
        darkBox.setPadding(new Insets(10));

        contentBox = new VBox(15,
                waterLabel,
                stretchLabel,
                eyeRestLabel,
                new HBox(10, pauseButton, settingsButton, historyButton),
                new HBox(10, resetButton)
        );
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(400);

        VBox topSection = new VBox(darkBox, headingBox);
        root = new BorderPane();
        root.setTop(topSection);
        root.setCenter(contentBox);
        root.setStyle("-fx-padding: 20;");

        mainScene = new Scene(root, 450, 300);
        applyLightTheme();

        FadeTransition fade = new FadeTransition(Duration.millis(800), root);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Health Tracker");
        primaryStage.show();

        startReminders();
    }

    private void togglePause() {
        paused = !paused;
        pauseButton.setText(paused ? "Resume ▶" : "Pause ⏸");
    }

    private void resetCounts() {
        waterCount = 0;
        stretchCount = 0;
        eyeRestCount = 0;

        updateLabel(waterLabel, "Water reminders completed: 0");
        updateLabel(stretchLabel, "Stretch reminders completed: 0");
        updateLabel(eyeRestLabel, "Eye rest reminders completed: 0");
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        if (darkMode) applyDarkTheme();
        else applyLightTheme();
    }

    private void applyDarkTheme() {
        root.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 20;");
        contentBox.getChildren().forEach(node -> {
            if (node instanceof Label label) {
                label.setStyle("-fx-text-fill: white;");
            }
        });
        heading.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
    }

    private void applyLightTheme() {
        root.setStyle("-fx-background-color: white; -fx-padding: 20;");
        contentBox.getChildren().forEach(node -> {
            if (node instanceof Label label) {
                label.setStyle("-fx-text-fill: black;");
            }
        });
        heading.setStyle("-fx-text-fill: black; -fx-font-size: 18px; -fx-font-weight: bold;");
    }

    private void startReminders() {
        timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                if (!paused) {
                    showReminder("Drink Water", "💧 Time to drink water!", () -> {
                        waterCount++;
                        updateLabel(waterLabel, "Water reminders completed: " + waterCount);
                    });
                }
            }
        }, 0, waterInterval * 1000L);

        timer.schedule(new TimerTask() {
            public void run() {
                if (!paused) {
                    showReminder("Stretch", "\uD83E\uDD38 Time to stretch!", () -> {
                        stretchCount++;
                        updateLabel(stretchLabel, "Stretch reminders completed: " + stretchCount);
                    });
                }
            }
        }, 10000, stretchInterval * 1000L);

        timer.schedule(new TimerTask() {
            public void run() {
                if (!paused) {
                    showReminder("Rest Eyes", "👀 Rest your eyes!", () -> {
                        eyeRestCount++;
                        updateLabel(eyeRestLabel, "Eye rest reminders completed: " + eyeRestCount);
                    });
                }
            }
        }, 20000, eyeRestInterval * 1000L);
    }

    private void updateLabel(Label label, String text) {
        Platform.runLater(() -> {
            label.setText(text);
            animateLabel(label);
        });
    }

    private void animateLabel(Label label) {
        ScaleTransition st = new ScaleTransition(Duration.millis(250), label);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.15);
        st.setToY(1.15);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }

    private void showReminder(String title, String message, Runnable onOk) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            onOk.run();
        });
    }

    private void openSettingsWindow() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.setTitle("Settings");

        TextField waterField = new TextField(String.valueOf(waterInterval));
        TextField stretchField = new TextField(String.valueOf(stretchInterval));
        TextField eyeRestField = new TextField(String.valueOf(eyeRestInterval));

        Button saveButton = new Button("Save & Close");
        saveButton.setOnAction(e -> {
            try {
                waterInterval = Integer.parseInt(waterField.getText());
                stretchInterval = Integer.parseInt(stretchField.getText());
                eyeRestInterval = Integer.parseInt(eyeRestField.getText());

                prefs.putInt("waterInterval", waterInterval);
                prefs.putInt("stretchInterval", stretchInterval);
                prefs.putInt("eyeRestInterval", eyeRestInterval);

                timer.cancel();
                startReminders();

                settingsStage.close();
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter valid numbers.");
                alert.showAndWait();
            }
        });

        VBox settingsBox = new VBox(10,
                new Label("Set Intervals (in seconds):"),
                new HBox(10, new Label("Water:"), waterField),
                new HBox(10, new Label("Stretch:"), stretchField),
                new HBox(10, new Label("Eye Rest:"), eyeRestField),
                saveButton
        );
        settingsBox.setStyle("-fx-padding: 20; -fx-font-size: 13;");

        Scene settingsScene = new Scene(settingsBox, 300, 220);
        settingsStage.setScene(settingsScene);
        settingsStage.show();
    }

    private void openHistoryWindow() {
        Stage dataStage = new Stage();
        dataStage.initModality(Modality.APPLICATION_MODAL);
        dataStage.setTitle("📊 History");

        Label title = new Label("📊 Your Previous Data");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label water = new Label("Water reminders completed: " + prefs.getInt("waterCount", 0));
        Label stretch = new Label("Stretch reminders completed: " + prefs.getInt("stretchCount", 0));
        Label eyeRest = new Label("Eye rest reminders completed: " + prefs.getInt("eyeRestCount", 0));

        Label intervals = new Label("\nSaved Intervals:");
        Label i1 = new Label("Water: " + prefs.getInt("waterInterval", 20) + " sec");
        Label i2 = new Label("Stretch: " + prefs.getInt("stretchInterval", 40) + " sec");
        Label i3 = new Label("Eye Rest: " + prefs.getInt("eyeRestInterval", 60) + " sec");

        VBox layout = new VBox(10, title, water, stretch, eyeRest, intervals, i1, i2, i3);
        layout.setStyle("-fx-padding: 20; -fx-font-size: 13;");

        Scene scene = new Scene(layout, 300, 250);
        dataStage.setScene(scene);
        dataStage.show();
    }

    @Override
    public void stop() {
        if (timer != null) timer.cancel();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

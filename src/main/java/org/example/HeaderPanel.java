package org.example.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HeaderPanel extends VBox {
    private static final String APP_TITLE = "Dealership Management System";
    private static final Color THEME_COLOR = Color.DODGERBLUE;
    private boolean darkModeEnabled = false;

    public HeaderPanel() {
        setPadding(new Insets(15));
        String colorHex = String.format("#%02X%02X%02X",
                (int) (THEME_COLOR.getRed() * 255),
                (int) (THEME_COLOR.getGreen() * 255),
                (int) (THEME_COLOR.getBlue() * 255));
        setStyle("-fx-background-color: " + colorHex + ";");
        Label titleLabel = new Label(APP_TITLE);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        getChildren().add(titleLabel);
    }

    public void setupDarkModeToggle(BorderPane mainLayout, Stage primaryStage) {
        // Create dark mode toggle button
        ToggleButton darkModeToggle = new ToggleButton("Dark Mode");
        darkModeToggle.setStyle("-fx-font-size: 12px;");

        // Place it in the top-right corner
        HBox topRightBox = new HBox(darkModeToggle);
        topRightBox.setAlignment(Pos.TOP_RIGHT);
        topRightBox.setPadding(new Insets(5, 10, 0, 0));

        // Create a new container with both header and toggle
        BorderPane headerContainer = new BorderPane();
        headerContainer.setCenter(this);
        headerContainer.setRight(topRightBox);

        // Set the new combined header
        mainLayout.setTop(headerContainer);

        // Add toggle functionality
        darkModeToggle.setOnAction(e -> {
            darkModeEnabled = darkModeToggle.isSelected();
            applyTheme(mainLayout, primaryStage);
        });
    }

    private void applyTheme(BorderPane mainLayout, Stage primaryStage) {
        Scene scene = primaryStage.getScene();

        if (darkModeEnabled) {
            // First remove any existing dark mode stylesheet to avoid duplication
            scene.getStylesheets().remove(getClass().getResource("/DarkMode.css").toExternalForm());

            // Remove light stylesheet
            scene.getStylesheets().remove(getClass().getResource("/LightMode.css").toExternalForm());

            // Apply dark stylesheet to the entire scene
            scene.getStylesheets().add(getClass().getResource("/DarkMode.css").toExternalForm());

            // Update header color
            setStyle("-fx-background-color: #006666;"); // Darker cyan
        } else {
            // Remove dark stylesheet
            scene.getStylesheets().remove(getClass().getResource("/DarkMode.css").toExternalForm());

            // Apply light stylesheet (if not already applied)
            if (!scene.getStylesheets().contains(getClass().getResource("/LightMode.css").toExternalForm())) {
                scene.getStylesheets().add(getClass().getResource("/LightMode.css").toExternalForm());
            }

            // Reset header color
            String colorHex = String.format("#%02X%02X%02X",
                    (int) (THEME_COLOR.getRed() * 255),
                    (int) (THEME_COLOR.getGreen() * 255),
                    (int) (THEME_COLOR.getBlue() * 255));
            setStyle("-fx-background-color: " + colorHex + ";");
        }
    }
}

package org.example.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.example.controller.DealershipController;
import org.example.DashboardView;
import kotlin.Unit; // <-- Needed for Kotlin interop

public class SearchPanel extends HBox {
    private DealershipController controller;
    private VehicleDisplayPanel displayPanel;

    private TextField searchField;
    private ComboBox<String> searchTypeComboBox;

    public SearchPanel(DealershipController controller) {
        this.controller = controller;

        setSpacing(10);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER_LEFT);

        createComponents();
    }

    public void setDisplayPanel(VehicleDisplayPanel displayPanel) {
        this.displayPanel = displayPanel;
    }

    private void createComponents() {
        // Create search label
        Label searchLabel = new Label("Search:");
        searchLabel.setStyle("-fx-font-weight: bold;");

        // Create search type combo box
        searchTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "ID", "Manufacturer", "Model", "Dealer ID", "Type", "All Fields"));
        searchTypeComboBox.setValue("All Fields");
        searchTypeComboBox.setPrefWidth(150);

        // Create search text field - narrower width
        searchField = new TextField();
        searchField.setPromptText("Enter search query");
        searchField.setPrefWidth(260);

        // Create search button
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        searchButton.setPrefWidth(100);
        searchButton.setPrefHeight(35);

        // Create dashboard button
        Button dashboardButton = new Button("Dashboard");
        dashboardButton.setStyle(
                "-fx-font-weight: bold; " +
                        "-fx-background-color: #FFA500; " + // Bright orange
                        "-fx-font-size: 14px;"
        );
        dashboardButton.setPrefWidth(100);
        dashboardButton.setPrefHeight(35);

        // Add listeners for search
        searchField.setOnAction(e -> performSearch());
        searchButton.setOnAction(e -> performSearch());

        // Clear search results when field is empty
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                if (displayPanel != null) {
                    displayPanel.refreshDisplay();
                }
            }
        });

        // Dashboard button action
        dashboardButton.setOnAction(e -> showDashboard());

        // Create a spacer to achieve 0.6:0.4 spacing
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Add components to search box
        getChildren().addAll(
                searchLabel,
                searchTypeComboBox,
                searchField,
                searchButton,
                spacer,
                dashboardButton
        );
    }

    private void performSearch() {
        if (displayPanel == null) return;

        String query = searchField.getText();
        String searchType = searchTypeComboBox.getValue();

        displayPanel.search(query, searchType);
    }

    private void showDashboard() {
        // Create and show the Kotlin dashboard
        if (displayPanel != null) {
            DashboardView dashboard = new DashboardView(
                    controller.getDealershipManager(),
                    () -> {
                        displayPanel.refreshDisplay();
                        return Unit.INSTANCE; // <-- Fix: return Unit for Kotlin lambda
                    }
            );
            dashboard.showAndWait();
        } else {
            // Fallback: pass a no-op lambda to match constructor
            DashboardView dashboard = new DashboardView(
                    controller.getDealershipManager(),
                    () -> Unit.INSTANCE
            );
            dashboard.showAndWait();
        }
    }
}

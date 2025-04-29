package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.controller.DealershipController;
import org.example.view.*;

import java.io.File;

public class MainView extends Application {
    // Core business logic controller
    private DealershipController controller;

    // UI Components
    private HeaderPanel headerPanel;
    private SearchPanel searchPanel;
    private InputPanel inputPanel;
    private ButtonPanel buttonPanel;
    private VehicleDisplayPanel displayPanel;

    // Main layout components
    private BorderPane mainLayout;
    private SplitPane mainSplitPane;
    private ScrollPane displayScrollPane; // New scroll pane for display

    @Override
    public void start(Stage primaryStage) {
        // Initialize the controller
        controller = new DealershipController();

        // Create the main layout
        mainLayout = new BorderPane();

        // Initialize UI components
        headerPanel = new HeaderPanel();
        displayPanel = new VehicleDisplayPanel(controller);
        searchPanel = new SearchPanel(controller);
        inputPanel = new InputPanel(controller);
        buttonPanel = new ButtonPanel(controller);

        // Create scrollable display area
        displayScrollPane = new ScrollPane(displayPanel);
        displayScrollPane.setFitToWidth(true);
        displayScrollPane.setPrefHeight(300);
        displayScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // Set up cross-component references
        searchPanel.setDisplayPanel(displayPanel);
        inputPanel.setDisplayPanel(displayPanel);
        inputPanel.setMainView(this);
        buttonPanel.setDisplayPanel(displayPanel);
        buttonPanel.setInputPanel(inputPanel);

        // Set up the layout
        setupLayout();

        // Create the scene
        Scene scene = new Scene(mainLayout, 1000, 700);

        // Configure the stage
        primaryStage.setTitle("Dealership Management System");
        primaryStage.setScene(scene);

        // Add dark mode toggle
        headerPanel.setupDarkModeToggle(mainLayout, primaryStage);

        // Register the display panel as a listener for inventory changes
        // This ensures it refreshes when inventory is cleared from the dashboard
        controller.addInventoryChangeListener(() -> {
            displayPanel.refreshDisplay();
        });

        // Show the stage
        primaryStage.show();

        // Load initial data
        loadInitialInventory();
    }

    private void setupLayout() {
        // Set header at the top
        mainLayout.setTop(headerPanel);

        // Create center section with input fields and buttons
        SplitPane centerPane = new SplitPane();
        centerPane.getItems().addAll(inputPanel, buttonPanel);
        centerPane.setDividerPositions(0.6); // Using 0.6 as preferred (60%-40% split)

        // Put search and center pane in a VBox
        VBox centerContent = new VBox(searchPanel, centerPane);
        VBox.setVgrow(centerPane, Priority.ALWAYS);

        // Create main split pane that can be collapsed
        mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        mainSplitPane.getItems().add(centerContent);
        // We don't add the inventory panel initially - it will be added when shown

        // Add the main content to the center of the main layout
        mainLayout.setCenter(mainSplitPane);
    }

    /**
     * Toggles the visibility of the inventory panel
     */
    public void toggleInventoryVisibility(boolean show) {
        if (show) {
            // Only add if not already present
            if (!mainSplitPane.getItems().contains(displayScrollPane)) {
                mainSplitPane.getItems().add(displayScrollPane);
                mainSplitPane.setDividerPositions(0.7);
                // Refresh the display when showing
                displayPanel.refreshDisplay();
            }
        } else {
            // Remove the inventory panel
            if (mainSplitPane.getItems().size() > 1) {
                mainSplitPane.getItems().remove(displayScrollPane);
            }
        }
    }

    private void loadInitialInventory() {
        // Load initial inventory from file
        File inventoryFile = new File("src/main/resources/inventory.json");
        if (inventoryFile.exists()) {
            controller.loadInitialInventory();
            displayPanel.refreshDisplay();
        }
    }

    // Static method to launch the application
    public static void main(String[] args) {
        launch(args);
    }
}

package org.example.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import org.example.*;
import org.example.controller.DealershipController;

import java.io.File;
import java.util.List;

/**
 * Input panel contains dealer selection and acquisition controls
 * Now also includes import/export functionality buttons
 */
public class InputPanel extends GridPane {

    private DealershipController controller;
    private VehicleDisplayPanel displayPanel;
    private MainView mainView;

    private TextField dealerIdField;
    private ComboBox<String> dealerIdComboBox;

    private Button enableAcquisitionBtn;
    private Button disableAcquisitionBtn;
    private Separator separator;
    private Button importXmlBtn;
    private Button importJsonBtn;
    private Button exportInventoryBtn;
    private Button clearExportBtn;
    private Button showInventoryBtn;
    private boolean inventoryVisible = false;

    public InputPanel(DealershipController controller) {
        this.controller = controller;
        setPadding(new Insets(20));
        setVgap(10);
        setHgap(10);
        createComponents();
        controller.updateDealerList();
        refreshDealerComboBox(); // Ensure ComboBox is in sync at startup
    }

    public void setDisplayPanel(VehicleDisplayPanel displayPanel) {
        this.displayPanel = displayPanel;
    }

    public void setMainView(MainView mainView) {
        this.mainView = mainView;
    }

    private void createComponents() {
        dealerIdField = new TextField();
        dealerIdField.setPrefWidth(180);

        dealerIdComboBox = new ComboBox<>();
        dealerIdComboBox.setPromptText("Select existing dealer");
        dealerIdComboBox.setEditable(false);
        dealerIdComboBox.setPrefWidth(260);

        // NEW: Do not bind directly to ObservableList; always repopulate explicitly
        refreshDealerComboBox();

        HBox dealerSelectionBox = new HBox(10);
        dealerSelectionBox.getChildren().addAll(dealerIdComboBox, dealerIdField);
        dealerSelectionBox.setAlignment(Pos.CENTER_LEFT);

        dealerIdComboBox.setOnAction(e -> {
            if (dealerIdComboBox.getValue() != null) {
                if (dealerIdComboBox.getValue().equals("-- New Dealer --")) {
                    dealerIdField.clear();
                    dealerIdField.setDisable(false);
                } else {
                    dealerIdField.clear();
                    dealerIdField.setDisable(true);
                }
            } else {
                dealerIdField.setDisable(false);
            }
        });

        dealerIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                dealerIdComboBox.setValue(null);
            }
        });

        int buttonWidth = 260;
        enableAcquisitionBtn = createButton("Enable Acquisition", buttonWidth);
        enableAcquisitionBtn.setOnAction(e -> handleEnableAcquisition());
        disableAcquisitionBtn = createButton("Disable Acquisition", buttonWidth);
        disableAcquisitionBtn.setOnAction(e -> handleDisableAcquisition());
        separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        importXmlBtn = createButton("Import XML", buttonWidth);
        importXmlBtn.setOnAction(e -> handleImportXML());
        importJsonBtn = createButton("Import JSON", buttonWidth);
        importJsonBtn.setOnAction(e -> handleImportJSON());
        exportInventoryBtn = createButton("Export Inventory", buttonWidth);
        exportInventoryBtn.setOnAction(e -> handleExportInventory());
        clearExportBtn = createButton("Clear Export", buttonWidth);
        clearExportBtn.setOnAction(e -> handleClearExport());
        showInventoryBtn = createButton("Show Inventory", buttonWidth - 20);
        showInventoryBtn.setOnAction(e -> toggleInventoryVisibility());

        Label dealerLabel = new Label("Dealer ID:");
        dealerLabel.setPrefWidth(70);
        dealerLabel.setMinWidth(70);
        setColumnSpan(dealerSelectionBox, 2);
        setColumnSpan(enableAcquisitionBtn, 2);
        setColumnSpan(disableAcquisitionBtn, 2);
        setColumnSpan(separator, 2);

        add(dealerLabel, 0, 0);
        add(dealerSelectionBox, 0, 0, 2, 1);
        add(enableAcquisitionBtn, 0, 1, 2, 1);
        add(disableAcquisitionBtn, 0, 2, 2, 1);
        add(separator, 0, 3, 2, 1);
        add(importXmlBtn, 0, 4);
        add(importJsonBtn, 0, 5);
        add(exportInventoryBtn, 0, 6);
        add(clearExportBtn, 0, 7);
        add(showInventoryBtn, 1, 7);
    }

    /**
     * Force the ComboBox to clear its value and repopulate the items from the controller.
     * This is the only way to guarantee UI updates after inventory changes.
     */
    public void refreshDealerComboBox() {
        // Get the latest dealer list, including "-- New Dealer --"
        List<String> latestDealers = controller.getDealerList();
        dealerIdComboBox.getItems().clear();
        dealerIdComboBox.getItems().addAll(latestDealers);
        dealerIdComboBox.getSelectionModel().clearSelection();
        dealerIdComboBox.setValue(null);
        dealerIdField.clear();
        dealerIdField.setDisable(false);
    }

    private Button createButton(String text, int width) {
        Button button = new Button(text);
        button.setPrefWidth(width);
        button.setPrefHeight(35);
        button.setStyle("-fx-font-size: 14px;");
        return button;
    }

    public void clearInputFields() {
        dealerIdField.clear();
        dealerIdField.setDisable(false);
        dealerIdComboBox.getSelectionModel().clearSelection();
        dealerIdComboBox.setValue(null);
    }

    public String getDealerId() {
        if (dealerIdComboBox.getValue() != null &&
                !dealerIdComboBox.getValue().isEmpty() &&
                !dealerIdComboBox.getValue().equals("-- New Dealer --")) {
            return dealerIdComboBox.getValue();
        } else {
            return dealerIdField.getText().trim();
        }
    }

    private void toggleInventoryVisibility() {
        inventoryVisible = !inventoryVisible;
        if (mainView != null) {
            mainView.toggleInventoryVisibility(inventoryVisible);
            if (inventoryVisible) {
                showInventoryBtn.setText("Hide Inventory");
            } else {
                showInventoryBtn.setText("Show Inventory");
            }
        } else {
            DialogUtils.showError("MainView reference not set");
        }
    }

    private void handleEnableAcquisition() {
        String dealerId = getDealerId();
        if (dealerId.isEmpty()) {
            DialogUtils.showError("Dealer ID is required to enable acquisition!");
            return;
        }
        if (controller.enableAcquisition(dealerId)) {
            DialogUtils.showSuccess("Acquisition enabled for dealer: " + dealerId);
            if (displayPanel != null) {
                displayPanel.refreshDisplay();
            }
            refreshDealerComboBox();
        }
    }

    private void handleDisableAcquisition() {
        String dealerId = getDealerId();
        if (dealerId.isEmpty()) {
            DialogUtils.showError("Dealer ID is required to disable acquisition!");
            return;
        }
        if (controller.disableAcquisition(dealerId)) {
            DialogUtils.showSuccess("Acquisition disabled for dealer: " + dealerId);
            if (displayPanel != null) {
                displayPanel.refreshDisplay();
            }
            refreshDealerComboBox();
        }
    }

    private void handleImportXML() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open XML File");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            int importCount = controller.importXML(selectedFile);
            if (importCount > 0) {
                if (displayPanel != null) {
                    displayPanel.refreshDisplay();
                }
                refreshDealerComboBox();
                DialogUtils.showSuccess("Successfully imported " + importCount + " vehicles from XML");
            } else {
                if (displayPanel != null) {
                    displayPanel.appendMessage("No vehicles were imported from XML");
                }
            }
        }
    }

    private void handleImportJSON() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open JSON File");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            int importCount = controller.importJSON(selectedFile);
            if (importCount > 0) {
                if (displayPanel != null) {
                    displayPanel.refreshDisplay();
                }
                refreshDealerComboBox();
                DialogUtils.showSuccess("Successfully imported " + importCount + " vehicles from JSON");
            } else {
                if (displayPanel != null) {
                    displayPanel.appendMessage("No vehicles were imported from JSON");
                }
            }
        }
    }

    private void handleExportInventory() {
        if (controller.exportInventory()) {
            DialogUtils.showSuccess("Successfully exported to export.json");
        } else {
            DialogUtils.showError("Failed to export: No vehicles found in inventory");
        }
    }

    private void handleClearExport() {
        try {
            controller.clearExport();
            DialogUtils.showSuccess("export.json has been cleared");
        } catch (Exception e) {
            DialogUtils.showError("Error clearing export.json: " + e.getMessage());
        }
    }
}

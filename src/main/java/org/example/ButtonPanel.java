package org.example.view;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import org.example.controller.DealershipController;
import org.example.VehicleAddWizard;
import org.example.DealershipControllerAdapter;
import org.example.Vehicle;

import java.util.List;

/**
 * ButtonPanel contains all the action buttons for vehicle operations
 * Reorganized to focus on vehicle management operations
 * Only main operations have colorful styling
 */
public class ButtonPanel extends VBox {

    private DealershipController controller;
    private InputPanel inputPanel;
    private VehicleDisplayPanel displayPanel;

    public ButtonPanel(DealershipController controller) {
        this.controller = controller;
        setPadding(new Insets(20));
        setSpacing(10);
        createComponents();
    }

    public void setInputPanel(InputPanel inputPanel) {
        this.inputPanel = inputPanel;
    }

    public void setDisplayPanel(VehicleDisplayPanel displayPanel) {
        this.displayPanel = displayPanel;
    }

    private void createComponents() {
        // Create vehicle management buttons with color styling
        Button addVehicleBtn = createButton("Add Vehicle");
        addVehicleBtn.setOnAction(e -> handleAddVehicle());

        Button removeVehicleBtn = createButton("Remove Vehicle");
        removeVehicleBtn.setOnAction(e -> handleRemoveVehicle());

        Button transferVehicleBtn = createButton("Transfer Vehicle");
        transferVehicleBtn.setOnAction(e -> handleTransferVehicle());

        // Create rental management buttons - moved from InputPanel
        Button rentVehicleBtn = createButton("Rent Vehicle");
        rentVehicleBtn.setOnAction(e -> handleRentVehicle());

        Button returnVehicleBtn = createButton("Return Vehicle");
        returnVehicleBtn.setOnAction(e -> handleReturnVehicle());

        getChildren().addAll(
                addVehicleBtn,
                removeVehicleBtn,
                transferVehicleBtn,
                rentVehicleBtn,
                returnVehicleBtn
        );
    }

    /**
     * Creates a regular button with the given text without color styling
     */
    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(45);
        button.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        return button;
    }

    /**
     * Handles adding a new vehicle using the Kotlin wizard
     */
    private void handleAddVehicle() {
        if (inputPanel != null) {
            inputPanel.refreshDealerComboBox();
        }
        DealershipControllerAdapter adapter = new DealershipControllerAdapter(controller);
        VehicleAddWizard wizard = new VehicleAddWizard(adapter);
        wizard.showAndWait().ifPresent(success -> {
            if (success) {
                if (displayPanel != null) {
                    displayPanel.refreshDisplay();
                }
                if (inputPanel != null) {
                    inputPanel.refreshDealerComboBox();
                }
            }
        });
    }

    /**
     * Handles removing a vehicle from inventory
     */
    private void handleRemoveVehicle() {
        // NEW: Only proceed if inventory is not empty
        List<Vehicle> allVehicles = controller.getVehiclesForDisplay();
        if (allVehicles.isEmpty()) {
            DialogUtils.showError("No vehicles in inventory to remove.");
            return;
        }

        Vehicle selectedVehicle = displayPanel != null ? displayPanel.getSelectedVehicle() : null;
        if (selectedVehicle != null) {
            if (selectedVehicle.isRented()) {
                DialogUtils.showError("Cannot remove a rented vehicle");
                return;
            }
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to remove the vehicle: " +
                            selectedVehicle.getManufacturer() + " " +
                            selectedVehicle.getModel() + " (ID: " +
                            selectedVehicle.getVehicleId() + ")?",
                    ButtonType.YES, ButtonType.NO);
            confirmAlert.setTitle("Confirm Removal");
            confirmAlert.setHeaderText("Remove Vehicle");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    boolean success = controller.removeVehicle(
                            selectedVehicle.getDealerId(),
                            selectedVehicle.getVehicleId(),
                            selectedVehicle.getManufacturer(),
                            selectedVehicle.getModel(),
                            selectedVehicle.getPrice()
                    );
                    if (success) {
                        if (displayPanel != null) {
                            displayPanel.refreshDisplay();
                            displayPanel.clearSelection();
                        }
                        if (inputPanel != null) {
                            inputPanel.clearInputFields();
                            inputPanel.refreshDealerComboBox();
                        }
                        DialogUtils.showSuccess("Vehicle removed successfully");
                    } else {
                        DialogUtils.showError("Failed to remove vehicle");
                    }
                }
            });
        } else {
            if (inputPanel != null) {
                inputPanel.refreshDealerComboBox();
            }
            RemoveVehicleDialog dialog = new RemoveVehicleDialog(controller.getDealershipManager());
            dialog.showAndWait().ifPresent(success -> {
                if (success) {
                    if (displayPanel != null) {
                        displayPanel.refreshDisplay();
                    }
                    if (inputPanel != null) {
                        inputPanel.clearInputFields();
                        inputPanel.refreshDealerComboBox();
                    }
                    DialogUtils.showSuccess("Vehicle removed successfully");
                }
            });
        }
    }

    /**
     * Handles transferring a vehicle between dealerships
     */
    private void handleTransferVehicle() {
        // NEW: Only proceed if inventory is not empty
        List<Vehicle> allVehicles = controller.getVehiclesForDisplay();
        if (allVehicles.isEmpty()) {
            DialogUtils.showError("No vehicles in inventory to transfer.");
            return;
        }

        Vehicle selectedVehicle = displayPanel != null ? displayPanel.getSelectedVehicle() : null;
        if (selectedVehicle != null) {
            if (selectedVehicle.isRented()) {
                DialogUtils.showError("Currently rented vehicles cannot be transferred");
                return;
            }
            if (inputPanel != null) {
                inputPanel.refreshDealerComboBox();
            }
            TransferVehicleDialog dialog = new TransferVehicleDialog(
                    controller.getDealershipManager(),
                    selectedVehicle.getDealerId(),
                    selectedVehicle.getVehicleId());
            dialog.showAndWait().ifPresent(success -> {
                if (success) {
                    if (displayPanel != null) {
                        displayPanel.refreshDisplay();
                        displayPanel.clearSelection();
                    }
                    if (inputPanel != null) {
                        inputPanel.refreshDealerComboBox();
                    }
                    DialogUtils.showSuccess("Vehicle transferred successfully");
                }
            });
        } else {
            if (inputPanel != null) {
                inputPanel.refreshDealerComboBox();
            }
            TransferVehicleDialog dialog = new TransferVehicleDialog(controller.getDealershipManager());
            dialog.showAndWait().ifPresent(success -> {
                if (success) {
                    if (displayPanel != null) {
                        displayPanel.refreshDisplay();
                    }
                    if (inputPanel != null) {
                        inputPanel.refreshDealerComboBox();
                    }
                    DialogUtils.showSuccess("Vehicle transferred successfully");
                }
            });
        }
    }

    /**
     * Handles renting a vehicle
     */
    private void handleRentVehicle() {
        // NEW: Only proceed if there are rentable vehicles
        List<Vehicle> allVehicles = controller.getVehiclesForDisplay();
        boolean hasRentable = allVehicles.stream().anyMatch(v -> v.isRentable() && !v.isRented());
        if (!hasRentable) {
            DialogUtils.showError("No available vehicles to rent.");
            return;
        }

        Vehicle selectedVehicle = displayPanel != null ? displayPanel.getSelectedVehicle() : null;
        if (selectedVehicle != null && selectedVehicle.isRentable() && !selectedVehicle.isRented()) {
            RentVehicleDialog dialog = new RentVehicleDialog(
                    controller.getDealershipManager(),
                    selectedVehicle.getDealerId(),
                    selectedVehicle.getVehicleId());
            dialog.showAndWait().ifPresent(success -> {
                if (success) {
                    if (displayPanel != null) {
                        displayPanel.refreshDisplay();
                        displayPanel.clearSelection();
                    }
                    DialogUtils.showSuccess("Vehicle rented successfully");
                }
            });
        } else {
            if (selectedVehicle != null && !selectedVehicle.isRentable()) {
                DialogUtils.showError("Selected vehicle is not available for rent");
                return;
            }
            if (selectedVehicle != null && selectedVehicle.isRented()) {
                DialogUtils.showError("Selected vehicle is already rented");
                return;
            }
            RentVehicleDialog dialog = new RentVehicleDialog(controller.getDealershipManager());
            dialog.showAndWait().ifPresent(success -> {
                if (success) {
                    if (displayPanel != null) {
                        displayPanel.refreshDisplay();
                    }
                    DialogUtils.showSuccess("Vehicle rented successfully");
                }
            });
        }
    }

    /**
     * Handles returning a rented vehicle
     */
    private void handleReturnVehicle() {
        // NEW: Only proceed if there are rented vehicles
        List<Vehicle> allVehicles = controller.getVehiclesForDisplay();
        boolean hasRented = allVehicles.stream().anyMatch(Vehicle::isRented);
        if (!hasRented) {
            DialogUtils.showError("No vehicles are currently rented.");
            return;
        }

        Vehicle selectedVehicle = displayPanel != null ? displayPanel.getSelectedVehicle() : null;
        if (selectedVehicle != null && selectedVehicle.isRented()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to return the vehicle: " +
                            selectedVehicle.getManufacturer() + " " +
                            selectedVehicle.getModel() + " (ID: " +
                            selectedVehicle.getVehicleId() + ")?",
                    ButtonType.YES, ButtonType.NO);
            confirmAlert.setTitle("Confirm Return");
            confirmAlert.setHeaderText("Return Vehicle");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    boolean success = controller.returnVehicle(
                            selectedVehicle.getDealerId(),
                            selectedVehicle.getVehicleId()
                    );
                    if (success) {
                        if (displayPanel != null) {
                            displayPanel.refreshDisplay();
                            displayPanel.clearSelection();
                        }
                        DialogUtils.showSuccess("Vehicle returned successfully");
                    } else {
                        DialogUtils.showError("Failed to return vehicle");
                    }
                }
            });
        } else {
            if (selectedVehicle != null && !selectedVehicle.isRented()) {
                DialogUtils.showError("Selected vehicle is not currently rented");
                return;
            }
            ReturnVehicleDialog dialog = new ReturnVehicleDialog(controller.getDealershipManager());
            dialog.showAndWait().ifPresent(success -> {
                if (success) {
                    if (displayPanel != null) {
                        displayPanel.refreshDisplay();
                    }
                    DialogUtils.showSuccess("Vehicle returned successfully");
                }
            });
        }
    }
}

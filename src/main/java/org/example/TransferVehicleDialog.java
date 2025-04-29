package org.example.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.DealershipManager;
import org.example.Vehicle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TransferVehicleDialog extends Dialog<Boolean> {
    private DealershipManager manager;
    private ComboBox<String> sourceDealerCombo;
    private ComboBox<String> vehicleCombo;
    private ComboBox<String> targetDealerCombo;
    private TextField newDealerField;

    public TransferVehicleDialog(DealershipManager manager) {
        this.manager = manager;

        setTitle("Transfer Vehicle");
        setHeaderText("Transfer a vehicle between dealerships");

        // Set the button types
        ButtonType transferButtonType = new ButtonType("Transfer Vehicle", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(transferButtonType, ButtonType.CANCEL);

        // Create the selection fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Source dealer dropdown
        sourceDealerCombo = new ComboBox<>();
        sourceDealerCombo.setPromptText("Select Source Dealer");

        // Find all dealers that have vehicles
        Set<String> dealersWithVehicles = new HashSet<>();
        for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
            dealersWithVehicles.add(vehicle.getDealerId());
        }

        // If no vehicles, show a message and return
        if (dealersWithVehicles.isEmpty()) {
            DialogUtils.showError("No vehicles in inventory");
            setResult(false);
            return;
        }

        sourceDealerCombo.getItems().addAll(dealersWithVehicles);

        // Vehicle dropdown for source dealer (initially empty)
        vehicleCombo = new ComboBox<>();
        vehicleCombo.setPromptText("Select a Vehicle");

        // Target dealer section
        Label targetDealerLabel = new Label("Target Dealer:");
        targetDealerCombo = new ComboBox<>();
        targetDealerCombo.setPromptText("Select Existing Dealer");
        newDealerField = new TextField();
        newDealerField.setPromptText("Or Enter New Dealer ID");

        // Populate existing dealers in target dealer dropdown
        targetDealerCombo.getItems().addAll(dealersWithVehicles);
        targetDealerCombo.getItems().add("-- New Dealer --");

        // Listener to handle new dealer option
        targetDealerCombo.setOnAction(e -> {
            if ("-- New Dealer --".equals(targetDealerCombo.getValue())) {
                newDealerField.setDisable(false);
            } else {
                newDealerField.clear();
                newDealerField.setDisable(true);
            }
        });

        // Update vehicle dropdown when source dealer changes
        sourceDealerCombo.setOnAction(e -> {
            String selectedDealer = sourceDealerCombo.getValue();
            if (selectedDealer != null) {
                vehicleCombo.getItems().clear();
                vehicleCombo.getItems().addAll(getAvailableVehiclesForDealer(selectedDealer));

                // Update target dealer dropdown - remove the source dealer
                targetDealerCombo.getItems().clear();
                dealersWithVehicles.stream()
                        .filter(dealer -> !dealer.equals(selectedDealer))
                        .forEach(dealer -> targetDealerCombo.getItems().add(dealer));
                targetDealerCombo.getItems().add("-- New Dealer --");
            }
        });

        grid.add(new Label("Source Dealer:"), 0, 0);
        grid.add(sourceDealerCombo, 1, 0);
        grid.add(new Label("Vehicle:"), 0, 1);
        grid.add(vehicleCombo, 1, 1);
        grid.add(targetDealerLabel, 0, 2);
        grid.add(targetDealerCombo, 1, 2);
        grid.add(new Label("New Dealer ID:"), 0, 3);
        grid.add(newDealerField, 1, 3);

        getDialogPane().setContent(grid);

        // Disable new dealer field initially
        newDealerField.setDisable(true);

        // Convert the result
        setResultConverter(dialogButton -> {
            if (dialogButton == transferButtonType) {
                String sourceDealerId = sourceDealerCombo.getValue();
                String targetDealerId = targetDealerCombo.getValue();

                if ("-- New Dealer --".equals(targetDealerId)) {
                    targetDealerId = newDealerField.getText().trim();
                    if (targetDealerId.isEmpty()) {
                        DialogUtils.showError("Please enter a dealer ID for the new dealer");
                        return false;
                    }
                }

                String vehicleId = getVehicleIdFromFormatted(vehicleCombo.getValue());

                if (sourceDealerId == null || targetDealerId == null || vehicleId == null) {
                    DialogUtils.showError("All fields must be filled");
                    return false;
                }

                if (sourceDealerId.equals(targetDealerId)) {
                    DialogUtils.showError("Source and target dealers cannot be the same");
                    return false;
                }

                // Enable acquisition for target dealer if it's new
                if (!dealersWithVehicles.contains(targetDealerId)) {
                    manager.enableAcquisition(targetDealerId);
                }

                File inventoryFile = new File("src/main/resources/inventory.json");
                boolean success = manager.transferVehicle(sourceDealerId, targetDealerId, vehicleId, inventoryFile);

                if (!success) {
                    DialogUtils.showError("Failed to transfer vehicle. Check dealer IDs and vehicle status.");
                }

                return success;
            }
            return false;
        });
    }

    /**
     * Constructor with pre-selected values
     */
    public TransferVehicleDialog(DealershipManager manager, String sourceDealerId, String vehicleId) {
        this(manager); // Call the default constructor

        // Pre-select the source dealer and vehicle
        sourceDealerCombo.setValue(sourceDealerId);

        // Populate the vehicle combo box with vehicles from the selected dealer
        vehicleCombo.getItems().clear();
        vehicleCombo.getItems().addAll(getAvailableVehiclesForDealer(sourceDealerId));

        // Find and select the specific vehicle
        for (String vehicleOption : vehicleCombo.getItems()) {
            if (getVehicleIdFromFormatted(vehicleOption).equals(vehicleId)) {
                vehicleCombo.setValue(vehicleOption);
                break;
            }
        }
    }

    private List<String> getAvailableVehiclesForDealer(String dealerId) {
        List<String> availableVehicles = new ArrayList<>();

        for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
            if (vehicle.getDealerId().equals(dealerId) && !vehicle.isRented()) {
                availableVehicles.add(vehicle.getVehicleId() + " - " +
                        vehicle.getManufacturer() + " " +
                        vehicle.getModel());
            }
        }

        return availableVehicles;
    }

    private String getVehicleIdFromFormatted(String formattedVehicle) {
        if (formattedVehicle == null || formattedVehicle.isEmpty()) return null;
        int index = formattedVehicle.indexOf(" - ");
        if (index > 0) {
            return formattedVehicle.substring(0, index);
        }
        return formattedVehicle;
    }
}
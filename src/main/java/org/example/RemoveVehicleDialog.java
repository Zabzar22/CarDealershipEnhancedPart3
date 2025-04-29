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

public class RemoveVehicleDialog extends Dialog<Boolean> {
    private DealershipManager manager;

    public RemoveVehicleDialog(DealershipManager manager) {
        this.manager = manager;

        setTitle("Remove Vehicle");
        setHeaderText("Select a vehicle to remove");

        // Set the button types
        ButtonType removeButtonType = new ButtonType("Remove Vehicle", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(removeButtonType, ButtonType.CANCEL);

        // Create the selection fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create dealer dropdown
        ComboBox<String> dealerCombo = new ComboBox<>();
        dealerCombo.setPromptText("Select a dealer");

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

        dealerCombo.getItems().addAll(dealersWithVehicles);

        // Create vehicle dropdown (initially empty)
        ComboBox<String> vehicleCombo = new ComboBox<>();
        vehicleCombo.setPromptText("Select a vehicle");

        // Additional input fields
        TextField manufacturerField = new TextField();
        manufacturerField.setPromptText("Manufacturer");
        manufacturerField.setEditable(false);
        TextField modelField = new TextField();
        modelField.setPromptText("Model");
        modelField.setEditable(false);
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.setEditable(false);

        // Update vehicle dropdown when dealer changes
        dealerCombo.setOnAction(e -> {
            String selectedDealer = dealerCombo.getValue();
            if (selectedDealer != null) {
                vehicleCombo.getItems().clear();
                vehicleCombo.getItems().addAll(getVehiclesForDealer(selectedDealer));
            }
        });

        // Populate additional fields when vehicle is selected
        vehicleCombo.setOnAction(e -> {
            String selectedVehicle = vehicleCombo.getValue();
            if (selectedVehicle != null) {
                String vehicleId = getVehicleIdFromFormatted(selectedVehicle);
                Vehicle vehicle = findVehicleById(dealerCombo.getValue(), vehicleId);
                if (vehicle != null) {
                    manufacturerField.setText(vehicle.getManufacturer());
                    modelField.setText(vehicle.getModel());
                    priceField.setText(String.format("%.2f", vehicle.getPrice()));
                }
            }
        });

        grid.add(new Label("Dealer:"), 0, 0);
        grid.add(dealerCombo, 1, 0);
        grid.add(new Label("Vehicle:"), 0, 1);
        grid.add(vehicleCombo, 1, 1);
        grid.add(new Label("Manufacturer:"), 0, 2);
        grid.add(manufacturerField, 1, 2);
        grid.add(new Label("Model:"), 0, 3);
        grid.add(modelField, 1, 3);
        grid.add(new Label("Price:"), 0, 4);
        grid.add(priceField, 1, 4);

        getDialogPane().setContent(grid);

        // Convert the result
        setResultConverter(dialogButton -> {
            if (dialogButton == removeButtonType) {
                try {
                    String dealerId = dealerCombo.getValue();
                    String vehicleId = getVehicleIdFromFormatted(vehicleCombo.getValue());
                    String manufacturer = manufacturerField.getText();
                    String model = modelField.getText();
                    double price = Double.parseDouble(priceField.getText());

                    if (dealerId == null || vehicleId == null) {
                        DialogUtils.showError("Dealer and Vehicle must be selected");
                        return false;
                    }

                    File inventoryFile = new File("src/main/resources/inventory.json");
                    boolean success = manager.removeVehicleFromInventory(
                            dealerId, vehicleId, manufacturer, model, price, inventoryFile);

                    if (!success) {
                        DialogUtils.showError("Failed to remove vehicle. Vehicle may be rented or not found.");
                    }

                    return success;
                } catch (NumberFormatException | NullPointerException ex) {
                    DialogUtils.showError("Invalid input. Please select a vehicle.");
                    return false;
                }
            }
            return false;
        });
    }

    private List<String> getVehiclesForDealer(String dealerId) {
        List<String> formattedVehicles = new ArrayList<>();
        for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
            if (vehicle.getDealerId().equals(dealerId)) {
                String formatted = String.format("%s - %s %s ($%.2f)",
                        vehicle.getVehicleId(),
                        vehicle.getManufacturer(),
                        vehicle.getModel(),
                        vehicle.getPrice());
                formattedVehicles.add(formatted);
            }
        }
        return formattedVehicles;
    }

    private String getVehicleIdFromFormatted(String formattedVehicle) {
        if (formattedVehicle == null || formattedVehicle.isEmpty()) return "";
        int index = formattedVehicle.indexOf(" - ");
        if (index > 0) {
            return formattedVehicle.substring(0, index);
        }
        return formattedVehicle;
    }

    private Vehicle findVehicleById(String dealerId, String vehicleId) {
        return manager.getVehiclesForDisplay().stream()
                .filter(vehicle -> vehicle.getDealerId().equals(dealerId) &&
                        vehicle.getVehicleId().equals(vehicleId))
                .findFirst()
                .orElse(null);
    }
}
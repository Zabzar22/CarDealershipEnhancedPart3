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

public class ReturnVehicleDialog extends Dialog<Boolean> {
    private DealershipManager manager;

    public ReturnVehicleDialog(DealershipManager manager) {
        this.manager = manager;

        setTitle("Return Vehicle");
        setHeaderText("Select a rented vehicle to return");

        // Set the button types
        ButtonType confirmButtonType = new ButtonType("Return Vehicle", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Get the return button and style it
        Button returnButton = (Button) getDialogPane().lookupButton(confirmButtonType);
        returnButton.setStyle("-fx-background-color: #E0A0A0;"); // Light red color

        // Create the selection fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create dealer dropdown
        ComboBox<String> dealerCombo = new ComboBox<>();
        dealerCombo.setPromptText("Select a dealer");

        // Find all dealers that have rented vehicles
        Set<String> dealersWithRentedVehicles = new HashSet<>();
        for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
            if (vehicle.isRented()) {
                dealersWithRentedVehicles.add(vehicle.getDealerId());
            }
        }

        // If no rented vehicles, show a message and return
        if (dealersWithRentedVehicles.isEmpty()) {
            DialogUtils.showError("No vehicles are currently rented");
            setResult(false);
            return;
        }

        dealerCombo.getItems().addAll(dealersWithRentedVehicles);

        // Create vehicle dropdown (initially empty)
        ComboBox<String> vehicleCombo = new ComboBox<>();
        vehicleCombo.setPromptText("Select a rented vehicle");

        // Update vehicle dropdown when dealer changes
        dealerCombo.setOnAction(e -> {
            String selectedDealer = dealerCombo.getValue();
            if (selectedDealer != null) {
                vehicleCombo.getItems().clear();
                vehicleCombo.getItems().addAll(getRentedVehiclesForDealer(selectedDealer));
            }
        });

        grid.add(new Label("Dealer:"), 0, 0);
        grid.add(dealerCombo, 1, 0);
        grid.add(new Label("Vehicle:"), 0, 1);
        grid.add(vehicleCombo, 1, 1);

        getDialogPane().setContent(grid);

        // Convert the result
        setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                String dealerId = dealerCombo.getValue();
                String vehicleId = getVehicleIdFromFormatted(vehicleCombo.getValue());

                if (dealerId == null || vehicleId == null || vehicleId.isEmpty()) {
                    DialogUtils.showError("Both dealer and vehicle must be selected");
                    return false;
                }

                File inventoryFile = new File("src/main/resources/inventory.json");
                boolean success = manager.returnVehicle(dealerId, vehicleId, inventoryFile);

                if (!success) {
                    DialogUtils.showError("Failed to return vehicle. An unexpected error occurred.");
                }

                return success;
            }
            return false;
        });
    }

    private List<String> getRentedVehiclesForDealer(String dealerId) {
        List<String> rentedVehicles = new ArrayList<>();

        for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
            if (vehicle.getDealerId().equals(dealerId) && vehicle.isRented()) {
                rentedVehicles.add(vehicle.getVehicleId() + " - " +
                        vehicle.getManufacturer() + " " +
                        vehicle.getModel());
            }
        }

        return rentedVehicles;
    }

    private String getVehicleIdFromFormatted(String formattedVehicle) {
        if (formattedVehicle == null || formattedVehicle.isEmpty()) return "";
        int index = formattedVehicle.indexOf(" - ");
        if (index > 0) {
            return formattedVehicle.substring(0, index);
        }
        return formattedVehicle;
    }
}
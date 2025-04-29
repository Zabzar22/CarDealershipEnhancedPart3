package org.example.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.DealershipManager;
import org.example.Vehicle;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RentVehicleDialog extends Dialog<Boolean> {

    private ComboBox<String> dealerIdComboBox;
    private ComboBox<String> vehicleIdComboBox;
    private TextField startDateField;
    private TextField endDateField;
    private DealershipManager manager;

    public RentVehicleDialog(DealershipManager manager) {
        this.manager = manager;
        setupDialog();
    }

    public RentVehicleDialog(DealershipManager manager, String dealerId, String vehicleId) {
        this.manager = manager;
        setupDialog();
        // Pre-select the dealer and vehicle
        dealerIdComboBox.setValue(dealerId);
        updateVehicleComboBox();
        vehicleIdComboBox.setValue(vehicleId);
        // Lock the fields
        dealerIdComboBox.setDisable(true);
        vehicleIdComboBox.setDisable(true);
    }

    private void setupDialog() {
        setTitle("Rent Vehicle");
        setHeaderText("Enter rental details");

        // Set the button types
        ButtonType rentButtonType = new ButtonType("Rent", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(rentButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Dealer ID dropdown
        dealerIdComboBox = new ComboBox<>();
        dealerIdComboBox.setPromptText("Select Dealer");
        populateDealerComboBox();

        // Vehicle ID dropdown (will be populated based on dealer selection)
        vehicleIdComboBox = new ComboBox<>();
        vehicleIdComboBox.setPromptText("Select Vehicle");

        // Date fields
        startDateField = new TextField();
        startDateField.setPromptText("MM/dd/yyyy");
        endDateField = new TextField();
        endDateField.setPromptText("MM/dd/yyyy");

        // Add fields to grid
        grid.add(new Label("Dealer ID:"), 0, 0);
        grid.add(dealerIdComboBox, 1, 0);
        grid.add(new Label("Vehicle ID:"), 0, 1);
        grid.add(vehicleIdComboBox, 1, 1);
        grid.add(new Label("Start Date (MM/dd/yyyy):"), 0, 2);
        grid.add(startDateField, 1, 2);
        grid.add(new Label("End Date (MM/dd/yyyy):"), 0, 3);
        grid.add(endDateField, 1, 3);

        // Set up dealer selection listener
        dealerIdComboBox.setOnAction(e -> updateVehicleComboBox());

        // Add the grid to the dialog
        getDialogPane().setContent(grid);

        // Request focus on the start date field by default
        startDateField.requestFocus();

        // Convert the result to Boolean when the rent button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == rentButtonType) {
                return processRental();
            }
            return false;
        });
    }

    private void populateDealerComboBox() {
        dealerIdComboBox.getItems().clear();
        for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
            if (!vehicle.isRented() && vehicle.isRentable()) {
                String dealerId = vehicle.getDealerId();
                if (!dealerIdComboBox.getItems().contains(dealerId)) {
                    dealerIdComboBox.getItems().add(dealerId);
                }
            }
        }
    }

    private void updateVehicleComboBox() {
        vehicleIdComboBox.getItems().clear();
        String selectedDealer = dealerIdComboBox.getValue();
        if (selectedDealer != null) {
            for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
                if (vehicle.getDealerId().equals(selectedDealer) &&
                        !vehicle.isRented() && vehicle.isRentable()) {
                    vehicleIdComboBox.getItems().add(vehicle.getVehicleId());
                }
            }
        }
    }

    private boolean validateDates() {
        String startDateStr = startDateField.getText();
        String endDateStr = endDateField.getText();
        if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
            DialogUtils.showError("Both start and end dates are required.");
            return false;
        }

        // Create date formatter with strict parsing
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);

        // Parse dates
        Date startDate;
        Date endDate;
        try {
            startDate = dateFormat.parse(startDateStr);
            endDate = dateFormat.parse(endDateStr);
        } catch (ParseException e) {
            DialogUtils.showError("Invalid date format. Please use MM/dd/yyyy format.");
            return false;
        }

        // NEW: Compare only the date part (ignore time) for today's date
        Date currentDate;
        try {
            currentDate = dateFormat.parse(dateFormat.format(new Date()));
        } catch (ParseException e) {
            currentDate = new Date(); // fallback, shouldn't happen
        }
        if (startDate.before(currentDate)) {
            DialogUtils.showError("Start date cannot be in the past.");
            return false;
        }

        // Validate date ranges
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        int startYear = calendar.get(Calendar.YEAR);
        calendar.setTime(endDate);
        int endYear = calendar.get(Calendar.YEAR);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (startYear < currentYear || startYear > currentYear + 10 ||
                endYear < currentYear || endYear > currentYear + 10) {
            DialogUtils.showError("Invalid date range: Years must be between " +
                    currentYear + " and " + (currentYear + 10));
            return false;
        }

        // Validate end date is after start date
        if (endDate.before(startDate)) {
            DialogUtils.showError("End date must be after start date.");
            return false;
        }

        return true;
    }

    private boolean processRental() {
        String dealerId = dealerIdComboBox.getValue();
        String vehicleId = vehicleIdComboBox.getValue();
        String startDate = startDateField.getText();
        String endDate = endDateField.getText();

        // Validate inputs
        if (dealerId == null || dealerId.isEmpty()) {
            DialogUtils.showError("Please select a dealer.");
            return false;
        }
        if (vehicleId == null || vehicleId.isEmpty()) {
            DialogUtils.showError("Please select a vehicle.");
            return false;
        }
        if (!validateDates()) {
            return false;
        }

        // Process the rental
        File inventoryFile = new File("src/main/resources/inventory.json");
        boolean success = manager.rentVehicle(dealerId, vehicleId, startDate, endDate, inventoryFile);
        if (!success) {
            DialogUtils.showError("Failed to rent vehicle. Please check your inputs.");
            return false;
        }
        return true;
    }
}

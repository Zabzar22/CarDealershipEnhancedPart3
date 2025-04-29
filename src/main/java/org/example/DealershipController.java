package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.Dealership;
import org.example.DealershipManager;
import org.example.Vehicle;
import org.example.view.DialogUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DealershipController {
    private static final String INVENTORY_PATH = "src/main/resources/inventory.json";
    private static final String EXPORT_PATH = "src/main/resources/export.json";
    private DealershipManager manager;

    // Observable collections for UI binding
    private ObservableList<String> dealerList = FXCollections.observableArrayList();
    private ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();

    // Add inventory change listener support
    private List<InventoryChangeListener> inventoryChangeListeners = new ArrayList<>();

    public DealershipController() {
        manager = new DealershipManager();
    }

    public void loadInitialInventory() {
        File inventoryFile = new File(INVENTORY_PATH);
        if (inventoryFile.exists()) {
            manager.readInventoryFile(inventoryFile);
            refreshAllViews();
        }
    }

    // Refreshes all views and notifies listeners of data changes
    public void refreshAllViews() {
        updateVehicleList();
        updateDealerList();
        notifyInventoryChangeListeners();
    }

    public void updateVehicleList() {
        vehicleList.clear();
        vehicleList.addAll(manager.getVehiclesForDisplay());
    }

    public void updateDealerList() {
        dealerList.clear();
        // Add "New Dealer" option
        dealerList.add("-- New Dealer --");
        // Add all unique dealer IDs
        Set<String> dealerIds = new HashSet<>();
        for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
            dealerIds.add(vehicle.getDealerId());
        }
        dealerList.addAll(dealerIds);
    }

    public boolean addVehicle(Vehicle vehicle, String dealerId) {
        vehicle.setDealerId(dealerId);
        File inventoryFile = new File(INVENTORY_PATH);
        // First check for duplicate ID to provide specific error
        for (Vehicle existingVehicle : manager.getVehiclesForDisplay()) {
            if (existingVehicle.getVehicleId().equals(vehicle.getVehicleId())) {
                DialogUtils.showError("Failed to add vehicle: A vehicle with ID '" +
                        vehicle.getVehicleId() + "' already exists");
                return false;
            }
        }

        // Then check if acquisition is disabled
        if (!manager.isAcquisitionEnabled(dealerId)) {
            DialogUtils.showError("Cannot add vehicle: Acquisition is disabled for dealer " + dealerId);
            return false;
        }

        // If no errors are detected, try to add the vehicle
        boolean success = manager.addVehicleToInventory(vehicle, inventoryFile);
        if (success) {
            refreshAllViews();
        }
        return success;
    }

    public boolean removeVehicle(String dealerId, String vehicleId, String manufacturer, String model, double price) {
        File inventoryFile = new File(INVENTORY_PATH);
        boolean success = manager.removeVehicleFromInventory(
                dealerId, vehicleId, manufacturer, model, price, inventoryFile);
        if (success) {
            refreshAllViews();
        }
        return success;
    }

    public boolean rentVehicle(String dealerId, String vehicleId, String startDate, String endDate) {
        File inventoryFile = new File(INVENTORY_PATH);
        boolean success = manager.rentVehicle(dealerId, vehicleId, startDate, endDate, inventoryFile);
        if (success) {
            refreshAllViews();
        }
        return success;
    }

    public boolean returnVehicle(String dealerId, String vehicleId) {
        File inventoryFile = new File(INVENTORY_PATH);
        boolean success = manager.returnVehicle(dealerId, vehicleId, inventoryFile);
        if (success) {
            refreshAllViews();
        }
        return success;
    }

    public boolean transferVehicle(String sourceDealerId, String targetDealerId, String vehicleId) {
        File inventoryFile = new File(INVENTORY_PATH);
        boolean success = manager.transferVehicle(sourceDealerId, targetDealerId, vehicleId, inventoryFile);
        if (success) {
            refreshAllViews();
        }
        return success;
    }

    public int importXML(File xmlFile) {
        File inventoryFile = new File(INVENTORY_PATH);
        int count = manager.importXMLFile(xmlFile, inventoryFile);
        if (count > 0) {
            refreshAllViews();
        }
        return count;
    }

    public int importJSON(File jsonFile) {
        File inventoryFile = new File(INVENTORY_PATH);
        int count = manager.importJSONFile(jsonFile, inventoryFile);
        if (count > 0) {
            refreshAllViews();
        }
        return count;
    }

    public boolean enableAcquisition(String dealerId) {
        boolean result = manager.enableAcquisition(dealerId);
        refreshAllViews(); // Refresh to reflect changes
        return result;
    }

    public boolean disableAcquisition(String dealerId) {
        boolean result = manager.disableAcquisition(dealerId);
        refreshAllViews(); // Refresh to reflect changes
        return result;
    }

    public boolean exportInventory() {
        File inventoryFile = new File(INVENTORY_PATH);
        File exportFile = new File(EXPORT_PATH);
        return manager.exportInventoryToExport(inventoryFile, exportFile);
    }

    public void clearExport() {
        manager.clearExportFile(new File(EXPORT_PATH));
    }

    /**
     * Clears all inventory data
     * @return true if successful, false otherwise
     */
    public boolean clearAllInventory() {
        File inventoryFile = new File(INVENTORY_PATH);
        boolean success = manager.clearAllInventory(inventoryFile);
        if (success) {
            refreshAllViews();
        }
        return success;
    }

    /**
     * Creates a backup of the current inventory
     * @param backupFile The file to save the backup to
     * @return true if successful, false otherwise
     */
    public boolean createInventoryBackup(File backupFile) {
        return manager.exportInventoryBackup(backupFile);
    }

    public List<Vehicle> searchVehicles(String query, String searchType) {
        if (query == null || query.trim().isEmpty()) {
            return manager.getVehiclesForDisplay();
        }

        String searchQuery = query.toLowerCase().trim();
        return manager.getVehiclesForDisplay().stream()
                .filter(vehicle -> matchesSearchCriteria(vehicle, searchType, searchQuery))
                .collect(Collectors.toList());
    }

    private boolean matchesSearchCriteria(Vehicle vehicle, String searchType, String query) {
        switch (searchType) {
            case "ID":
                return vehicle.getVehicleId().toLowerCase().contains(query);
            case "Manufacturer":
                return vehicle.getManufacturer().toLowerCase().contains(query);
            case "Model":
                return vehicle.getModel().toLowerCase().contains(query);
            case "Dealer ID":
                return vehicle.getDealerId().toLowerCase().contains(query);
            case "Type":
                return vehicle.getClass().getSimpleName().toLowerCase().contains(query);
            case "All Fields":
            default:
                return vehicle.getVehicleId().toLowerCase().contains(query) ||
                        vehicle.getManufacturer().toLowerCase().contains(query) ||
                        vehicle.getModel().toLowerCase().contains(query) ||
                        vehicle.getDealerId().toLowerCase().contains(query) ||
                        vehicle.getClass().getSimpleName().toLowerCase().contains(query);
        }
    }

    public ObservableList<String> getDealerList() {
        return dealerList;
    }

    public ObservableList<Vehicle> getVehicleList() {
        return vehicleList;
    }

    public List<Vehicle> getVehiclesForDisplay() {
        return manager.getVehiclesForDisplay();
    }

    public DealershipManager getDealershipManager() {
        return manager;
    }

    /**
     * Interface for inventory change listeners
     */
    public interface InventoryChangeListener {
        void onInventoryChanged();
    }

    /**
     * Adds a listener that will be notified when inventory changes
     * @param listener The listener to add
     */
    public void addInventoryChangeListener(InventoryChangeListener listener) {
        inventoryChangeListeners.add(listener);
    }

    /**
     * Notifies all registered listeners that inventory has changed
     */
    private void notifyInventoryChangeListeners() {
        for (InventoryChangeListener listener : inventoryChangeListeners) {
            listener.onInventoryChanged();
        }
    }
}

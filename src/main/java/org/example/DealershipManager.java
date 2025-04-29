package org.example;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The DealershipManager class manages all the dealerships and the inventories.
 * This class provides the methods add, remove, export vehicle, enable acquisition, and lastly disable acquisition.
 */
public class DealershipManager {

    private Map<String, Dealership> dealerships = new HashMap<>(); // stores the dealership by their id
    private final JSONFileHandler jsonFileHandler = new JSONFileHandler(); // Handles all the JSON files
    private final XMLFileHandler xmlFileHandler = new XMLFileHandler(); // Handles XML import

    /**
     * Reads the inventory and loads the vehicles into their respective dealership
     * @param file The inventory file that you want to read from
     */
    public void readInventoryFile(File file) {
        List<Vehicle> vehicles = jsonFileHandler.readInventory(file);
        Map<String, Boolean> acquisitionStates = jsonFileHandler.readAcquisitionStates(file);

        for (Vehicle vehicle : vehicles) {
            String dealerId = vehicle.getDealerId();
            String dealerName = null;
            // Check if dealer name is in metadata
            if (vehicle.getMetadata().containsKey("dealer_name")) {
                dealerName = (String) vehicle.getMetadata().get("dealer_name");
            }

            // Get or create the dealership
            Dealership dealership = dealerships.get(dealerId);
            if (dealership == null) {
                dealership = new Dealership(dealerId, dealerName);
                dealerships.put(dealerId, dealership);
            } else if (dealerName != null && !dealerName.isEmpty()) {
                dealership.setName(dealerName);
            }

            // Set acquisition state from saved state
            if (acquisitionStates.containsKey(dealerId)) {
                if (acquisitionStates.get(dealerId)) {
                    dealership.enableAcquisition();
                } else {
                    dealership.disableAcquisition();
                }
            }

            // Add the vehicle
            dealership.addVehicle(vehicle);
        }
    }

    /**
     * A command to add a vehicle to a dealership
     * @param dealerId Unique id for dealership
     * @param vehicle The vehicle you want added
     * @return true if the vehicle was added, false otherwise
     */
    public boolean processAddVehicleCommand(String dealerId, Vehicle vehicle) {
        return processAddVehicleCommand(dealerId, vehicle, null);
    }

    /**
     * A command to add a vehicle to a dealership with dealer name
     * @param dealerId Unique id for dealership
     * @param vehicle The vehicle you want added
     * @param dealerName Optional dealer name
     * @return true if the vehicle was added, false otherwise
     */
    public boolean processAddVehicleCommand(String dealerId, Vehicle vehicle, String dealerName) {
        Dealership dealership = dealerships.get(dealerId);
        if (dealership == null) {
            dealership = new Dealership(dealerId, dealerName);
            dealership.enableAcquisition();
            dealerships.put(dealerId, dealership);
        } else if (dealerName != null && !dealerName.isEmpty()) {
            dealership.setName(dealerName);
        }

        if (!dealership.isAcquisitionEnabled()) {
            System.out.println("Cannot add vehicle: Acquisition disabled for dealer " + dealerId);
            return false;
        }

        if (dealership.addVehicle(vehicle)) {
            System.out.println("Vehicle added successfully to dealer " + dealerId);
            return true;
        } else {
            System.out.println("Failed to add vehicle: Duplicate vehicle ID");
            return false;
        }
    }

    /**
     * Adds a vehicle to the inventory as well as updating the dealership
     * @param vehicle The vehicle you want added
     * @param inventoryFile The file where the inventory is stored
     * @return true if the vehicle was added, false otherwise
     */
    public boolean addVehicleToInventory(Vehicle vehicle, File inventoryFile) {
        // No error checking or messages here - let the controller handle that
        boolean result = processAddVehicleCommand(vehicle.getDealerId(), vehicle);
        if (result) {
            saveState(inventoryFile);
        }
        return result;
    }

    /**
     * Get acquisition states map for all dealerships
     * @return Map of dealer IDs to acquisition states
     */
    private Map<String, Boolean> getAcquisitionStates() {
        Map<String, Boolean> states = new HashMap<>();
        for (Map.Entry<String, Dealership> entry : dealerships.entrySet()) {
            states.put(entry.getKey(), entry.getValue().isAcquisitionEnabled());
        }
        return states;
    }

    /**
     * Auto-saves the current state to the inventory file
     * @param inventoryFile The file to save to
     */
    private void saveState(File inventoryFile) {
        List<Vehicle> allVehicles = getVehiclesForDisplay();
        Map<String, Boolean> acquisitionStates = getAcquisitionStates();
        jsonFileHandler.writeInventory(allVehicles, acquisitionStates, inventoryFile);
    }

    /**
     * Removes a vehicle from inventory and dealership lists.
     * @param dealerId The unique id of a dealership
     * @param vehicleId The id of the vehicle you want removed
     * @param manufacturer The manufacturer of the vehicle
     * @param model The model of the vehicle
     * @param price The price of the vehicle
     * @param inventoryFile The file where the inventory is stored
     * @return true if the vehicle was removed, otherwise false
     */
    public boolean removeVehicleFromInventory(String dealerId, String vehicleId, String manufacturer, String model, double price, File inventoryFile) {
        // Find the dealership
        Dealership dealership = dealerships.get(dealerId);
        if (dealership == null) {
            return false;
        }

        // Find the vehicle in the dealership
        Vehicle vehicleToRemove = null;
        for (Vehicle vehicle : dealership.getVehicles()) {
            if (vehicle.getVehicleId().equals(vehicleId) &&
                    vehicle.getManufacturer().equals(manufacturer) &&
                    vehicle.getModel().equals(model) &&
                    Math.abs(vehicle.getPrice() - price) < 0.01) {
                vehicleToRemove = vehicle;
                break;
            }
        }

        if (vehicleToRemove == null) {
            return false;
        }

        // Can't remove a rented vehicle
        if (vehicleToRemove.isRented()) {
            return false;
        }

        // Create a new dealership instance with the same ID and name
        Dealership updatedDealership = new Dealership(dealerId, dealership.getName());
        // Set acquisition state based on original dealership
        if (dealership.isAcquisitionEnabled()) {
            updatedDealership.enableAcquisition();
        } else {
            updatedDealership.disableAcquisition();
        }

        // Add all vehicles except the one to remove
        for (Vehicle v : dealership.getVehicles()) {
            if (!v.getVehicleId().equals(vehicleToRemove.getVehicleId())) {
                updatedDealership.addVehicle(v);
            }
        }

        // Replace the old dealership with the updated one
        dealerships.put(dealerId, updatedDealership);
        // Save updated state
        saveState(inventoryFile);
        return true;
    }

    /**
     * Exports the inventory to an external file
     * @param inventoryFile The inventory file
     * @param exportFile The destination export file
     * @return true if export is successful, otherwise false
     */
    public boolean exportInventoryToExport(File inventoryFile, File exportFile) {
        // First try to read from file
        List<Vehicle> inventory = jsonFileHandler.readInventory(inventoryFile);
        // If no vehicles in file, check in-memory vehicles
        if (inventory.isEmpty()) {
            inventory = getVehiclesForDisplay();
        }
        // If still no vehicles, return false
        if (inventory.isEmpty()) {
            System.out.println("No vehicles to export.");
            return false;
        }
        try {
            jsonFileHandler.writeInventory(inventory, exportFile);
            System.out.println("Exported " + inventory.size() + " vehicles to export.json");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Clears the export file by writing an empty inventory list
     * @param exportFile The file you want cleared
     */
    public void clearExportFile(File exportFile) {
        List<Vehicle> emptyList = new ArrayList<>();
        jsonFileHandler.writeInventory(emptyList, exportFile);
    }

    /**
     * Enables vehicle acquisition for a specific dealership
     * @param dealerId The unique id of the dealership
     * @return true after acquisition was enabled
     */
    public boolean enableAcquisition(String dealerId) {
        Dealership dealership = dealerships.get(dealerId);
        if (dealership == null) {
            dealership = new Dealership(dealerId);
            dealerships.put(dealerId, dealership);
        }
        dealership.enableAcquisition();
        // Save the state to persist the acquisition setting
        File inventoryFile = new File("src/main/resources/inventory.json");
        saveState(inventoryFile);
        return true;
    }

    /**
     * Disables vehicle acquisition for a specific dealership
     * @param dealerId The unique id of the dealership
     * @return true after disabling acquisition
     */
    public boolean disableAcquisition(String dealerId) {
        Dealership dealership = dealerships.get(dealerId);
        if (dealership == null) {
            dealership = new Dealership(dealerId);
            dealerships.put(dealerId, dealership);
        }
        dealership.disableAcquisition();
        // Save the state to persist the acquisition setting
        File inventoryFile = new File("src/main/resources/inventory.json");
        saveState(inventoryFile);
        return true;
    }

    /**
     * Checks if acquisition is enabled for a specific dealership
     * @param dealerId The unique id of the dealership
     * @return true if acquisition is enabled or dealership doesn't exist, false otherwise
     */
    public boolean isAcquisitionEnabled(String dealerId) {
        Dealership dealership = dealerships.get(dealerId);
        if (dealership == null) {
            return true; // New dealerships default to enabled
        }
        return dealership.isAcquisitionEnabled();
    }

    /**
     * Imports vehicles from an XML file
     * @param xmlFile The XML file to import
     * @param inventoryFile The inventory file to update
     * @return Number of vehicles successfully imported
     */
    public int importXMLFile(File xmlFile, File inventoryFile) {
        List<Vehicle> importedVehicles = xmlFileHandler.importXML(xmlFile);
        int successCount = 0;
        for (Vehicle vehicle : importedVehicles) {
            String dealerId = vehicle.getDealerId();
            String dealerName = null;
            if (vehicle.getMetadata().containsKey("dealer_name")) {
                dealerName = (String) vehicle.getMetadata().get("dealer_name");
            }
            if (processAddVehicleCommand(dealerId, vehicle, dealerName)) {
                successCount++;
            }
        }
        if (successCount > 0) {
            saveState(inventoryFile);
        }
        return successCount;
    }

    /**
     * Imports vehicles from a JSON file
     * @param jsonFile The JSON file to import
     * @param inventoryFile The inventory file to update
     * @return Number of vehicles successfully imported
     */
    public int importJSONFile(File jsonFile, File inventoryFile) {
        List<Vehicle> importedVehicles = jsonFileHandler.readInventory(jsonFile);
        int successCount = 0;
        for (Vehicle vehicle : importedVehicles) {
            String dealerId = vehicle.getDealerId();
            String dealerName = null;
            if (vehicle.getMetadata().containsKey("dealer_name")) {
                dealerName = (String) vehicle.getMetadata().get("dealer_name");
            }
            if (processAddVehicleCommand(dealerId, vehicle, dealerName)) {
                successCount++;
            }
        }
        if (successCount > 0) {
            saveState(inventoryFile);
        }
        return successCount;
    }

    /**
     * Transfers a vehicle from one dealership to another
     * @param sourceDealerId The ID of the source dealership
     * @param targetDealerId The ID of the target dealership
     * @param vehicleId The ID of the vehicle to transfer
     * @param inventoryFile The inventory file to update
     * @return true if transfer was successful, false otherwise
     */
    public boolean transferVehicle(String sourceDealerId, String targetDealerId, String vehicleId, File inventoryFile) {
        Dealership sourceDealership = dealerships.get(sourceDealerId);
        Dealership targetDealership = dealerships.get(targetDealerId);
        if (sourceDealership == null || targetDealership == null) return false;
        if (!targetDealership.isAcquisitionEnabled()) return false;
        boolean result = sourceDealership.transferVehicle(vehicleId, targetDealership);
        if (result) {
            saveState(inventoryFile);
        }
        return result;
    }

    /**
     * Rents a vehicle
     * @param dealerId The dealer ID
     * @param vehicleId The vehicle ID
     * @param startDateStr The rental start date string (MM/dd/yyyy)
     * @param endDateStr The rental end date string (MM/dd/yyyy)
     * @param inventoryFile The inventory file to update
     * @return true if successful, false otherwise
     */
    public boolean rentVehicle(String dealerId, String vehicleId, String startDateStr, String endDateStr, File inventoryFile) {
        try {
            Dealership dealership = dealerships.get(dealerId);
            if (dealership == null) return false;
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
                System.err.println("Invalid date format. Please use MM/dd/yyyy format.");
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
                System.err.println("Start date cannot be in the past.");
                return false;
            }

            // Validate date ranges
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            int startYear = calendar.get(Calendar.YEAR);
            calendar.setTime(endDate);
            int endYear = calendar.get(Calendar.YEAR);
            // Check if years are reasonable (between current year and current year + 10)
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            if (startYear < currentYear || startYear > currentYear + 10 ||
                    endYear < currentYear || endYear > currentYear + 10) {
                System.err.println("Invalid date range: Years must be between " +
                        currentYear + " and " + (currentYear + 10));
                return false;
            }

            // Validate end date is after start date
            if (endDate.before(startDate)) {
                System.err.println("End date must be after start date.");
                return false;
            }

            boolean result = dealership.rentVehicle(vehicleId, startDate, endDate);
            if (result) {
                saveState(inventoryFile);
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error processing rental: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns a rented vehicle
     * @param dealerId The dealer ID
     * @param vehicleId The vehicle ID
     * @param inventoryFile The inventory file to update
     * @return true if successful, false otherwise
     */
    public boolean returnVehicle(String dealerId, String vehicleId, File inventoryFile) {
        Dealership dealership = dealerships.get(dealerId);
        if (dealership == null) return false;
        boolean result = dealership.returnVehicle(vehicleId);
        if (result) {
            saveState(inventoryFile);
        }
        return result;
    }

    /**
     * Gets a list of all vehicles for all the dealerships
     * @return A list containing all vehicles in all the dealerships
     */
    public List<Vehicle> getVehiclesForDisplay() {
        List<Vehicle> allVehicles = new ArrayList<>();
        for (Dealership dealership : dealerships.values()) {
            allVehicles.addAll(dealership.getVehicles());
        }
        return allVehicles;
    }

    /**
     * Clears all inventory data from memory and the inventory file
     * @param inventoryFile The inventory file to clear
     * @return true if successful, false otherwise
     */
    public boolean clearAllInventory(File inventoryFile) {
        try {
            // Clear all dealerships from memory
            dealerships.clear();
            // Create an empty inventory structure that matches our JSON format
            List<Vehicle> emptyVehicleList = new ArrayList<>();
            Map<String, Boolean> emptyAcquisitionStates = new HashMap<>();
            // Use the existing jsonFileHandler to write the empty structure
            jsonFileHandler.writeInventory(emptyVehicleList, emptyAcquisitionStates, inventoryFile);
            System.out.println("Inventory successfully cleared at " + new Date());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to clear inventory: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates a backup of the current inventory
     * @param backupFile The file to save the backup to
     * @return true if successful, false otherwise
     */
    public boolean exportInventoryBackup(File backupFile) {
        try {
            List<Vehicle> allVehicles = getVehiclesForDisplay();
            Map<String, Boolean> acquisitionStates = getAcquisitionStates();
            // Write current inventory to backup file
            jsonFileHandler.writeInventory(allVehicles, acquisitionStates, backupFile);
            System.out.println("Backup created successfully with " + allVehicles.size() + " vehicles at " + new Date());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to create backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

package org.example;

import com.fasterxml.jackson.databind.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class JSONFileHandler {
    private final ObjectMapper objectMapper;

    public JSONFileHandler() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Configure date handling
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    }

    /**
     * Reads vehicle inventory from a JSON file and returns a list of Vehicle objects
     * @param file The file to read from
     * @return List of vehicles in the inventory
     */
    public List<Vehicle> readInventory(File file) {
        try {
            if (!file.exists()) {
                return new ArrayList<>();
            }
            JsonNode rootNode = objectMapper.readTree(file);
            JsonNode inventory = rootNode.get("car_inventory");
            if (inventory == null) return Collections.emptyList();

            List<Vehicle> vehicles = new ArrayList<>();
            for (JsonNode node : inventory) {
                Vehicle vehicle = createVehicleFromNode(node);
                if (vehicle != null) {
                    vehicles.add(vehicle);
                }
            }
            return vehicles;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Reads acquisition states from the JSON file
     * @param file The file to read from
     * @return Map of dealer IDs to acquisition states
     */
    public Map<String, Boolean> readAcquisitionStates(File file) {
        Map<String, Boolean> acquisitionStates = new HashMap<>();
        try {
            if (!file.exists()) {
                return acquisitionStates;
            }

            JsonNode rootNode = objectMapper.readTree(file);
            JsonNode statesNode = rootNode.get("acquisition_states");

            if (statesNode != null && statesNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = statesNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    acquisitionStates.put(entry.getKey(), entry.getValue().asBoolean());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return acquisitionStates;
    }

    /**
     * Creates a vehicle from a JSON node with comprehensive property mapping
     */
    private Vehicle createVehicleFromNode(JsonNode node) {
        try {
            // Determine vehicle type
            Vehicle vehicle = determineVehicleType(node);
            if (vehicle == null) {
                System.err.println("Could not create vehicle for node: " + node);
                return null;
            }

            // Set core properties
            vehicle.setVehicleId(node.has("vehicle_id") ?
                    node.get("vehicle_id").asText() :
                    generateRandomId());

            if (node.has("vehicle_manufacturer")) {
                vehicle.setManufacturer(node.get("vehicle_manufacturer").asText());
            }

            if (node.has("vehicle_model")) {
                vehicle.setModel(node.get("vehicle_model").asText());
            }

            if (node.has("price")) {
                vehicle.setPrice(node.get("price").asDouble());
            }

            if (node.has("dealership_id")) {
                vehicle.setDealerId(node.get("dealership_id").asText());
            }

            if (node.has("acquisition_date")) {
                vehicle.setAcquisitionDate(new Date(node.get("acquisition_date").asLong()));
            }

            // Rental status
            if (node.has("is_rented")) {
                vehicle.setRented(node.get("is_rented").asBoolean());
            }

            if (vehicle.isRented()) {
                if (node.has("rental_start_date")) {
                    vehicle.setRentalStartDate(new Date(node.get("rental_start_date").asLong()));
                }
                if (node.has("rental_end_date")) {
                    vehicle.setRentalEndDate(new Date(node.get("rental_end_date").asLong()));
                }
            }

            // Preserve any additional metadata
            if (node.has("metadata")) {
                JsonNode metadataNode = node.get("metadata");
                for (Iterator<String> it = metadataNode.fieldNames(); it.hasNext(); ) {
                    String key = it.next();
                    vehicle.getMetadata().put(key, metadataNode.get(key).asText());
                }
            }

            return vehicle;
        } catch (Exception e) {
            System.err.println("Error creating vehicle from node: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Determines the vehicle type from the JSON node
     */
    private Vehicle determineVehicleType(JsonNode node) {
        try {
            // Prioritize explicit vehicle type
            if (node.has("vehicle_type")) {
                String explicitType = node.get("vehicle_type").asText().toLowerCase();
                switch (explicitType) {
                    case "suv": return new SUV();
                    case "sedan": return new Sedan();
                    case "pickup": return new Pickup();
                    case "sports car": return new SportsCar();
                }
            }

            // Fallback to model-based inference if possible
            if (node.has("vehicle_model")) {
                String model = node.get("vehicle_model").asText().toLowerCase();
                if (model.contains("silverado") || model.contains("f-150")) return new Pickup();
                if (model.contains("cr-v") || model.contains("explorer")) return new SUV();
                if (model.contains("model 3") || model.contains("accord")) return new Sedan();
                if (model.contains("supra")) return new SportsCar();
            }

            // Last resort default
            return new SUV();
        } catch (Exception e) {
            System.err.println("Error determining vehicle type: " + e.getMessage());
            return new SUV();
        }
    }

    /**
     * Generates a random ID for vehicles
     */
    private String generateRandomId() {
        return "GEN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    /**
     * Writes the inventory to a JSON file with acquisition states
     */
    public void writeInventory(List<Vehicle> vehicles, Map<String, Boolean> acquisitionStates, File file) {
        try {
            Map<String, Object> wrapper = new HashMap<>();
            List<Map<String, Object>> inventoryList = new ArrayList<>();

            for (Vehicle vehicle : vehicles) {
                Map<String, Object> vehicleData = new HashMap<>();

                // Core vehicle properties
                vehicleData.put("vehicle_id", vehicle.getVehicleId());
                vehicleData.put("vehicle_manufacturer", vehicle.getManufacturer());
                vehicleData.put("vehicle_model", vehicle.getModel());
                vehicleData.put("acquisition_date", vehicle.getAcquisitionDate().getTime());
                vehicleData.put("price", vehicle.getPrice());
                vehicleData.put("dealership_id", vehicle.getDealerId());

                // Vehicle type
                vehicleData.put("vehicle_type", vehicle.getClass().getSimpleName().toLowerCase());

                // Rental status
                vehicleData.put("is_rented", vehicle.isRented());
                if (vehicle.isRented()) {
                    if (vehicle.getRentalStartDate() != null) {
                        vehicleData.put("rental_start_date", vehicle.getRentalStartDate().getTime());
                    }
                    if (vehicle.getRentalEndDate() != null) {
                        vehicleData.put("rental_end_date", vehicle.getRentalEndDate().getTime());
                    }
                }

                // Metadata
                if (!vehicle.getMetadata().isEmpty()) {
                    vehicleData.put("metadata", vehicle.getMetadata());
                }

                inventoryList.add(vehicleData);
            }

            wrapper.put("car_inventory", inventoryList);
            wrapper.put("acquisition_states", acquisitionStates);
            objectMapper.writeValue(file, wrapper);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Legacy method to maintain backward compatibility
     */
    public void writeInventory(List<Vehicle> vehicles, File file) {
        Map<String, Boolean> emptyAcquisitionStates = new HashMap<>();
        writeInventory(vehicles, emptyAcquisitionStates, file);
    }
}
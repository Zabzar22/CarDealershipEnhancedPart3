package org.example;

import com.fasterxml.jackson.annotation.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "vehicle_type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SUV.class, name = "suv"),
        @JsonSubTypes.Type(value = Sedan.class, name = "sedan"),
        @JsonSubTypes.Type(value = Pickup.class, name = "pickup"),
        @JsonSubTypes.Type(value = SportsCar.class, name = "sports car")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Vehicle {
    @JsonProperty("vehicle_id")
    private String vehicleId;
    @JsonProperty("vehicle_manufacturer")
    private String manufacturer;
    @JsonProperty("vehicle_model")
    private String model;
    @JsonProperty("acquisition_date")
    private Date acquisitionDate;
    @JsonProperty("price")
    private double price;
    @JsonProperty("dealership_id")
    private String dealerId;
    @JsonProperty("is_rented")
    private boolean isRented = false;
    @JsonProperty("rental_start_date")
    private Date rentalStartDate;
    @JsonProperty("rental_end_date")
    private Date rentalEndDate;

    @JsonIgnore
    private Map<String, Object> metadata = new HashMap<>();

    @JsonAnySetter
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Date getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(Date acquisitionDate) { this.acquisitionDate = acquisitionDate; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDealerId() { return dealerId; }
    public void setDealerId(String dealerId) { this.dealerId = dealerId; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public boolean isRented() { return isRented; }
    public void setRented(boolean rented) { this.isRented = rented; }

    public Date getRentalStartDate() { return rentalStartDate; }
    public void setRentalStartDate(Date rentalStartDate) { this.rentalStartDate = rentalStartDate; }

    public Date getRentalEndDate() { return rentalEndDate; }
    public void setRentalEndDate(Date rentalEndDate) { this.rentalEndDate = rentalEndDate; }

    /**
     * Determines if the vehicle is available for rent.
     * @return true if the vehicle can be rented, false otherwise
     */
    public boolean isRentable() {
        return true; // Default implementation allows renting
    }

    /**
     * Returns a display status for the vehicle.
     * @return Status string for display purposes
     */
    public String getStatusDisplay() {
        if (!isRentable()) {
            return "SALE ONLY";
        } else if (isRented()) {
            return "RENTED";
        } else {
            return "AVAILABLE";
        }
    }

    public boolean isAvailableForRent() {
        return isRentable() && !isRented;
    }

    public boolean rent(Date startDate, Date endDate) {
        if (!isRentable() || isRented) return false;
        this.isRented = true;
        this.rentalStartDate = startDate;
        this.rentalEndDate = endDate;
        return true;
    }

    public boolean returnVehicle() {
        if (!isRented) return false;
        this.isRented = false;
        this.rentalStartDate = null;
        this.rentalEndDate = null;
        return true;
    }
}
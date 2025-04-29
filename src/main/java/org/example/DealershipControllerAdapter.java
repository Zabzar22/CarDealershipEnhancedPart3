package org.example;

import javafx.collections.ObservableList;
import org.example.controller.DealershipController;

/**
 * Simple adapter to help Kotlin access the Java controller
 */
public class DealershipControllerAdapter {
    private final DealershipController controller;

    public DealershipControllerAdapter(DealershipController controller) {
        this.controller = controller;
    }

    public ObservableList getDealerList() {
        return controller.getDealerList();
    }

    public boolean addVehicle(Vehicle vehicle, String dealerId) {
        // This method is called from the Kotlin wizard, so we should NOT show any error dialogs here
        // since the controller will already show them
        return controller.addVehicle(vehicle, dealerId);
    }

    // Add a method to check if acquisition is enabled for a dealer
    public boolean isAcquisitionEnabled(String dealerId) {
        return controller.getDealershipManager().isAcquisitionEnabled(dealerId);
    }
}

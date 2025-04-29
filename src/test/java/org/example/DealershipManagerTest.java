import org.example.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DealershipManagerTest {
    private DealershipManager manager;
    private File dummyFile;

    @BeforeEach
    void setUp() {
        manager = new DealershipManager();
        dummyFile = new File("test_inventory.json");
        dummyFile.delete(); // Clean up before each test
    }

    private Vehicle createTestVehicle(String id, String dealerId) {
        Vehicle vehicle = new SUV();
        vehicle.setVehicleId(id);
        vehicle.setDealerId(dealerId);
        vehicle.setManufacturer("Toyota");
        vehicle.setModel("RAV4");
        vehicle.setPrice(25000);
        vehicle.setAcquisitionDate(new Date());
        return vehicle;
    }

    @Test
    void testAddVehicleToInventory() {
        Vehicle vehicle = createTestVehicle("V1", "D1");
        assertTrue(manager.addVehicleToInventory(vehicle, dummyFile));
        assertFalse(manager.addVehicleToInventory(vehicle, dummyFile)); // duplicate
    }

    @Test
    void testRemoveVehicleFromInventory() {
        Vehicle vehicle = createTestVehicle("V2", "D2");
        manager.addVehicleToInventory(vehicle, dummyFile);

        assertTrue(manager.removeVehicleFromInventory(
                "D2", "V2", "Toyota", "RAV4", 25000, dummyFile));
        assertFalse(manager.removeVehicleFromInventory(
                "D2", "V2", "Toyota", "RAV4", 25000, dummyFile)); // already removed
    }

    @Test
    void testEnableDisableAcquisition() {
        manager.enableAcquisition("D3");
        assertTrue(manager.isAcquisitionEnabled("D3"));
        manager.disableAcquisition("D3");
        assertFalse(manager.isAcquisitionEnabled("D3"));
    }

    @Test
    void testClearAllInventory() {
        Vehicle vehicle = createTestVehicle("V3", "D4");
        manager.addVehicleToInventory(vehicle, dummyFile);
        assertTrue(manager.clearAllInventory(dummyFile));
        assertEquals(0, manager.getVehiclesForDisplay().size());
    }
}

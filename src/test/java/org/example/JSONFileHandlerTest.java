import org.example.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JSONFileHandlerTest {
    private JSONFileHandler handler;
    private File testFile;

    @BeforeEach
    void setUp() {
        handler = new JSONFileHandler();
        testFile = new File("test_inventory.json");
        testFile.delete();
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
    void testWriteAndReadInventory() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(createTestVehicle("V1", "D1"));

        Map<String, Boolean> acquisitionStates = new HashMap<>();
        acquisitionStates.put("D1", true);

        handler.writeInventory(vehicles, acquisitionStates, testFile);

        List<Vehicle> loaded = handler.readInventory(testFile);
        assertEquals(1, loaded.size());
        assertEquals("V1", loaded.get(0).getVehicleId());
    }

    @Test
    void testReadAcquisitionStates() {
        Map<String, Boolean> acquisitionStates = new HashMap<>();
        acquisitionStates.put("D1", true);
        handler.writeInventory(new ArrayList<>(), acquisitionStates, testFile);

        Map<String, Boolean> loadedStates = handler.readAcquisitionStates(testFile);
        assertTrue(loadedStates.containsKey("D1"));
        assertTrue(loadedStates.get("D1"));
    }
}

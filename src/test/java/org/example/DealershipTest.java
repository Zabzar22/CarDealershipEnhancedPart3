import org.example.*;
import org.junit.jupiter.api.*;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DealershipTest {
    private Dealership dealership;

    @BeforeEach
    void setUp() {
        dealership = new Dealership("D1", "Test Dealer");
    }

    private Vehicle createTestVehicle(String id) {
        Vehicle v = new SUV();
        v.setVehicleId(id);
        v.setManufacturer("Toyota");
        v.setModel("RAV4");
        v.setPrice(25000);
        v.setDealerId("D1");
        v.setAcquisitionDate(new Date());
        return v;
    }

    @Test
    void testAddVehicle() {
        Vehicle v1 = createTestVehicle("V1");
        assertTrue(dealership.addVehicle(v1));
        assertFalse(dealership.addVehicle(v1)); // duplicate
    }

    @Test
    void testTransferVehicle() {
        Vehicle v2 = createTestVehicle("V2");
        dealership.addVehicle(v2);

        Dealership target = new Dealership("D2", "Target Dealer");
        assertTrue(dealership.transferVehicle("V2", target));
        assertNull(dealership.findVehicleById("V2"));
        assertNotNull(target.findVehicleById("V2"));
    }

    @Test
    void testRentAndReturnVehicle() {
        Vehicle v3 = createTestVehicle("V3");
        dealership.addVehicle(v3);
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        Date tomorrow = cal.getTime();

        assertTrue(dealership.rentVehicle("V3", now, tomorrow));
        assertFalse(dealership.rentVehicle("V3", now, tomorrow)); // already rented
        assertTrue(dealership.returnVehicle("V3"));
        assertFalse(dealership.returnVehicle("V3")); // already returned
    }
}

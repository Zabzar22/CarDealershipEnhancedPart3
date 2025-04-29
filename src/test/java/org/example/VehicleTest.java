import org.example.*;
import org.junit.jupiter.api.*;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {
    private Vehicle createTestVehicle(Vehicle v) {
        v.setVehicleId("VTest");
        v.setDealerId("DTest");
        v.setManufacturer("Test");
        v.setModel("TestModel");
        v.setPrice(10000);
        v.setAcquisitionDate(new Date());
        return v;
    }

    @Test
    void testRentableStatus() {
        Vehicle suv = createTestVehicle(new SUV());
        Vehicle sportsCar = createTestVehicle(new SportsCar());
        assertTrue(suv.isRentable());
        assertFalse(sportsCar.isRentable());
    }

    @Test
    void testRentAndReturn() {
        Vehicle sedan = createTestVehicle(new Sedan());
        Date start = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 2);
        Date end = cal.getTime();

        assertTrue(sedan.rent(start, end));
        assertFalse(sedan.rent(start, end)); // already rented
        assertTrue(sedan.returnVehicle());
        assertFalse(sedan.returnVehicle()); // already returned
    }

    @Test
    void testStatusDisplay() {
        Vehicle pickup = createTestVehicle(new Pickup());
        assertEquals("AVAILABLE", pickup.getStatusDisplay());
        pickup.rent(new Date(), new Date(System.currentTimeMillis() + 100000));
        assertEquals("RENTED", pickup.getStatusDisplay());
        pickup.returnVehicle();
        assertEquals("AVAILABLE", pickup.getStatusDisplay());
    }
}

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.example.SUV
import org.example.Sedan
import org.example.DashboardView

class DashboardViewTest {

    @Test
    fun testVehicleTypeCount() {
        val vehicles = listOf(
                SUV().apply {
                    vehicleId = "1"
                    dealerId = "A"
                    manufacturer = "Toyota"
                    model = "RAV4"
                    price = 20000.0
                    acquisitionDate = java.util.Date()
                },
                Sedan().apply {
                    vehicleId = "2"
                    dealerId = "A"
                    manufacturer = "Honda"
                    model = "Accord"
                    price = 18000.0
                    acquisitionDate = java.util.Date()
                },
                Sedan().apply {
                    vehicleId = "3"
                    dealerId = "B"
                    manufacturer = "Honda"
                    model = "Civic"
                    price = 17000.0
                    acquisitionDate = java.util.Date()
                }
        )
        val typeCounts = DashboardView.countVehiclesByType(vehicles)
        assertEquals(1, typeCounts["SUV"])
        assertEquals(2, typeCounts["Sedan"])
    }

    @Test
    fun testVehicleTypeCountEmptyList() {
        val vehicles = emptyList<org.example.Vehicle>()
        val typeCounts = DashboardView.countVehiclesByType(vehicles)
        assertTrue(typeCounts.isEmpty())
    }

    @Test
    fun testVehicleTypeCountAllSameType() {
        val vehicles = List(3) {
            SUV().apply {
                vehicleId = (it + 1).toString()
                dealerId = "A"
                manufacturer = "Toyota"
                model = "RAV4"
                price = 20000.0 + it
                acquisitionDate = java.util.Date()
            }
        }
        val typeCounts = DashboardView.countVehiclesByType(vehicles)
        assertEquals(3, typeCounts["SUV"])
        assertNull(typeCounts["Sedan"])
    }

    @Test
    fun testVehicleTypeCountMixedCase() {
        val vehicles = listOf(
                SUV().apply { vehicleId = "1"; dealerId = "A"; manufacturer = "Toyota"; model = "RAV4"; price = 20000.0; acquisitionDate = java.util.Date() },
                SUV().apply { vehicleId = "2"; dealerId = "A"; manufacturer = "Toyota"; model = "Highlander"; price = 25000.0; acquisitionDate = java.util.Date() }
        )
        val typeCounts = DashboardView.countVehiclesByType(vehicles)
        assertEquals(2, typeCounts["SUV"])
        assertNull(typeCounts["Sedan"])
    }
}

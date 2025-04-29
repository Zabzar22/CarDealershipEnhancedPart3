import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.example.VehicleAddWizard

class VehicleAddWizardTest {

    @Test
    fun testPriceValidation() {
        assertTrue(VehicleAddWizard.isValidPrice("25000"))
        assertFalse(VehicleAddWizard.isValidPrice("-100"))
        assertFalse(VehicleAddWizard.isValidPrice("abc"))
    }

    @Test
    fun testPriceValidationEmptyAndNull() {
        assertFalse(VehicleAddWizard.isValidPrice(""))
        assertFalse(VehicleAddWizard.isValidPrice(null))
    }

    @Test
    fun testPriceValidationZero() {
        assertTrue(VehicleAddWizard.isValidPrice("0"))
    }

    @Test
    fun testPriceValidationWhitespace() {
        assertFalse(VehicleAddWizard.isValidPrice("   "))
    }

    @Test
    fun testPriceValidationDecimal() {
        assertTrue(VehicleAddWizard.isValidPrice("1234.56"))
        assertFalse(VehicleAddWizard.isValidPrice("-0.01"))
    }
}

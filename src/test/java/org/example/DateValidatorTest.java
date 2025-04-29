import org.example.util.DateValidator;
import org.junit.jupiter.api.*;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateValidatorTest {
    @Test
    void testValidDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) + 1;
        String futureDate = "06/15/" + year;
        assertTrue(DateValidator.isValidDate(futureDate));
        assertFalse(DateValidator.isValidDate("13/01/2025")); // invalid month
        assertFalse(DateValidator.isValidDate("")); // empty
        assertFalse(DateValidator.isValidDate(null));
    }

    @Test
    void testValidDateRange() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) + 1;
        String start = "06/01/" + year;
        String end = "06/10/" + year;
        assertTrue(DateValidator.isValidDateRange(start, end));
        assertFalse(DateValidator.isValidDateRange(end, start)); // end before start
        assertFalse(DateValidator.isValidDateRange("invalid", end));
    }

    @Test
    void testFormatDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.APRIL, 28);
        Date date = cal.getTime();
        assertEquals("04/28/2025", DateValidator.formatDate(date));
        assertEquals("", DateValidator.formatDate(null));
    }
}

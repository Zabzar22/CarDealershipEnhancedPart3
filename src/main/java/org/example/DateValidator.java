package org.example.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for validating dates in the application
 */
public class DateValidator {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    static {
        DATE_FORMAT.setLenient(false);
    }

    /**
     * Validates if a date string is in the correct format and within reasonable range
     * @param dateStr The date string to validate (MM/dd/yyyy)
     * @return true if the date is valid, false otherwise
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }

        try {
            Date date = DATE_FORMAT.parse(dateStr);

            // Check if date is in the past
            Date currentDate = new Date();
            if (date.before(currentDate)) {
                return false;
            }

            // Check if year is reasonable
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);

            return year >= currentYear && year <= currentYear + 10;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Validates if a date range is valid (both dates valid and end date after start date)
     * @param startDateStr The start date string (MM/dd/yyyy)
     * @param endDateStr The end date string (MM/dd/yyyy)
     * @return true if the date range is valid, false otherwise
     */
    public static boolean isValidDateRange(String startDateStr, String endDateStr) {
        if (!isValidDate(startDateStr) || !isValidDate(endDateStr)) {
            return false;
        }

        try {
            Date startDate = DATE_FORMAT.parse(startDateStr);
            Date endDate = DATE_FORMAT.parse(endDateStr);

            return !endDate.before(startDate);
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Formats a date object to string in MM/dd/yyyy format
     * @param date The date to format
     * @return Formatted date string
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMAT.format(date);
    }
}

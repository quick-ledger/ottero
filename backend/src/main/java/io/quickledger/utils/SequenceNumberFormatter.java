package io.quickledger.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for generating sequence numbers with date placeholders and
 * padding.
 * Supports placeholders: {YYYY}, {YY}, {MM}, {DD}
 */
public class SequenceNumberFormatter {

    /**
     * Formats a sequence number with date placeholders and padding.
     * 
     * @param prefix  The prefix pattern (may contain date placeholders)
     * @param number  The sequential number
     * @param postfix The postfix pattern (may contain date placeholders)
     * @param padding Number of digits to pad the number (e.g., 4 = 0001)
     * @param date    The date to use for placeholder replacement
     * @return The formatted sequence number
     */
    public static String format(String prefix, Integer number, String postfix, Integer padding, LocalDate date) {
        // Apply date placeholders
        String formattedPrefix = applyDatePlaceholders(prefix != null ? prefix : "", date);
        String formattedPostfix = applyDatePlaceholders(postfix != null ? postfix : "", date);

        // Apply number padding
        String paddedNumber = padNumber(number, padding != null ? padding : 0);

        return formattedPrefix + paddedNumber + formattedPostfix;
    }

    /**
     * Replaces date placeholders in a string.
     * Supported placeholders:
     * - {YYYY} = Full year (2026)
     * - {YY} = Short year (26)
     * - {MM} = Month (01-12)
     * - {DD} = Day (01-31)
     */
    private static String applyDatePlaceholders(String pattern, LocalDate date) {
        if (pattern == null || pattern.isEmpty()) {
            return "";
        }

        String result = pattern;
        result = result.replace("{YYYY}", date.format(DateTimeFormatter.ofPattern("yyyy")));
        result = result.replace("{YY}", date.format(DateTimeFormatter.ofPattern("yy")));
        result = result.replace("{MM}", date.format(DateTimeFormatter.ofPattern("MM")));
        result = result.replace("{DD}", date.format(DateTimeFormatter.ofPattern("dd")));

        return result;
    }

    /**
     * Pads a number with leading zeros.
     * 
     * @param number  The number to pad
     * @param padding The total number of digits (0 = no padding)
     * @return The padded number as a string
     */
    private static String padNumber(Integer number, Integer padding) {
        if (number == null) {
            number = 0;
        }

        if (padding == null || padding <= 0) {
            return number.toString();
        }

        return String.format("%0" + padding + "d", number);
    }
}

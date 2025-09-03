package com.portfolio.util;

/**
 * Utility class for string operations and validation
 */
public final class StringUtils {

    private StringUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Checks if a string is null or empty
     * 
     * @param str the string to check
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Checks if a string is null, empty, or contains only whitespace
     * 
     * @param str the string to check
     * @return true if the string is null, empty, or contains only whitespace, false otherwise
     */
    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Checks if a string has content (not null, not empty)
     * 
     * @param str the string to check
     * @return true if the string has content, false otherwise
     */
    public static boolean hasContent(String str) {
        return !isNullOrEmpty(str);
    }

    /**
     * Checks if a string has meaningful content (not null, not empty, not just whitespace)
     * 
     * @param str the string to check
     * @return true if the string has meaningful content, false otherwise
     */
    public static boolean hasMeaningfulContent(String str) {
        return !isNullOrBlank(str);
    }

    /**
     * Returns the string if it has content, otherwise returns the default value
     * 
     * @param str the string to check
     * @param defaultValue the default value to return if str is null or empty
     * @return the original string if it has content, otherwise the default value
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return hasContent(str) ? str : defaultValue;
    }

    /**
     * Returns the string if it has meaningful content, otherwise returns the default value
     * 
     * @param str the string to check
     * @param defaultValue the default value to return if str is null, empty, or blank
     * @return the original string if it has meaningful content, otherwise the default value
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return hasMeaningfulContent(str) ? str : defaultValue;
    }
}

package com.portfolio.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.EmptySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for StringUtils utility methods
 */
class StringUtilsTest {

    @Test
    void testIsNullOrEmpty_withNull() {
        assertTrue(StringUtils.isNullOrEmpty(null));
    }

    @Test
    void testIsNullOrEmpty_withEmptyString() {
        assertTrue(StringUtils.isNullOrEmpty(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "  ", "\t", "\n", "hello", "world"})
    void testIsNullOrEmpty_withContent(String input) {
        assertFalse(StringUtils.isNullOrEmpty(input));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n", "   \t\n   "})
    void testIsNullOrBlank_withNullEmptyOrBlank(String input) {
        assertTrue(StringUtils.isNullOrBlank(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "world", " hello ", "  test  "})
    void testIsNullOrBlank_withContent(String input) {
        assertFalse(StringUtils.isNullOrBlank(input));
    }

    @Test
    void testHasContent_withNull() {
        assertFalse(StringUtils.hasContent(null));
    }

    @Test
    void testHasContent_withEmptyString() {
        assertFalse(StringUtils.hasContent(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "  ", "\t", "\n", "hello", "world"})
    void testHasContent_withContent(String input) {
        assertTrue(StringUtils.hasContent(input));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n", "   \t\n   "})
    void testHasMeaningfulContent_withNullEmptyOrBlank(String input) {
        assertFalse(StringUtils.hasMeaningfulContent(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "world", " hello ", "  test  "})
    void testHasMeaningfulContent_withContent(String input) {
        assertTrue(StringUtils.hasMeaningfulContent(input));
    }

    @Test
    void testDefaultIfEmpty_withNull() {
        assertEquals("default", StringUtils.defaultIfEmpty(null, "default"));
    }

    @Test
    void testDefaultIfEmpty_withEmptyString() {
        assertEquals("default", StringUtils.defaultIfEmpty("", "default"));
    }

    @Test
    void testDefaultIfEmpty_withContent() {
        assertEquals("hello", StringUtils.defaultIfEmpty("hello", "default"));
        assertEquals(" ", StringUtils.defaultIfEmpty(" ", "default"));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n", "   \t\n   "})
    void testDefaultIfBlank_withNullEmptyOrBlank(String input) {
        assertEquals("default", StringUtils.defaultIfBlank(input, "default"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "world", " hello ", "  test  "})
    void testDefaultIfBlank_withContent(String input) {
        assertEquals(input, StringUtils.defaultIfBlank(input, "default"));
    }

    @Test
    void testDefaultIfEmpty_withNullDefault() {
        assertNull(StringUtils.defaultIfEmpty(null, null));
        assertNull(StringUtils.defaultIfEmpty("", null));
        assertEquals("hello", StringUtils.defaultIfEmpty("hello", null));
    }

    @Test
    void testDefaultIfBlank_withNullDefault() {
        assertNull(StringUtils.defaultIfBlank(null, null));
        assertNull(StringUtils.defaultIfBlank("", null));
        assertNull(StringUtils.defaultIfBlank(" ", null));
        assertEquals("hello", StringUtils.defaultIfBlank("hello", null));
    }
}

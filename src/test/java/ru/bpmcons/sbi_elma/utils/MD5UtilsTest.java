package ru.bpmcons.sbi_elma.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MD5UtilsTest {
    @Test
    void shouldValidateHash() {
        assertTrue(MD5Utils.isValidHash("Q2hlY2sgSW50ZWdyaXR5IQ=="));

        assertFalse(MD5Utils.isValidHash("Q2hlY2sgSW50ZWdyaX34"));
        assertFalse(MD5Utils.isValidHash("Q2hl"));
        assertFalse(MD5Utils.isValidHash("Q2hlY2sgSW50ZWdyaXR5IQ==Q2hlY2sgSW50ZWdyaXR5IQ=="));
        assertFalse(MD5Utils.isValidHash("test"));
        assertFalse(MD5Utils.isValidHash(""));
    }
}
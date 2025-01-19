package ru.bpmcons.sbi_elma.infra.version;

import org.junit.jupiter.api.Test;
import ru.bpmcons.sbi_elma.infra.version.exception.InvalidVersionException;

import static org.junit.jupiter.api.Assertions.*;
class VersionTest {

    @Test
    void shouldParse() {
        assertEquals(new Version(1, 1, 1), Version.parse("1.1.1"));
        assertEquals(new Version(1, 1, 0), Version.parse("1.1"));
        assertEquals(new Version(1, 1, 0), Version.parse("1.1."));
        assertEquals(new Version(1, 0, 0), Version.parse("1"));

        assertEquals(new Version(1, 1, 1), Version.parse("v1.1.1"));
        assertEquals(new Version(1, 1, 0), Version.parse("v1.1"));
        assertEquals(new Version(1, 1, 0), Version.parse("v1.1."));
        assertEquals(new Version(1, 0, 0), Version.parse("v1"));

        assertThrows(InvalidVersionException.class, () -> Version.parse("1.1.1.1"));
        assertThrows(InvalidVersionException.class, () -> Version.parse("1.1a"));
        assertThrows(InvalidVersionException.class, () -> Version.parse("a"));
    }

    @Test
    void shouldCompare() {
        assertTrue(new Version(2, 0, 0).compareTo(new Version(1, 0, 0)) > 0);
        assertTrue(new Version(1, 1, 0).compareTo(new Version(1, 0, 0)) > 0);
        assertTrue(new Version(1, 0, 1).compareTo(new Version(1, 0, 0)) > 0);
        assertEquals(0, new Version(1, 0, 0).compareTo(new Version(1, 0, 0)));
        assertTrue(new Version(0, 0, 0).compareTo(new Version(1, 0, 0)) < 0);
        assertTrue(new Version(0, 1, 0).compareTo(new Version(1, 0, 0)) < 0);
        assertTrue(new Version(0, 0, 1).compareTo(new Version(1, 0, 0)) < 0);
    }

    @Test
    void shouldBeBefore() {
        assertFalse(new Version(1, 1, 1).isBefore(new Version(1, 1, 1)));
        assertFalse(new Version(1, 1, 1).isBefore(new Version(1, 1, 0)));
        assertTrue(new Version(1, 1, 1).isBefore(new Version(1, 1, 2)));

        assertFalse(new Version(1, 1, 1).isBefore(new Version(1, 1, 1)));
        assertFalse(new Version(1, 1, 1).isBefore(new Version(1, 0, 1)));
        assertTrue(new Version(1, 1, 1).isBefore(new Version(1, 2, 1)));

        assertFalse(new Version(1, 1, 1).isBefore(new Version(1, 1, 1)));
        assertFalse(new Version(1, 1, 1).isBefore(new Version(0, 1, 1)));
        assertTrue(new Version(1, 1, 1).isBefore(new Version(2, 1, 1)));
    }

    @Test
    void shouldBeNotBefore() {
        assertTrue(new Version(1, 1, 1).isNotBefore(new Version(1, 1, 1)));
        assertTrue(new Version(1, 1, 1).isNotBefore(new Version(1, 1, 0)));
        assertFalse(new Version(1, 1, 1).isNotBefore(new Version(1, 1, 2)));

        assertTrue(new Version(1, 1, 1).isNotBefore(new Version(1, 1, 1)));
        assertTrue(new Version(1, 1, 1).isNotBefore(new Version(1, 0, 1)));
        assertFalse(new Version(1, 1, 1).isNotBefore(new Version(1, 2, 1)));

        assertTrue(new Version(1, 1, 1).isNotBefore(new Version(1, 1, 1)));
        assertTrue(new Version(1, 1, 1).isNotBefore(new Version(0, 1, 1)));
        assertFalse(new Version(1, 1, 1).isNotBefore(new Version(2, 1, 1)));
    }

}
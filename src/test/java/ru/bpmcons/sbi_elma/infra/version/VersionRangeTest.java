package ru.bpmcons.sbi_elma.infra.version;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionRangeTest {
    @Test
    void shouldWorkWithFullConstraints() {
        assertTrue(new VersionRange(Version.parse("1.0.0"), Version.parse("2.0.0")).contains(Version.parse("1.1")));
        assertTrue(new VersionRange(Version.parse("1.0.0"), Version.parse("2.0.0")).contains(Version.parse("1.0.0")));
        assertFalse(new VersionRange(Version.parse("1.0.0"), Version.parse("2.0.0")).contains(Version.parse("3.0.0")));
        assertFalse(new VersionRange(Version.parse("1.0.0"), Version.parse("2.0.0")).contains(Version.parse("0.5.0")));
    }

    @Test
    void shouldWorkWithOnlyUntil() {
        assertTrue(new VersionRange(null, Version.parse("2.0.0")).contains(Version.parse("1.1")));
        assertTrue(new VersionRange(null, Version.parse("2.0.0")).contains(Version.parse("1.0.0")));
        assertFalse(new VersionRange(null, Version.parse("2.0.0")).contains(Version.parse("3.0.0")));
        assertTrue(new VersionRange(null, Version.parse("2.0.0")).contains(Version.parse("0.5.0")));
    }

    @Test
    void shouldWorkWithOnlySince() {
        assertTrue(new VersionRange(Version.parse("1.0.0"), null).contains(Version.parse("1.1")));
        assertTrue(new VersionRange(Version.parse("1.0.0"), null).contains(Version.parse("1.0.0")));
        assertTrue(new VersionRange(Version.parse("1.0.0"), null).contains(Version.parse("3.0.0")));
        assertFalse(new VersionRange(Version.parse("1.0.0"), null).contains(Version.parse("0.5.0")));
    }

    @Test
    void shouldWorkWithoutConstraints() {
        assertTrue(new VersionRange(null, null).contains(Version.parse("1.1")));
        assertTrue(new VersionRange(null, null).contains(Version.parse("1.0.0")));
        assertTrue(new VersionRange(null, null).contains(Version.parse("3.0.0")));
        assertTrue(new VersionRange(null, null).contains(Version.parse("0.5.0")));
    }

    @Test
    void shouldContainsNullOnlyWithNoConstraints() {
        assertTrue(new VersionRange(null, null).contains(null));
        assertFalse(new VersionRange(Version.parse("1.0.0"), null).contains(null));
        assertFalse(new VersionRange(null, Version.parse("2.0.0")).contains(null));
        assertFalse(new VersionRange(Version.parse("1.0.0"), Version.parse("2.0.0")).contains(null));
    }

    @Test
    void shouldCompare() {
        assertTrue(new VersionRange(Version.parse("1.0.0"), null).compareTo(new VersionRange(Version.parse("2.0.0"), null)) < 0);
        assertTrue(new VersionRange(Version.parse("2.0.0"), null).compareTo(new VersionRange(Version.parse("1.0.0"), null)) > 0);
        assertEquals(0, new VersionRange(Version.parse("1.0.0"), null).compareTo(new VersionRange(Version.parse("1.0.0"), null)));

        assertTrue(new VersionRange(null, null).compareTo(new VersionRange(Version.parse("2.0.0"), null)) < 0);
        assertTrue(new VersionRange(Version.parse("2.0.0"), null).compareTo(new VersionRange(null, null)) > 0);
        assertEquals(0, new VersionRange(null, null).compareTo(new VersionRange(null, null)));
    }
}
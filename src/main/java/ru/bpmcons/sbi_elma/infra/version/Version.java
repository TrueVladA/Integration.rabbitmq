package ru.bpmcons.sbi_elma.infra.version;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.bpmcons.sbi_elma.infra.version.exception.InvalidVersionException;

/**
 * SemVer версия
 */
@Data
@RequiredArgsConstructor
public class Version implements Comparable<Version> {
    public static Version EMPTY = new Version(0, 0, 0);

    private final int major;
    private final int minor;
    private final int patch;

    public static Version parse(String str) {
        if (str.startsWith("v")) {
            str = str.substring(1);
        }
        String[] parts = str.split("\\.");
        try {
            if (parts.length == 1) {
                return new Version(Integer.parseInt(parts[0]), 0, 0);
            } else if (parts.length == 2) {
                return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 0);
            } else if (parts.length == 3) {
                return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            } else {
                throw new InvalidVersionException("слишком много частей версии (" + parts.length + "), должно быть 3 (формат - major.minor.patch)");
            }
        } catch (NumberFormatException e) {
            throw new InvalidVersionException(e.getMessage());
        }
    }

    public boolean isBefore(Version version) {
        if (major != version.getMajor()) {
            return major < version.getMajor();
        } else if (minor != version.minor) {
            return minor < version.getMinor();
        } else {
            return patch < version.getPatch();
        }
    }

    public boolean isNotBefore(Version version) {
        return !isBefore(version);
    }

    @Override
    public int compareTo(Version o) {
        int cmp = Integer.compare(major, o.major);
        if (cmp == 0) {
            cmp = Integer.compare(minor, o.minor);
        }
        if (cmp == 0) {
            cmp = Integer.compare(patch, o.patch);
        }
        return cmp;
    }
}

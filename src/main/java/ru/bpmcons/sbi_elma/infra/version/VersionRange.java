package ru.bpmcons.sbi_elma.infra.version;

import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * Промежуток версий <code>(since <= $ver < until)</code>
 */
@Data
public class VersionRange implements Comparable<VersionRange> {
    private final Version since;
    private final Version until;

    public boolean contains(@Nullable Version version) {
        if (version == null) {
            return since == null && until == null;
        }
        if (since != null && version.compareTo(since) < 0) {
            return false;
        }
        return until == null || version.compareTo(until) < 0;
    }

    @Override
    public int compareTo(VersionRange o) {
        if (since == null) {
            if (o.since == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (o.since == null) {
                return 1;
            } else {
                return since.compareTo(o.since);
            }
        }
    }
}

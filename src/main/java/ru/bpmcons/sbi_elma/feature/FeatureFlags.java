package ru.bpmcons.sbi_elma.feature;

import ru.bpmcons.sbi_elma.feature.exception.UnknownFeatureFlagException;

public enum FeatureFlags {
    ;

    public static FeatureFlags parse(String name) {
        name = name.trim();
        for (FeatureFlags value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new UnknownFeatureFlagException(name);
    }
}

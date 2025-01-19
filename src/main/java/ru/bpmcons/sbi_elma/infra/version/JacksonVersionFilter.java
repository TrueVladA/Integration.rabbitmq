package ru.bpmcons.sbi_elma.infra.version;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

@RequiredArgsConstructor
public class JacksonVersionFilter extends JacksonAnnotationIntrospector {
    @NonNull
    private final Version currentVersion;

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        Since since = m.getAnnotation(Since.class);
        Until until = m.getAnnotation(Until.class);
        VersionRange versionRange = new VersionRange(
                since == null ? null : new Version(since.major(), since.minor(), since.patch()),
                until == null ? null : new Version(until.major(), until.minor(), until.patch())
        );
        if (!versionRange.contains(currentVersion)) {
            return true;
        }
        return super.hasIgnoreMarker(m);
    }
}

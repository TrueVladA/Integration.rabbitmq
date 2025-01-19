package ru.bpmcons.sbi_elma.s3;

import java.util.Map;

public class EmptyS3Metadata implements S3Metadata {
    @Override
    public Map<String, String> toAttributes() {
        return Map.of();
    }
}

package ru.bpmcons.sbi_elma.s3;

import java.util.Map;

public interface S3Metadata {
    Map<String, String> toAttributes();
}

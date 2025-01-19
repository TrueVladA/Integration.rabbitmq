package ru.bpmcons.sbi_elma.s3.dto;

import lombok.Data;

@Data
public class PresignResponse {
    private Bucket bucket;
    private String file;
    private String presignUrl;
    private Method method;
}

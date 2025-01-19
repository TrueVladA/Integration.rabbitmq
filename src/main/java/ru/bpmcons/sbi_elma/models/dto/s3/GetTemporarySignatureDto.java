package ru.bpmcons.sbi_elma.models.dto.s3;

import lombok.Data;

@Data
public class GetTemporarySignatureDto {
    private String[] filename;
    private String method;
    private boolean disableBucketCheck;
}

package ru.bpmcons.sbi_elma.models.dto.s3;

import lombok.Data;

@Data
public class ResponseError {
    private String code;
    private String description;
    private String message;
}

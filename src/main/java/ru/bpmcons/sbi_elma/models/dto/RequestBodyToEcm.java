package ru.bpmcons.sbi_elma.models.dto;

import lombok.Data;

@Data
public class RequestBodyToEcm {
    private boolean active;
    private int size;
    private int from;
}

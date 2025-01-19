package ru.bpmcons.sbi_elma.models.dto;

import lombok.Data;

@Data
public class ResponseHeaderFromEcm {
    private boolean success;
    private String error;
}

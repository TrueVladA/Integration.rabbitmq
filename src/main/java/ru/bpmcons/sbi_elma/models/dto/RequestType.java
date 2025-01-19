package ru.bpmcons.sbi_elma.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class RequestType {
    private String request;
    private String code;
    private String type;
}

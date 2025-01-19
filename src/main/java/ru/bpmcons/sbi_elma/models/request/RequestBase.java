package ru.bpmcons.sbi_elma.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public abstract class RequestBase {
    @NotBlank
    @JsonProperty("rquid")
    private String requestId;
}

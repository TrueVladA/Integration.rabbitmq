package ru.bpmcons.sbi_elma.elma.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ElmaContextRequest<T> {
    @JsonProperty("context")
    private T context;
}

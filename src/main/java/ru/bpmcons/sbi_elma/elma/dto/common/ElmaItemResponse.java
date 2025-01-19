package ru.bpmcons.sbi_elma.elma.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class ElmaItemResponse<T> {
    @JsonProperty("error")
    private final String error;
    @JsonProperty("success")
    private final boolean success;
    @JsonProperty("item")
    private final T item;
}

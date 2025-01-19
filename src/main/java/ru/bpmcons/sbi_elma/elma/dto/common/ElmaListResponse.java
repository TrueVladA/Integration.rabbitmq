package ru.bpmcons.sbi_elma.elma.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class ElmaListResponse<T> {
    @JsonProperty("error")
    private final String error;
    @JsonProperty("success")
    private final boolean success;
    @JsonProperty("result")
    private final Result<T> result;

    @Data
    @Builder
    @Jacksonized
    public static class Result<T> {
        @JsonProperty("total")
        private final int total;
        @JsonProperty("result")
        private final List<T> result;
    }
}

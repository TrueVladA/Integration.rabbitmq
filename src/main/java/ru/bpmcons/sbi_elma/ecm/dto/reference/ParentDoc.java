package ru.bpmcons.sbi_elma.ecm.dto.reference;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class ParentDoc {
    @JsonProperty("id")
    private String id;
    @JsonProperty("code")
    private String code;
    @JsonProperty("namespace")
    private String namespace;
}

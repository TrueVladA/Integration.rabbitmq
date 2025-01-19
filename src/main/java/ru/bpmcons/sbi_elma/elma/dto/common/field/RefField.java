package ru.bpmcons.sbi_elma.elma.dto.common.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RefField extends Field {
    @JsonProperty("field")
    private final String field;
}

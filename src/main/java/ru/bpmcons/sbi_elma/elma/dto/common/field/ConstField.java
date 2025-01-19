package ru.bpmcons.sbi_elma.elma.dto.common.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ConstField extends Field {
    @JsonProperty("const")
    private final String c;
}

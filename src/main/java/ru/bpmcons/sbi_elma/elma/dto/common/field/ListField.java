package ru.bpmcons.sbi_elma.elma.dto.common.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ListField extends Field {
    @JsonProperty("list")
    private final List<String> list;
}

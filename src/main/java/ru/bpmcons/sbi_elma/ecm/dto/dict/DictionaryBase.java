package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class DictionaryBase {
    @JsonProperty("__id")
    private String id;

    @JsonProperty("__name")
    private String ecmName;
}

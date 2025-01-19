package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@DictionaryEntity(name = "${sys_names.currency}", displayName = "Валюты")
@EqualsAndHashCode(callSuper = true)
public class Currency extends DictionaryBase {
    @JsonProperty("digital_code")
    private String digitalCode;

    @JsonProperty("symbol_code")
    private String symbolCode;
}

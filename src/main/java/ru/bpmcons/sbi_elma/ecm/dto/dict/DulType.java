package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@DictionaryEntity(name = "${sys_names.dulTypes}", displayName = "Типы ДУЛ")
@EqualsAndHashCode(callSuper = true)
public class DulType extends DictionaryBase {
    @JsonProperty("code")
    private String code;
    @JsonProperty("short_name")
    private String shortName;
}

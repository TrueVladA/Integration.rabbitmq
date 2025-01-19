package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@DictionaryEntity(name = "${sys_names.orgleg}", displayName = "Классификатор организационно-правовых форм")
@EqualsAndHashCode(callSuper = true)
public class Opf extends DictionaryBase {
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
}

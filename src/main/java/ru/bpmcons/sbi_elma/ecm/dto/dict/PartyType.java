package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@DictionaryEntity(name = "${sys_names.partyType}", displayName = "Типы участников")
@EqualsAndHashCode(callSuper = true)
public class PartyType extends DictionaryBase {
    @JsonProperty("sys_name")
    private String sysName;
    @JsonProperty("role_bus_name")
    private String busName;
}

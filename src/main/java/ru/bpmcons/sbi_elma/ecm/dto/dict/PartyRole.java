package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@DictionaryEntity(name = "${sys_names.partyRole}", displayName = "Роли участников")
@EqualsAndHashCode(callSuper = true)
public class PartyRole extends DictionaryBase {
    @JsonProperty("role_sys_name")
    private String sysName;
    @JsonProperty("role_bus_name")
    private String busName;
}

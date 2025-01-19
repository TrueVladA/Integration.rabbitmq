package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

@Data
@DictionaryEntity(name = "${sys_names.vip}", displayName = "Типы VIP")
@EqualsAndHashCode(callSuper = true)
public class Vip extends DictionaryBase {
    @JsonProperty("type_sys_name")
    private String sysName;
    @JsonProperty("type_bus_name")
    private String busName;
    @JsonProperty("source_sys")
    @SerializeAsArray
    private String source;
}

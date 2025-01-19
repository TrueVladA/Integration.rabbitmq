package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@DictionaryEntity(name = "${sys_names.docFlows?:doc_flows}", displayName = "Потоки документов")
public class DocFlow extends DictionaryBase {
    @JsonProperty("doc_flow_code")
    private String code;
}

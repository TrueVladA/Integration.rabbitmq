package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.ecm.dto.reference.EcmDocumentRef;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@DictionaryEntity(name = "${sys_names.docTypes}", displayName = "Виды документов")
@EqualsAndHashCode(callSuper = true)
public class DocType extends DictionaryBase {
    @JsonProperty("type_sys_name")
    private String sysName;
    @JsonProperty("type_bus_name")
    private String busName;
    @JsonProperty("ecm_doc")
    private EcmDocumentRef[] ecmDoc;
}

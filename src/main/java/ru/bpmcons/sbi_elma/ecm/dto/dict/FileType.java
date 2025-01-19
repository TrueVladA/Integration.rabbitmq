package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@DictionaryEntity(name = "${sys_names.fileTypes}", displayName = "Типы файлов")
@EqualsAndHashCode(callSuper = true)
public class FileType extends DictionaryBase {
    @JsonProperty("file_type_id")
    private String fileTypeId;
    @JsonProperty("file_type_name")
    private String fileTypeName;
}

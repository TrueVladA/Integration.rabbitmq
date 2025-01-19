package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@DictionaryEntity(name = "${sys_names.extension}", displayName = "Запрещенные к загрузке расширения файлов")
@EqualsAndHashCode(callSuper = true)
public class DeniedFileExtension extends DictionaryBase {
    @JsonProperty("file_extension")
    private String fileExtension;
    @JsonProperty("mime_type")
    private String mimeType;
}

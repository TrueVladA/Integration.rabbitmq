package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@DictionaryEntity(name = "${sys_names.files_statuses?:files_statuses}", displayName = "Статусы файлов")
@EqualsAndHashCode(callSuper = true)
public class FileStatus extends DictionaryBase {
    @JsonProperty("sys_name")
    private String sysName;
}

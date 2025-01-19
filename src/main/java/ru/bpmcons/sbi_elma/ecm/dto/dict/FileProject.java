package ru.bpmcons.sbi_elma.ecm.dto.dict;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.infra.dictionary.entity.DictionaryEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@DictionaryEntity(name = "${sys_names.fileProjects?:file_projects}", displayName = "Проекты файлов")
public class FileProject extends DictionaryBase {
    @JsonProperty("sys_name")
    private String sysName;
}

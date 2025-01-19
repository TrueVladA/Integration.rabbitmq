package ru.bpmcons.sbi_elma.ecm.dto.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.bpmcons.sbi_elma.ecm.dto.trait.FileMetadataTrait;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataTable;

@Data
public class FileMetadataContainer implements FileMetadataTrait {
    @JsonProperty("file_metadata")
    private FileMetadataTable fileMetadata;
}

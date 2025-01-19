package ru.bpmcons.sbi_elma.models.dto;

import lombok.Data;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataTable;

@Data
public class ContextFileMetadata {
    private FileMetadataTable file_metadata;
}

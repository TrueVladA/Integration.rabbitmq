package ru.bpmcons.sbi_elma.models.dto;

import lombok.Data;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataRow;

@Data
public class RequestDeleteFile {
    private DeleteFileContext context;
    private FileMetadataRow[] rows;
}

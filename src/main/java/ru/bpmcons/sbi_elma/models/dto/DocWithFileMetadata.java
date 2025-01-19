package ru.bpmcons.sbi_elma.models.dto;

import lombok.Data;
import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataTable;
import ru.bpmcons.sbi_elma.ecm.dto.trait.FileMetadataTrait;

@Data
public class DocWithFileMetadata implements FileMetadataTrait {
    private FileMetadataTable file_metadata;

    @Override
    public FileMetadataTable getFileMetadata() {
        return file_metadata;
    }

    @Override
    public void setFileMetadata(FileMetadataTable table) {
        this.file_metadata = table;
    }
}

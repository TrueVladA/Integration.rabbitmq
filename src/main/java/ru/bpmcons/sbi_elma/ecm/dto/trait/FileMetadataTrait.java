package ru.bpmcons.sbi_elma.ecm.dto.trait;

import ru.bpmcons.sbi_elma.ecm.dto.file.FileMetadataTable;

public interface FileMetadataTrait {
    FileMetadataTable getFileMetadata();

    void setFileMetadata(FileMetadataTable table);
}

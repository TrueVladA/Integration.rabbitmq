package ru.bpmcons.sbi_elma.models.ability;

import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;

import java.util.List;

public interface FileMetadataContainer {
    List<FileMetadata> getFileMetadata();
}

package ru.bpmcons.sbi_elma.ecm.dto.file;

import lombok.Data;

import java.util.List;

@Data
public class FileMetadataTable {
    private List<FileMetadataRow> rows;
}

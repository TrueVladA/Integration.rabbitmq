package ru.bpmcons.sbi_elma.models.dto.fileMetadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadataVersionTable {
    private List<FileMetadataVersion> rows;
}

package ru.bpmcons.sbi_elma.ecm.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataStatus {
    private FileUploadStatus code;
}

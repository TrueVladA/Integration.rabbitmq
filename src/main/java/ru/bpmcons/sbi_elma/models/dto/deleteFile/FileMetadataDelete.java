package ru.bpmcons.sbi_elma.models.dto.deleteFile;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileMetadataDelete {
    private String id_ecm_filemetadata;
    private String id_as_filemetadata;
}

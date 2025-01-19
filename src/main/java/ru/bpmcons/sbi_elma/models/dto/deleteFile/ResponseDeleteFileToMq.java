package ru.bpmcons.sbi_elma.models.dto.deleteFile;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.models.dto.responseMq.CodeMessage;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class ResponseDeleteFileToMq extends CodeMessage {
    private String rquid;
    private String id_ecm_doc;
    private String id_as_doc;
    private FileMetadataDelete[] file_metadata;
}

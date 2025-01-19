package ru.bpmcons.sbi_elma.models.dto.fileMetadata;

import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.ResponseHeaderFromEcm;

@Data
public class ResponseFromEcmCreateFileMetadata extends ResponseHeaderFromEcm {
    private RequestFileMetadataContext item;
}

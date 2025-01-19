package ru.bpmcons.sbi_elma.models.dto.fileMetadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class RequestCreateFileMetadata {
    private RequestFileMetadataContext context;
}

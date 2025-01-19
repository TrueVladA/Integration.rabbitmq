package ru.bpmcons.sbi_elma.models.dto.fileMetadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Result {
    private RequestFileMetadataContext[] result;
}

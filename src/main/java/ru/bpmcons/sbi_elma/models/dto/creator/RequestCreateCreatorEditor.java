package ru.bpmcons.sbi_elma.models.dto.creator;

import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.fileMetadata.RequestFileMetadataContext;

@Data
public class RequestCreateCreatorEditor {
    private CreateContext context;
}

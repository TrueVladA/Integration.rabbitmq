package ru.bpmcons.sbi_elma.models.dto.deleteFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.bpmcons.sbi_elma.models.dto.generralized.Creator_editor;
import ru.bpmcons.sbi_elma.models.dto.generralized.FileMetadata;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;
import ru.bpmcons.sbi_elma.models.dto.generralized.TypeDocument;
import ru.bpmcons.sbi_elma.models.dto.jwt.JwtToken;

@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
public class RequestDeleteFileFromRabbit {
    private String rquid;
    private JwtToken jwt_token;
    private String id_ecm_doc;
    private String id_as_doc;
    private String app_id;
    private PerecoderObject doc_type;
    private FileMetadata[] file_metadata;
    private Creator_editor editor;
}

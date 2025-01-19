package ru.bpmcons.sbi_elma.models.dto.responseMq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeMessage {
    private String rquid;
    private String response_code;
    private String response_message;
    private String id_ecm_doc;
    private String id_as_doc;
}

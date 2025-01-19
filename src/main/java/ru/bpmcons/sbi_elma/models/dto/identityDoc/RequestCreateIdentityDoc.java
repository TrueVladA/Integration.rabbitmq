package ru.bpmcons.sbi_elma.models.dto.identityDoc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@Data
public class RequestCreateIdentityDoc {
    private RequestIdentityDocContext context;
}

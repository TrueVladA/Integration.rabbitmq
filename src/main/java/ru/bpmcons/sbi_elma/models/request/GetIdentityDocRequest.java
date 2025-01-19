package ru.bpmcons.sbi_elma.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetIdentityDocRequest extends AuthenticatedRequestBase {
    @NotBlank
    @JsonProperty("id_ecm_doc")
    private String ecmId;
}

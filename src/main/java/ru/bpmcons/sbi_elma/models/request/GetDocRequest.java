package ru.bpmcons.sbi_elma.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.bpmcons.sbi_elma.infra.version.Since;
import ru.bpmcons.sbi_elma.models.ability.Identifiable;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;
import ru.bpmcons.sbi_elma.validation.ValidIdentifiable;

import javax.validation.Valid;
import java.util.List;

@Data
@Valid
@ValidIdentifiable
@EqualsAndHashCode(callSuper = true)
public class GetDocRequest extends AuthenticatedRequestBase implements Identifiable {
    @JsonProperty("id_ecm_doc")
    private String ecmId;
    @JsonProperty("doc_type")
    private PerecoderObject docType;
    @JsonProperty("contract_type")
    private PerecoderObject contractType;

    @JsonProperty("id_as_doc")
    private String asId;

    @JsonProperty("list_flmd")
    private List<String> fileIds;

    @Setter(onMethod = @__(@Since(major = 1, minor = 1, patch = 18)))
    @Getter(onMethod = @__(@Since(major = 1, minor = 1, patch = 18)))
    @Since(major = 1, minor = 1, patch = 18)
    @JsonProperty("hide_archived")
    private boolean hideArchived = true;
}

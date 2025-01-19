package ru.bpmcons.sbi_elma.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.models.ability.Identifiable;
import ru.bpmcons.sbi_elma.models.dto.generralized.Creator_editor;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;
import ru.bpmcons.sbi_elma.validation.ValidIdentifiable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Valid
@ValidIdentifiable
@EqualsAndHashCode(callSuper = true)
public class DeleteDocRequest extends AuthenticatedRequestBase implements Identifiable {
    @JsonProperty("id_ecm_doc")
    private String ecmId;
    @JsonProperty("doc_type")
    private PerecoderObject docType;
    @JsonProperty("contract_type")
    private PerecoderObject contractType;

    @JsonProperty("id_as_doc")
    private String asId;
    @NotNull
    private Creator_editor editor;
}

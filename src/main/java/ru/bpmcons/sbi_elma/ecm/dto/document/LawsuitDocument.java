package ru.bpmcons.sbi_elma.ecm.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.ecm.dto.base.DocumentBase;
import ru.bpmcons.sbi_elma.ecm.dto.reference.DocParties;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DocPartiesTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.FlowTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.MedicalDocTrait;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LawsuitDocument extends DocumentBase implements DocPartiesTrait, MedicalDocTrait, FlowTrait {
    @JsonProperty("doc_parties")
    private DocParties docParties;
    @JsonProperty("medical_doc")
    private boolean medicalDoc;

    @SerializeAsArray
    @JsonProperty("flow")
    private String flow;
}

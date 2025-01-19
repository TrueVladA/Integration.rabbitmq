package ru.bpmcons.sbi_elma.ecm.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.ecm.dto.base.DocumentBase;
import ru.bpmcons.sbi_elma.ecm.dto.reference.DocParties;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DealTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DocNameTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DocPartiesTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.MedicalDocTrait;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExtractDocument extends DocumentBase implements DocNameTrait, DocPartiesTrait, DealTrait, MedicalDocTrait {
    @JsonProperty("doc_name")
    private String docName;
    @JsonProperty("deal")
    private String deal;
    @JsonProperty("doc_parties")
    private DocParties docParties;
    @JsonProperty("medical_doc")
    private boolean medicalDoc;
}

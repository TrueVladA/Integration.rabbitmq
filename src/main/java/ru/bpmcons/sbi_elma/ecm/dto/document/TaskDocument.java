package ru.bpmcons.sbi_elma.ecm.dto.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.ecm.dto.base.DocumentWithContractBase;
import ru.bpmcons.sbi_elma.ecm.dto.reference.DocParties;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DocNameTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DocPartiesTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.MedicalDocTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.ProductLineTrait;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TaskDocument extends DocumentWithContractBase implements DocPartiesTrait, ProductLineTrait, DocNameTrait, MedicalDocTrait {
    @JsonProperty("doc_parties")
    private DocParties docParties;
    @JsonProperty("doc_name")
    private String docName;
    @JsonProperty("product_line")
    @JsonInclude
    @SerializeAsArray
    private String productLine;
    @JsonInclude
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("medical_doc")
    private boolean medicalDoc;
}

package ru.bpmcons.sbi_elma.ecm.dto.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.ecm.dto.base.DocumentWithContractBase;
import ru.bpmcons.sbi_elma.ecm.dto.reference.DocParties;
import ru.bpmcons.sbi_elma.ecm.dto.trait.*;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

import java.util.Date;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LetterDocument extends DocumentWithContractBase implements DocNameTrait, DealTrait, ContractPeriodTrait, ProductLineTrait, DocPartiesTrait, MedicalDocTrait {
    @JsonProperty("docflow")
    private String docflow; // todo нигде не заполняется

    @JsonProperty("doc_name")
    private String docName;
    @JsonProperty("deal")
    private String deal;
    @JsonProperty("contract_start_date")
    private Date contractStartDate;
    @JsonProperty("contract_end_date")
    private Date contractEndDate;
    @JsonInclude
    @SerializeAsArray
    @JsonProperty("product_line")
    private String productLine;
    @JsonInclude
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("doc_parties")
    private DocParties docParties;
    @JsonProperty("medical_doc")
    private boolean medicalDoc;
}

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
public class ComplaintDocument extends DocumentWithContractBase implements ContractPeriodTrait, ProductLineTrait, DocNameTrait, DocPartiesTrait, MedicalDocTrait {
    @JsonProperty("contract_start_date")
    private Date contractStartDate;
    @JsonProperty("contract_end_date")
    private Date contractEndDate;
    @JsonProperty("doc_name")
    private String docName;
    @JsonProperty("doc_parties")
    private DocParties docParties;
    @JsonInclude
    @JsonProperty("product_line")
    @SerializeAsArray
    private String productLine;
    @JsonInclude
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("medical_doc")
    private boolean medicalDoc;
}

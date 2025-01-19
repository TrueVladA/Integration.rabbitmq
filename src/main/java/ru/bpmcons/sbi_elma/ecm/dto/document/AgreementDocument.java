package ru.bpmcons.sbi_elma.ecm.dto.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.ecm.dto.base.DocumentWithContractBase;
import ru.bpmcons.sbi_elma.ecm.dto.reference.DocParties;
import ru.bpmcons.sbi_elma.ecm.dto.trait.ContractPartiesTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.ContractPeriodTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.MedicalDocTrait;
import ru.bpmcons.sbi_elma.ecm.dto.trait.ProductLineTrait;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

import java.util.Date;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AgreementDocument extends DocumentWithContractBase implements ContractPeriodTrait, ContractPartiesTrait, ProductLineTrait, MedicalDocTrait {
    @JsonProperty("contract_start_date")
    private Date contractStartDate;
    @JsonProperty("contract_end_date")
    private Date contractEndDate;
    @JsonProperty("contract_parties")
    private DocParties contractParties;
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

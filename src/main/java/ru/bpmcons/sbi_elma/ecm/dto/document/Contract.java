package ru.bpmcons.sbi_elma.ecm.dto.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.ecm.dto.base.EntityBase;
import ru.bpmcons.sbi_elma.ecm.dto.reference.DocParties;
import ru.bpmcons.sbi_elma.ecm.dto.trait.*;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

import java.util.Date;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Contract extends EntityBase implements ContractTrait, ContractPeriodTrait, ContractPartiesTrait, ProductLineTrait, MedicalDocTrait {
    @SerializeAsArray
    @JsonProperty("contract_type")
    private String contractType;
    @JsonProperty("contract_series")
    private String contractSeries;
    @JsonProperty("contract_number")
    private String contractNumber;
    @JsonProperty("contract_full_number")
    private String contractFullNumber;
    @JsonProperty("contract_date")
    private Date contractDate;
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

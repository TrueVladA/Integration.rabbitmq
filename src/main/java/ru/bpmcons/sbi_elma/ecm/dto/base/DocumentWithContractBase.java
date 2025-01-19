package ru.bpmcons.sbi_elma.ecm.dto.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.ecm.dto.trait.ContractTrait;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

import java.util.Date;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DocumentWithContractBase extends DocumentBase implements ContractTrait {
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
}

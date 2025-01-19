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

import java.math.BigDecimal;
import java.util.Date;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FinDocument extends DocumentWithContractBase implements DocPartiesTrait, ProductLineTrait, ContractPeriodTrait, CommentTrait, PaymentPurposeTrait, SumTrait {
    @JsonProperty("contract_start_date")
    private Date contractStartDate;
    @JsonProperty("contract_end_date")
    private Date contractEndDate;
    @JsonProperty("doc_parties")
    private DocParties docParties;
    @JsonProperty("product_line")
    @JsonInclude
    @SerializeAsArray
    private String productLine;
    @JsonInclude
    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("currency")
    @SerializeAsArray
    private String currency;
    @JsonProperty("summ")
    private BigDecimal sum;

    @JsonProperty("payment_purpose")
    private String paymentPurpose;
}

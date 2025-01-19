package ru.bpmcons.sbi_elma.models.dto.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.models.dto.DocWithFileMetadata;
import ru.bpmcons.sbi_elma.models.dto.contract.ParentDoc;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

import java.math.BigDecimal;
import java.util.Date;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseFromPublicApi extends DocWithFileMetadata {
    private String id_as;
    private String[] source;
    private String[] doc_type;
    private String doc_series;
    private String doc_number;
    private String doc_full_number;
    private String[] contract_type;
    private String contract_series;
    private String contract_number;
    private String contract_full_number;
    private Date doc_date;
    private Date contract_date;
    private Date contract_start_date;
    private Date contract_end_date;
    private String[] creator;
    private String[] editor;
    private String status;
    private ParentDoc parent_doc;
    private DocParties doc_parties;
    private DocParties contract_parties;
    private String[] product_line;
    private String product_name;
    private boolean medical_doc;
    private boolean archive;
    private String __id;
    private String __name;
    private String doc_name;


    @Nullable
    @JsonProperty("comment")
    private String comment;
    @Nullable
    @JsonProperty("currency")
    private String[] currency;
    @Nullable
    @JsonProperty("summ")
    private BigDecimal sum;
    @Nullable
    @JsonProperty("payment_purpose")
    private String paymentPurpose;

    @Nullable
    @JsonProperty("damage_dks")
    private Boolean damageDks;
    @Nullable
    @SerializeAsArray
    @JsonProperty("flow")
    private String flow;
}

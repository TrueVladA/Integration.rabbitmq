package ru.bpmcons.sbi_elma.ecm.dto.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DocumentTrait;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

import java.util.Date;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DocumentBase extends EntityBase implements DocumentTrait {
    @JsonProperty("doc_type")
    @SerializeAsArray
    private String docType;
    @JsonProperty("doc_series")
    private String docSeries;
    @JsonProperty("doc_number")
    private String docNumber;
    @JsonProperty("doc_full_number")
    private String docFullNumber;
    @JsonProperty("doc_date")
    private Date docDate;
}

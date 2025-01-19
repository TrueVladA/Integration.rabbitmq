package ru.bpmcons.sbi_elma.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;
import ru.bpmcons.sbi_elma.serialization.StringDeserialization;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchDocRequest extends AuthenticatedRequestBase {
    @JsonProperty("doc_type")
    private PerecoderObject docType;
    @JsonProperty("doc_series")
    @JsonDeserialize(using = StringDeserialization.class)
    private String docSeries;
    @JsonProperty("doc_number")
    @JsonDeserialize(using = StringDeserialization.class)
    private String docNumber;
    @JsonProperty("doc_full_number")
    @JsonDeserialize(using = StringDeserialization.class)
    private String docFullNumber;

    @JsonProperty("contract_type")
    private PerecoderObject contractType;
    @JsonProperty("contract_series")
    @JsonDeserialize(using = StringDeserialization.class)
    private String contractSeries;
    @JsonProperty("contract_number")
    @JsonDeserialize(using = StringDeserialization.class)
    private String contractNumber;
    @JsonProperty("contract_full_number")
    @JsonDeserialize(using = StringDeserialization.class)
    private String contractFullNumber;
}

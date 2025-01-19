package ru.bpmcons.sbi_elma.ecm.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.ecm.dto.base.FileMetadataContainer;
import ru.bpmcons.sbi_elma.ecm.dto.reference.Fio;
import ru.bpmcons.sbi_elma.elma.ElmaJsonView;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

import java.util.Date;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IdentityDocument extends FileMetadataContainer {
    @JsonProperty("__id")
    @ElmaJsonView
    private String id;

    @JsonProperty("id_as")
    private String externalId;

    @SerializeAsArray
    @JsonProperty("type_identitydoc")
    private String identityDocType;

    @JsonProperty("fio")
    private Fio fio;

    @JsonProperty("series")
    private String series;

    @JsonProperty("number")
    private String number;

    @JsonProperty("full_number")
    private String fullNumber;

    @JsonProperty("issued")
    private String issued;

    @JsonProperty("issue_date")
    private Date issueDate;

    @JsonProperty("end_date")
    private Date endDate;

    @JsonProperty("source")
    @SerializeAsArray
    private String source;

    @JsonProperty("archive")
    private boolean archive;
}

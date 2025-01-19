package ru.bpmcons.sbi_elma.ecm.dto.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.bpmcons.sbi_elma.elma.ElmaJsonView;
import ru.bpmcons.sbi_elma.ecm.dto.reference.ParentDoc;
import ru.bpmcons.sbi_elma.ecm.dto.trait.BaseEntityTrait;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EntityBase extends FileMetadataContainer implements BaseEntityTrait {
    @JsonProperty("__id")
    @ElmaJsonView
    private String id;

    @JsonProperty("id_as")
    private String externalId;

    @JsonProperty("source")
    @SerializeAsArray
    private String source;

    @SerializeAsArray
    @JsonProperty("creator")
    private String creator;
    @SerializeAsArray
    @JsonProperty("editor")
    private String editor;

    @JsonProperty("status")
    private String status;

    @JsonProperty("archive")
    private boolean archive;

    @JsonProperty("parent_doc")
    private ParentDoc parentDoc;
}

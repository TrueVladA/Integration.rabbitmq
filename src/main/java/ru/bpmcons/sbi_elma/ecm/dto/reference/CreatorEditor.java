package ru.bpmcons.sbi_elma.ecm.dto.reference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatorEditor {
    @JsonProperty("__id")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String id;
    @JsonProperty("__name")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String name;
    @JsonProperty("id_ecm_creator")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String ecmId;
    @JsonProperty("id_as_creator")
    private String externalId;
    @JsonProperty("source")
    @SerializeAsArray
    private String source;
    @JsonProperty("fio")
    private Fio fio;
    @JsonProperty("role")
    private String role;
    @JsonProperty("email")
    private Email[] email;
}

package ru.bpmcons.sbi_elma.ecm.dto.reference;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.bpmcons.sbi_elma.utils.SerializeAsArray;

import java.util.Date;
import java.util.List;

@Data
public class DocParty {
    private String dul;     //Строка dul
    private Fio fio;
    private String inn;
    @JsonProperty("id_as")
    private String externalId;
    @SerializeAsArray
    private String identitydoc;
    private Date birthdate;
    @SerializeAsArray
    private String opf;
    @SerializeAsArray
    private String vip;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("party_role")
    private List<String> partyRoles;
    @SerializeAsArray
    @JsonProperty("party_type")
    private String partyType;
    @JsonProperty("short_name")
    private String shortName;
    private String email;
}

package ru.bpmcons.sbi_elma.ecm.dto.reference;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Fio {
    @JsonProperty("lastname")
    private String lastname;
    @JsonProperty("firstname")
    private String firstname;
    @JsonProperty("middlename")
    private String middlename;
}

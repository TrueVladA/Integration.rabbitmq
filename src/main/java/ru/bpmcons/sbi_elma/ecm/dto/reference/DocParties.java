package ru.bpmcons.sbi_elma.ecm.dto.reference;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocParties {
    @JsonProperty("rows")
    private List<DocParty> rows;
}

package ru.bpmcons.sbi_elma.models.dto.generralized;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import ru.bpmcons.sbi_elma.models.ability.Identifiable;

@Data
public class InputParentDoc implements Identifiable {
    private String id_ecm_doc;
    private String id_as_doc;
    private String app_id;
    private String series;
    private String number;
    private PerecoderObject doc_type;
    private PerecoderObject contract_type;

    @Override
    @JsonIgnore
    public PerecoderObject getDocType() {
        return doc_type;
    }

    @Override
    @JsonIgnore
    public PerecoderObject getContractType() {
        return contract_type;
    }

    @Override
    @JsonIgnore
    public String getEcmId() {
        return id_ecm_doc;
    }
}

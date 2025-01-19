package ru.bpmcons.sbi_elma.models.ability;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;

public interface Identifiable extends DocumentTyped, ContractTyped {
    @Nullable
    PerecoderObject getDocType();
    @Nullable
    PerecoderObject getContractType();
    @NonNull
    String getEcmId();
}

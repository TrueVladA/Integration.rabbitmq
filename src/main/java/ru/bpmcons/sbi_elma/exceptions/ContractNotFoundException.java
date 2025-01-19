package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.ecm.dto.dict.ContractType;

public class ContractNotFoundException extends EcmDocumentAwareException {
    public ContractNotFoundException(ContractType contractType, String ecmId, String asId) {
        super(HttpStatus.CONFLICT.value(), "Договор " + contractType.getEcmName() + " с id = " + ecmId + " (id_as = " + asId + ") не существует", ecmId, asId);
    }
}

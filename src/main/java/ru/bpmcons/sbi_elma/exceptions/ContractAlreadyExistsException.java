package ru.bpmcons.sbi_elma.exceptions;

import org.springframework.http.HttpStatus;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

import java.util.Arrays;

public class ContractAlreadyExistsException extends EcmDocumentAwareException {
    public ContractAlreadyExistsException(GeneralizedDoc doc, String ecmId) {
        super(HttpStatus.CONFLICT.value(), "Такой контракт уже существует (contract_full_number = '" + doc.getContract_full_number() + "', id_as = '" + doc.getId_as_doc() + "', contract_type = '" + Arrays.toString(doc.getContract_type().getDictValues()) + "')", ecmId, doc.getId_as_doc());
    }
}

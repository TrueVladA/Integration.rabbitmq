package ru.bpmcons.sbi_elma.ecm.dto.trait;

import ru.bpmcons.sbi_elma.ecm.dto.reference.DocParties;

public interface ContractPartiesTrait {
    DocParties getContractParties();

    void setContractParties(DocParties contractParties);
}

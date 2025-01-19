package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.EcmPartiesConverter;
import ru.bpmcons.sbi_elma.ecm.dto.trait.ContractPartiesTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
@RequiredArgsConstructor
public class ContractPartiesTraitMapper implements TraitMapper<ContractPartiesTrait> {
    private final EcmPartiesConverter ecmPartiesConverter;

    @Override
    public void mapRequired(GeneralizedDoc doc, ContractPartiesTrait target, ContractPartiesTrait existingDoc) {
        if (doc.getDoc_parties() != null) {
            target.setContractParties(ecmPartiesConverter.convert(doc.getDoc_parties()));
        }
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof ContractPartiesTrait;
    }
}

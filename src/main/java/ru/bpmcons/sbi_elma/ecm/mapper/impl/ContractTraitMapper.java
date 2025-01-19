package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.dict.ContractType;
import ru.bpmcons.sbi_elma.ecm.dto.document.Contract;
import ru.bpmcons.sbi_elma.ecm.dto.trait.ContractTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.ContractTypeRepository;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
@RequiredArgsConstructor
public class ContractTraitMapper implements TraitMapper<ContractTrait> {
    private final ContractTypeRepository contractTypeRepository;

    @Override
    public void mapRequired(GeneralizedDoc doc, ContractTrait target, ContractTrait existingDoc) {
        if ((doc.getContract_type() != null && doc.getContract_type().valid()) || target instanceof Contract) {
            ContractType type = contractTypeRepository.findByTypeSysName(doc.getContract_type());
            target.setContractType(type.getId());
        }
        target.setContractDate(doc.getContract_date());
        target.setContractNumber(doc.getContract_number());
        target.setContractSeries(doc.getContract_series());
        target.setContractFullNumber(doc.getContract_full_number());
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof ContractTrait;
    }
}

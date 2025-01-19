package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.trait.ContractPeriodTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
public class ContractPeriodTraitMapper implements TraitMapper<ContractPeriodTrait> {
    @Override
    public void mapRequired(GeneralizedDoc doc, ContractPeriodTrait target, ContractPeriodTrait existingDoc) {
        target.setContractEndDate(doc.getContract_end_date());
        target.setContractStartDate(doc.getContract_start_date());
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof ContractPeriodTrait;
    }
}

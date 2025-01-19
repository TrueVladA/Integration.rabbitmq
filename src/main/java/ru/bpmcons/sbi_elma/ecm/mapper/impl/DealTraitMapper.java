package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DealTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
public class DealTraitMapper implements TraitMapper<DealTrait> {
    @Override
    public void mapRequired(GeneralizedDoc doc, DealTrait target, DealTrait existingDoc) {
        target.setDeal(doc.getDeal());
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof DealTrait;
    }
}

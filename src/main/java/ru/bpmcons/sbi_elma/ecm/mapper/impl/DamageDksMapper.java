package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DamageDksTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
public class DamageDksMapper implements TraitMapper<DamageDksTrait> {
    @Override
    public void mapRequired(GeneralizedDoc doc, DamageDksTrait target, DamageDksTrait existingDoc) {
        if (doc.getDamageDks() != null) {
            target.setDamageDks(doc.getDamageDks().toString());
        }
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof DamageDksTrait;
    }
}

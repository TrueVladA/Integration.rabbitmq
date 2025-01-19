package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DocNameTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
public class DocNameTraitMapper implements TraitMapper<DocNameTrait> {
    @Override
    public void mapRequired(GeneralizedDoc doc, DocNameTrait target, DocNameTrait existingDoc) {
        target.setDocName(doc.getDoc_name());
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof DocNameTrait;
    }
}

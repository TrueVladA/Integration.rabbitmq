package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.trait.CommentTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
public class CommentTraitMapper implements TraitMapper<CommentTrait> {
    @Override
    public boolean isApplicable(Object target) {
        return target instanceof CommentTrait;
    }

    @Override
    public void mapRequired(GeneralizedDoc doc, CommentTrait target, CommentTrait existingDoc) {
        target.setComment(doc.getComment());
    }
}

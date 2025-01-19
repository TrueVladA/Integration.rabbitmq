package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DocumentTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DocTypeRepository;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
@RequiredArgsConstructor
public class DocumentTraitMapper implements TraitMapper<DocumentTrait> {
    private final DocTypeRepository docTypeRepository;

    @Override
    public void mapRequired(GeneralizedDoc doc, DocumentTrait target, DocumentTrait existingDoc) {
        target.setDocSeries(doc.getDoc_series());
        target.setDocNumber(doc.getDoc_number());
        target.setDocFullNumber(doc.getDoc_full_number());
        target.setDocDate(doc.getDoc_date());
        target.setDocType(docTypeRepository.findBySysName(doc.getDoc_type()).getId());
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof DocumentTrait;
    }
}

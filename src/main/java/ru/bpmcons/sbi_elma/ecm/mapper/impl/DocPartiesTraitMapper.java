package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.EcmPartiesConverter;
import ru.bpmcons.sbi_elma.ecm.dto.trait.DocPartiesTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
@RequiredArgsConstructor
public class DocPartiesTraitMapper implements TraitMapper<DocPartiesTrait> {
    private final EcmPartiesConverter ecmPartiesConverter;

    @Override
    public void mapRequired(GeneralizedDoc doc, DocPartiesTrait target, DocPartiesTrait existingDoc) {
        if (doc.getDoc_parties() != null) {
            target.setDocParties(ecmPartiesConverter.convert(doc.getDoc_parties()));
        }
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof DocPartiesTrait;
    }
}

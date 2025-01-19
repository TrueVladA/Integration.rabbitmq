package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.dict.DocFlow;
import ru.bpmcons.sbi_elma.ecm.dto.trait.FlowTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.DocFlowRepository;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
@RequiredArgsConstructor
public class FlowTraitMapper implements TraitMapper<FlowTrait> {
    private final DocFlowRepository docFlowRepository;

    @Override
    public void mapRequired(GeneralizedDoc doc, FlowTrait target, FlowTrait existingDoc) {
        if (doc.getFlow() != null) {
            DocFlow flow = docFlowRepository.findByCode(doc.getFlow());
            target.setFlow(flow.getId());
        }
    }

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof FlowTrait;
    }
}

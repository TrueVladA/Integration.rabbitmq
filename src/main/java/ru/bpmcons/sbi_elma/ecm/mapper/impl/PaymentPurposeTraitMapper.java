package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.trait.PaymentPurposeTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
public class PaymentPurposeTraitMapper implements TraitMapper<PaymentPurposeTrait> {
    @Override
    public boolean isApplicable(Object target) {
        return target instanceof PaymentPurposeTrait;
    }

    @Override
    public void mapRequired(GeneralizedDoc doc, PaymentPurposeTrait target, PaymentPurposeTrait existingDoc) {
        target.setPaymentPurpose(doc.getPaymentPurpose());
    }
}

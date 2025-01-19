package ru.bpmcons.sbi_elma.ecm.mapper.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bpmcons.sbi_elma.ecm.dto.trait.SumTrait;
import ru.bpmcons.sbi_elma.ecm.mapper.TraitMapper;
import ru.bpmcons.sbi_elma.ecm.repository.dictionary.CurrencyRepository;
import ru.bpmcons.sbi_elma.exceptions.CheckRequiredParametersException;
import ru.bpmcons.sbi_elma.models.dto.generralized.GeneralizedDoc;

@Service
@RequiredArgsConstructor
public class SumTraitMapper implements TraitMapper<SumTrait> {
    private final CurrencyRepository currencyRepository;

    @Override
    public boolean isApplicable(Object target) {
        return target instanceof SumTrait;
    }

    @Override
    public void mapRequired(GeneralizedDoc doc, SumTrait target, SumTrait existingDoc) {
        if (doc.getSum() != null && doc.getCurrencyNum() == null) {
            throw new CheckRequiredParametersException(400, "При заполнении суммы документа необходимо также заполнить валюту (currency_num)");
        }

        target.setSum(doc.getSum());
        if (doc.getCurrencyNum() != null) {
            target.setCurrency(currencyRepository.findByDigitalCode(doc.getCurrencyNum().toString()).getId());
        }
    }
}

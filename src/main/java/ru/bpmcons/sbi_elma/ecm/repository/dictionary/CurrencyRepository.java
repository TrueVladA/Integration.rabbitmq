package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.Currency;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;

public interface CurrencyRepository extends DictionaryRepository<Currency> {
    Currency findByDigitalCode(String code);

    Currency findById(String id);
}

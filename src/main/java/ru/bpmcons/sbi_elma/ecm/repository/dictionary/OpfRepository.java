package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.Opf;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;

public interface OpfRepository extends DictionaryRepository<Opf> {
    Opf findByCode(String code);

    Opf findById(String id);
}

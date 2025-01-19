package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.DocFlow;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;

public interface DocFlowRepository extends DictionaryRepository<DocFlow> {
    DocFlow findById(String id);

    DocFlow findByCode(String code);
}

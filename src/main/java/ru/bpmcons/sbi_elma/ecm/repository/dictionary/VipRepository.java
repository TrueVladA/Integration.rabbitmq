package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.Vip;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;

public interface VipRepository extends DictionaryRepository<Vip> {
    Vip findBySysNameAndSource(String sysName, String source);

    Vip findById(String id);
}

package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.PartyRole;
import ru.bpmcons.sbi_elma.infra.dictionary.normalize.LowercaseDictionaryKeyNormalizer;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.Normalize;

public interface PartyRoleRepository extends DictionaryRepository<PartyRole> {
    PartyRole findById(String id);

    @Normalize(LowercaseDictionaryKeyNormalizer.class)
    PartyRole findBySysName(String sysName);
}

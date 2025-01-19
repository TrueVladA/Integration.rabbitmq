package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.DulType;
import ru.bpmcons.sbi_elma.infra.dictionary.normalize.LowercaseDictionaryKeyNormalizer;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.Normalize;

import java.util.Optional;

public interface DulTypeRepository extends DictionaryRepository<DulType> {
    DulType findById(String id);

    @Normalize(LowercaseDictionaryKeyNormalizer.class)
    DulType findByCode(String code);

    @Normalize(LowercaseDictionaryKeyNormalizer.class)
    Optional<DulType> findByCodeOptional(String code);
}

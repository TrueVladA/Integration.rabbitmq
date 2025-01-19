package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.DocType;
import ru.bpmcons.sbi_elma.infra.dictionary.normalize.LowercaseDictionaryKeyNormalizer;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.Normalize;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;

import java.util.Optional;

public interface DocTypeRepository extends DictionaryRepository<DocType> {
    DocType findById(String id);

    @Normalize(LowercaseDictionaryKeyNormalizer.class)
    DocType findBySysName(String sysName);

    @Normalize(LowercaseDictionaryKeyNormalizer.class)
    Optional<DocType> findBySysNameOptional(String sysName);

    default DocType findBySysName(PerecoderObject sysName) {
        return findBySysName(sysName == null ? null : sysName.getSingleValue());
    }
}

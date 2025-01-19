package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.ContractType;
import ru.bpmcons.sbi_elma.infra.dictionary.normalize.LowercaseDictionaryKeyNormalizer;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.Normalize;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;

import java.util.Optional;

public interface ContractTypeRepository extends DictionaryRepository<ContractType> {
    ContractType findById(String id);

    @Normalize(LowercaseDictionaryKeyNormalizer.class)
    ContractType findByTypeSysName(String name);

    @Normalize(LowercaseDictionaryKeyNormalizer.class)
    Optional<ContractType> findByTypeSysNameOptional(String name);

    default ContractType findByTypeSysName(PerecoderObject object) {
        return findByTypeSysName(object == null ? null : object.getSingleValue());
    }
}

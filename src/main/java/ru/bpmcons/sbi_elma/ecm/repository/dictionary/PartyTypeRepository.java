package ru.bpmcons.sbi_elma.ecm.repository.dictionary;

import ru.bpmcons.sbi_elma.ecm.dto.dict.PartyType;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.DictionaryRepository;
import ru.bpmcons.sbi_elma.models.dto.generralized.PerecoderObject;

import java.util.Optional;

public interface PartyTypeRepository extends DictionaryRepository<PartyType> {
    Optional<PartyType> findByIdOptional(String id);

    PartyType findBySysName(String sysName);

    default PartyType findBySysName(PerecoderObject object) {
        return findBySysName(object == null ? null : object.getSingleValue());
    }
}
